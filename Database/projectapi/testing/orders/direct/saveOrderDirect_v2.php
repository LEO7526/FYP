<?php
/**
 * 保存訂單（包括菜品自訂選項）
 * 支持：訂單商品、自訂選項、優惠券、積分系統
 */
header('Content-Type: application/json; charset=utf-8');

// Connect to MySQL
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
    http_response_code(400);
    echo json_encode(["success" => false, "error" => "Empty request body"]);
    $conn->close();
    exit;
}
$input = json_decode($inputRaw, true);
if (!is_array($input)) {
    http_response_code(400);
    echo json_encode(["success" => false, "error" => "Invalid JSON"]);
    $conn->close();
    exit;
}

// Extract fields
$cid          = isset($input['cid']) ? intval($input['cid']) : null;
$ostatus      = isset($input['ostatus']) ? intval($input['ostatus']) : 1;
$items        = isset($input['items']) && is_array($input['items']) ? $input['items'] : [];
$sid          = isset($input['sid']) && $input['sid'] !== "" ? intval($input['sid']) : null;
$table_number = isset($input['table_number']) && $input['table_number'] !== "" ? intval($input['table_number']) : null;
$total_amount = isset($input['total_amount']) ? intval($input['total_amount']) : 0; // in cents
$coupon_id    = isset($input['coupon_id']) ? intval($input['coupon_id']) : null;
$actor        = isset($input['actor']) ? $input['actor'] : 'system';

if ($cid === null || count($items) === 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "error" => "Missing required fields"]);
    $conn->close();
    exit;
}

// Start transaction
$conn->begin_transaction();

try {
    // Insert into orders (with coupon_id)
    $odate = date('Y-m-d H:i:s');
    $orderRef = isset($input['orderRef']) ? $input['orderRef'] : ('temp_order_' . round(microtime(true) * 1000));

    $stmt = $conn->prepare("INSERT INTO orders (odate, cid, ostatus, orderRef, coupon_id) VALUES (?, ?, ?, ?, ?)");
    if (!$stmt) throw new Exception("Prepare failed for orders: " . $conn->error);
    $stmt->bind_param("siisi", $odate, $cid, $ostatus, $orderRef, $coupon_id);
    if (!$stmt->execute()) throw new Exception("Execute failed for orders: " . $stmt->error);
    $order_id = $stmt->insert_id;
    $stmt->close();

    // Insert each item with customizations
    $itemStmt = $conn->prepare("INSERT INTO order_items (oid, item_id, qty) VALUES (?, ?, ?)");
    if (!$itemStmt) throw new Exception("Prepare failed for order_items: " . $conn->error);
    
    foreach ($items as $item) {
        $item_id = intval($item['item_id']);
        $qty     = intval($item['qty']);
        if ($item_id && $qty > 0) {
            $itemStmt->bind_param("iii", $order_id, $item_id, $qty);
            if (!$itemStmt->execute()) {
                error_log("Execute failed for item: item_id=$item_id, qty=$qty — " . $itemStmt->error);
            } else {
                // 保存該項目的自訂選項
                saveItemCustomizations($conn, $order_id, $item_id, $item);
            }
        }
    }
    $itemStmt->close();

    // Staff-specific table_orders logic
    if ($sid !== null && $table_number !== null && $table_number !== 0) {
        $selectStmt = $conn->prepare("SELECT toid FROM table_orders WHERE table_number = ? LIMIT 1");
        $selectStmt->bind_param("i", $table_number);
        $selectStmt->execute();
        $selectStmt->bind_result($existing_toid);
        $hasExisting = $selectStmt->fetch();
        $selectStmt->close();

        if ($hasExisting && $existing_toid) {
            $updateStmt = $conn->prepare("UPDATE table_orders SET oid=?, staff_id=?, status='ordering', updated_at=CURRENT_TIMESTAMP WHERE toid=?");
            $updateStmt->bind_param("iii", $order_id, $sid, $existing_toid);
            $updateStmt->execute();
            $updateStmt->close();
        } else {
            $tableStmt = $conn->prepare("INSERT INTO table_orders (table_number, oid, staff_id, status) VALUES (?, ?, ?, 'ordering')");
            $tableStmt->bind_param("iii", $table_number, $order_id, $sid);
            $tableStmt->execute();
            $tableStmt->close();
        }
    }

    // Record coupon redemption and order_coupons if applicable
    if ($coupon_id) {
        $redStmt = $conn->prepare("INSERT INTO coupon_redemptions (coupon_id, cid, is_used, used_at) VALUES (?, ?, 1, NOW())");
        $redStmt->bind_param("ii", $coupon_id, $cid);
        $redStmt->execute();
        $redStmt->close();

        // Insert into order_coupons
        $discountApplied = null;
        if (isset($input['discount_applied'])) {
            $discountApplied = floatval($input['discount_applied']);
        } else {
            // Fallback: calculate from coupon definition
            $stmt = $conn->prepare("SELECT type, discount_amount FROM coupons WHERE coupon_id=?");
            $stmt->bind_param("i", $coupon_id);
            $stmt->execute();
            $row = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            if ($row) {
                if ($row['type'] === 'percent') {
                    $discountApplied = ($total_amount / 100.0) * ($row['discount_amount'] / 100.0);
                } elseif ($row['type'] === 'cash') {
                    $discountApplied = $row['discount_amount'] / 100.0;
                } else {
                    $discountApplied = 0.0; // free_item or other logic
                }
            }
        }

        $ocStmt = $conn->prepare("INSERT INTO order_coupons (oid, coupon_id, discount_amount) VALUES (?, ?, ?)");
        $ocStmt->bind_param("iid", $order_id, $coupon_id, $discountApplied);
        $ocStmt->execute();
        $ocStmt->close();
    }

    // Award points (1 HK$ = 1 point)
    $pointsAdded = 0;
    if ($cid !== 0 && $total_amount > 0) {
        $pointsToAdd = intdiv($total_amount, 100);
        if ($pointsToAdd > 0) {
            $sel = $conn->prepare("SELECT cp_id, points FROM coupon_point WHERE cid=? LIMIT 1");
            $sel->bind_param("i", $cid);
            $sel->execute();
            $sel->bind_result($cp_id_existing, $existing_points);
            $hasRow = $sel->fetch();
            $sel->close();

            if ($hasRow && $cp_id_existing) {
                $newPoints = $existing_points + $pointsToAdd;
                $upd = $conn->prepare("UPDATE coupon_point SET points=?, updated_at=CURRENT_TIMESTAMP WHERE cp_id=?");
                $upd->bind_param("ii", $newPoints, $cp_id_existing);
                $upd->execute();
                $upd->close();

                $hist = $conn->prepare("INSERT INTO coupon_point_history (cp_id, cid, coupon_id, delta, resulting_points, action, note) VALUES (?, ?, ?, ?, ?, 'earn', ?)");
                $note = "Order $order_id";
                $hist->bind_param("iiiiss", $cp_id_existing, $cid, $coupon_id, $pointsToAdd, $newPoints, $note);
                $hist->execute();
                $hist->close();
                $pointsAdded = $pointsToAdd;
            } else {
                $ins = $conn->prepare("INSERT INTO coupon_point (cid, points, last_changed_by, reason) VALUES (?, ?, ?, ?)");
                $reason = "Earned from order $order_id";
                $ins->bind_param("iiss", $cid, $pointsToAdd, $actor, $reason);
                $ins->execute();
                $newCpId = $ins->insert_id;
                $ins->close();

                $hist = $conn->prepare("INSERT INTO coupon_point_history (cp_id, cid, coupon_id, delta, resulting_points, action, note) VALUES (?, ?, ?, ?, ?, 'earn', ?)");
                $note = "Order $order_id";
                $hist->bind_param("iiiiss", $newCpId, $cid, $coupon_id, $pointsToAdd, $pointsToAdd, $note);
                $hist->execute();
                $hist->close();
                $pointsAdded = $pointsToAdd;
            }
        }
    }

    // Commit
    $conn->commit();

    $response = [
        "success"      => true,
        "order_id"     => $order_id,
        "points_added" => $pointsAdded
    ];
    echo json_encode($response);

} catch (Exception $e) {
    $conn->rollback();
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error"   => $e->getMessage()
    ]);
}

$conn->close();

/**
 * 保存菜品項目的自訂選項
 */
function saveItemCustomizations(&$conn, $order_id, $item_id, $itemData) {
    if (!isset($itemData['customizations']) || !is_array($itemData['customizations'])) {
        return; // 沒有自訂選項
    }

    $customizations = $itemData['customizations'];
    
    // 構建複合 ID (oid, item_id) - 但 SQL 只用 oid 和 item_id
    $stmt = $conn->prepare(
        "INSERT INTO order_item_customizations 
         (oid, oid_item_id, option_id, option_name, choice_ids, choice_names, text_value, additional_cost)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    );
    
    if (!$stmt) {
        error_log("Prepare failed for customizations: " . $conn->error);
        return;
    }

    foreach ($customizations as $custom) {
        $option_id = isset($custom['option_id']) ? intval($custom['option_id']) : 0;
        $option_name = isset($custom['option_name']) ? $custom['option_name'] : '';
        $choice_ids = isset($custom['choice_ids']) ? json_encode($custom['choice_ids']) : null;
        $choice_names = isset($custom['choice_names']) ? json_encode($custom['choice_names']) : null;
        $text_value = isset($custom['text_value']) ? $custom['text_value'] : null;
        $additional_cost = isset($custom['additional_cost']) ? floatval($custom['additional_cost']) : 0;

        $stmt->bind_param("iissssi", $order_id, $item_id, $option_id, $option_name, 
                         $choice_ids, $choice_names, $text_value, $additional_cost);
        
        if (!$stmt->execute()) {
            error_log("Execute failed for customization: item_id=$item_id, option_id=$option_id — " . $stmt->error);
        }
    }

    $stmt->close();
}
?>
