<?php
header('Content-Type: application/json');

// Connect to MySQL using mysqli
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    error_log("DB Connection failed: " . $conn->connect_error);
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Connection failed"]);
    exit;
}

// Read JSON input
$inputRaw = file_get_contents("php://input");
if ($inputRaw === false) {
    error_log("Failed to read input stream");
    http_response_code(400);
    echo json_encode(["success" => false, "error" => "Empty request body"]);
    $conn->close();
    exit;
}

$input = json_decode($inputRaw, true);
if (!is_array($input)) {
    error_log("Invalid JSON: " . $inputRaw);
    http_response_code(400);
    echo json_encode(["success" => false, "error" => "Invalid JSON"]);
    $conn->close();
    exit;
}

error_log("Received input: " . json_encode($input));

// Extract and validate fields
$cid = isset($input['cid']) ? intval($input['cid']) : null;
$ostatus = isset($input['ostatus']) ? intval($input['ostatus']) : 1;
$items = isset($input['items']) && is_array($input['items']) ? $input['items'] : [];
$sid = isset($input['sid']) && $input['sid'] !== "" ? intval($input['sid']) : null;
$table_number = isset($input['table_number']) && $input['table_number'] !== "" ? intval($input['table_number']) : null;
$total_amount = isset($input['total_amount']) ? intval($input['total_amount']) : 0; // in cents
$actor = isset($input['actor']) ? $input['actor'] : 'system';

if ($cid === null || count($items) === 0) {
    error_log("Missing required fields: cid=" . var_export($cid, true) . ", items=" . json_encode($items));
    http_response_code(400);
    echo json_encode(["success" => false, "error" => "Missing required fields"]);
    $conn->close();
    exit;
}

// Start transaction
if (!$conn->begin_transaction()) {
    error_log("Failed to start transaction: " . $conn->error);
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Failed to start transaction"]);
    $conn->close();
    exit;
}

try {
    // Insert into orders
    $odate = date('Y-m-d H:i:s');
    $orderRef = isset($input['orderRef']) ? $input['orderRef'] : ('temp_order_' . round(microtime(true) * 1000));

    $stmt = $conn->prepare("INSERT INTO orders (odate, cid, ostatus, orderRef) VALUES (?, ?, ?, ?)");
    if (!$stmt) {
        throw new Exception("Prepare failed for orders: " . $conn->error);
    }
    $stmt->bind_param("siis", $odate, $cid, $ostatus, $orderRef);
    if (!$stmt->execute()) {
        $err = $stmt->error;
        $stmt->close();
        throw new Exception("Execute failed for orders: " . $err);
    }
    $order_id = $stmt->insert_id;
    error_log("Order header saved with ID: $order_id");
    $stmt->close();

    // Insert each item into order_items
    $itemStmt = $conn->prepare("INSERT INTO order_items (oid, item_id, qty) VALUES (?, ?, ?)");
    if (!$itemStmt) {
        throw new Exception("Prepare failed for order_items: " . $conn->error);
    }

    foreach ($items as $item) {
        $item_id = isset($item['item_id']) ? intval($item['item_id']) : null;
        $qty = isset($item['qty']) ? intval($item['qty']) : null;

        if ($item_id === null || $qty === null || $qty <= 0) {
            error_log("Skipping invalid item: " . json_encode($item));
            continue;
        }

        $itemStmt->bind_param("iii", $order_id, $item_id, $qty);
        if (!$itemStmt->execute()) {
            error_log("Execute failed for item: item_id=$item_id, qty=$qty — " . $itemStmt->error);
        } else {
            error_log("Item saved: order_id=$order_id, item_id=$item_id, qty=$qty");
        }
    }
    $itemStmt->close();

    // Staff-specific logic for table_orders: insert or update
    $table_order_id = null;
    if ($sid !== null && $table_number !== null && $table_number !== "" && $table_number !== 0) {
        // Try to find existing table_orders for this table_number
        $selectStmt = $conn->prepare("SELECT toid FROM table_orders WHERE table_number = ? LIMIT 1");
        if (!$selectStmt) {
            throw new Exception("Prepare failed for table_orders select: " . $conn->error);
        }
        $selectStmt->bind_param("i", $table_number);
        if (!$selectStmt->execute()) {
            $selectStmt->close();
            throw new Exception("Execute failed for table_orders select: " . $selectStmt->error);
        }
        $selectStmt->bind_result($existing_toid);
        $hasExisting = $selectStmt->fetch();
        $selectStmt->close();

        if ($hasExisting && $existing_toid) {
            // Update existing row
            $updateStmt = $conn->prepare("UPDATE table_orders SET oid = ?, staff_id = ?, status = 'ordering', updated_at = CURRENT_TIMESTAMP WHERE toid = ?");
            if (!$updateStmt) {
                throw new Exception("Prepare failed for table_orders update: " . $conn->error);
            }
            $updateStmt->bind_param("iii", $order_id, $sid, $existing_toid);
            if (!$updateStmt->execute()) {
                $updateStmt->close();
                throw new Exception("Execute failed for table_orders update: " . $updateStmt->error);
            }
            $table_order_id = $existing_toid;
            $updateStmt->close();
            error_log("Updated table_orders toid=$table_order_id with order_id=$order_id");
        } else {
            // Insert new row
            $tableStmt = $conn->prepare("INSERT INTO table_orders (table_number, oid, staff_id, status) VALUES (?, ?, ?, 'ordering')");
            if (!$tableStmt) {
                throw new Exception("Prepare failed for table_orders insert: " . $conn->error);
            }
            $tableStmt->bind_param("iii", $table_number, $order_id, $sid);
            if (!$tableStmt->execute()) {
                $err = $tableStmt->error;
                $tableStmt->close();
                throw new Exception("Execute failed for table_orders insert: " . $err);
            }
            $table_order_id = $tableStmt->insert_id;
            $tableStmt->close();
            error_log("Table order saved with ID: $table_order_id");
        }
    }

    // Coupon point logic: 1 HK$ = 1 point, total_amount expected in cents, skip walk-in cid=0
    $pointsAdded = 0;
    if ($cid !== 0 && $total_amount > 0) {
        $pointsToAdd = intdiv($total_amount, 100); // integer division cents -> dollars
        if ($pointsToAdd > 0) {
            // Ensure a coupon_point row exists for this customer
            $sel = $conn->prepare("SELECT cp_id, points FROM coupon_point WHERE cid = ? LIMIT 1");
            if (!$sel) {
                throw new Exception("Prepare failed coupon select: " . $conn->error);
            }
            $sel->bind_param("i", $cid);
            if (!$sel->execute()) {
                $sel->close();
                throw new Exception("Execute failed coupon select: " . $sel->error);
            }
            $sel->bind_result($cp_id_existing, $existing_points);
            $hasRow = $sel->fetch();
            $sel->close();

            if ($hasRow && $cp_id_existing) {
                $newPoints = $existing_points + $pointsToAdd;
                $upd = $conn->prepare("UPDATE coupon_point SET points = ?, last_changed_by = ?, reason = ?, updated_at = CURRENT_TIMESTAMP WHERE cp_id = ?");
                if (!$upd) {
                    throw new Exception("Prepare failed coupon update: " . $conn->error);
                }
                $reason = "Earned from order $order_id";
                $upd->bind_param("issi", $newPoints, $actor, $reason, $cp_id_existing);
                if (!$upd->execute()) {
                    $upd->close();
                    throw new Exception("Execute failed coupon update: " . $upd->error);
                }
                $upd->close();

                // Insert history
                $hist = $conn->prepare("INSERT INTO coupon_point_history (cp_id, cid, delta, resulting_points, action, note) VALUES (?, ?, ?, ?, 'earn', ?)");
                if ($hist) {
                    $note = "Order $order_id";
                    $hist->bind_param("iiiss", $cp_id_existing, $cid, $pointsToAdd, $newPoints, $note);
                    $hist->execute();
                    $hist->close();
                }
                $pointsAdded = $pointsToAdd;
            } else {
                // Insert new coupon_point row
                $ins = $conn->prepare("INSERT INTO coupon_point (cid, points, last_changed_by, reason) VALUES (?, ?, ?, ?)");
                if (!$ins) {
                    throw new Exception("Prepare failed coupon insert: " . $conn->error);
                }
                $reason = "Earned from order $order_id";
                $ins->bind_param("iiss", $cid, $pointsToAdd, $actor, $reason);
                if (!$ins->execute()) {
                    $ins->close();
                    throw new Exception("Execute failed coupon insert: " . $ins->error);
                }
                $newCpId = $ins->insert_id;
                $ins->close();

                // Insert history
                $hist = $conn->prepare("INSERT INTO coupon_point_history (cp_id, cid, delta, resulting_points, action, note) VALUES (?, ?, ?, ?, 'earn', ?)");
                if ($hist) {
                    $note = "Order $order_id";
                    $hist->bind_param("iiiss", $newCpId, $cid, $pointsToAdd, $pointsToAdd, $note);
                    $hist->execute();
                    $hist->close();
                }
                $pointsAdded = $pointsToAdd;
            }
        }
    }

    // Commit transaction
    if (!$conn->commit()) {
        throw new Exception("Commit failed: " . $conn->error);
    }

    $response = ["success" => true, "order_id" => $order_id, "points_added" => $pointsAdded];
    if ($table_order_id !== null) {
        $response["table_order_id"] = $table_order_id;
    }
    echo json_encode($response);
    $conn->close();
    exit;
} catch (Exception $e) {
    error_log("Transaction failed: " . $e->getMessage());
    $conn->rollback();
    http_response_code(500);
    echo json_encode(["success" => false, "error" => $e->getMessage()]);
    $conn->close();
    exit;
}
?>