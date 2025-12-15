<?php
header('Content-Type: application/json');

// Connect to MySQL
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    error_log("DB Connection failed: " . $conn->connect_error);
    echo json_encode(["error" => "Connection failed"]);
    exit;
}

// Read JSON input
$input = json_decode(file_get_contents("php://input"), true);
error_log("Received input: " . json_encode($input));

// Extract and validate fields
$cid = $input['cid'] ?? null;
$ostatus = $input['ostatus'] ?? 0;
$items = $input['items'] ?? [];
$odate = date('Y-m-d H:i:s'); // Current timestamp

if ($cid === null || empty($items)) {
    error_log("Missing required fields: cid=$cid, items=" . json_encode($items));
    echo json_encode(["error" => "Missing required fields"]);
    exit;
}

// Insert into orders table (removed ocost and odeliverdate as they don't exist in your schema)
$stmt = $conn->prepare("
    INSERT INTO orders (odate, cid, ostatus)
    VALUES (?, ?, ?)
");
if (!$stmt) {
    error_log("Prepare failed for orders: " . $conn->error);
    echo json_encode(["error" => "Failed to prepare order insert"]);
    exit;
}

$stmt->bind_param("sii", $odate, $cid, $ostatus);

if (!$stmt->execute()) {
    error_log("Execute failed for orders: " . $stmt->error);
    echo json_encode(["error" => "Failed to save order header"]);
    $stmt->close();
    $conn->close();
    exit;
}

$order_id = $stmt->insert_id;
error_log("Order header saved with ID: $order_id");
$stmt->close();

// Insert each item into order_items (adjusted for your schema)
foreach ($items as $item) {
    $item_id = $item['item_id'] ?? null;
    $qty = $item['qty'] ?? null;
    $customizations = $item['customization'] ?? null;  // ✅ 新增：提取自訂信息

    if (!$item_id || !$qty) {
        error_log("Skipping invalid item: " . json_encode($item));
        continue;
    }

    $itemStmt = $conn->prepare("
        INSERT INTO order_items (oid, item_id, qty)
        VALUES (?, ?, ?)
    ");
    if (!$itemStmt) {
        error_log("Prepare failed for item: " . $conn->error);
        continue;
    }

    $itemStmt->bind_param("iii", $order_id, $item_id, $qty);

    if (!$itemStmt->execute()) {
        error_log("Execute failed for item: item_id=$item_id, qty=$qty — " . $itemStmt->error);
        $itemStmt->close();
        continue;
    }

    $oiid = $itemStmt->insert_id;  // ✅ 新增：獲取order_item_id
    error_log("Item saved: order_id=$order_id, item_id=$item_id, qty=$qty, oiid=$oiid");
    $itemStmt->close();

    // ✅ 新增：保存自訂詳情到order_item_customizations
    if ($customizations && is_array($customizations)) {
        if (!empty($customizations['customization_details'])) {
            foreach ($customizations['customization_details'] as $custom) {
                $option_id = intval($custom['option_id'] ?? 0);
                $option_name = $custom['option_name'] ?? '';
                $choice_names = '';
                $text_value = $custom['text_value'] ?? '';
                $additional_cost = floatval($custom['additional_cost'] ?? 0);

                // 處理selectedChoices陣列
                if (!empty($custom['selected_choices']) && is_array($custom['selected_choices'])) {
                    $choice_names = implode(',', $custom['selected_choices']);
                }

                // 只有當有選擇或文本時才保存
                if (!empty($choice_names) || !empty($text_value) || $additional_cost > 0) {
                    $customStmt = $conn->prepare("
                        INSERT INTO order_item_customizations 
                        (oid, item_id, oiid, option_id, option_name, choice_names, text_value, additional_cost)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ");

                    if (!$customStmt) {
                        error_log("Prepare failed for customization: " . $conn->error);
                        continue;
                    }

                    $customStmt->bind_param("iiiissd", 
                        $order_id, $item_id, $oiid, $option_id, 
                        $option_name, $choice_names, $text_value, $additional_cost
                    );

                    if (!$customStmt->execute()) {
                        error_log("Execute failed for customization: " . $customStmt->error);
                    } else {
                        error_log("Customization saved: item=$item_id, option=$option_name, choices=$choice_names, cost=$additional_cost");
                    }

                    $customStmt->close();
                }
            }
        }

        // ✅ 新增：保存特殊要求
        if (!empty($customizations['extra_notes'])) {
            $extra_notes = $customizations['extra_notes'];
            $notesStmt = $conn->prepare("
                INSERT INTO order_item_customizations 
                (oid, item_id, oiid, option_id, option_name, text_value)
                VALUES (?, ?, ?, ?, ?, ?)
            ");

            if (!$notesStmt) {
                error_log("Prepare failed for notes: " . $conn->error);
            } else {
                $notes_option_id = 999;  // 特殊要求使用特殊ID
                $notes_option_name = "Special Instructions";

                $notesStmt->bind_param("iiiiss", 
                    $order_id, $item_id, $oiid, $notes_option_id, 
                    $notes_option_name, $extra_notes
                );

                if (!$notesStmt->execute()) {
                    error_log("Execute failed for notes: " . $notesStmt->error);
                } else {
                    error_log("Special instructions saved: item=$item_id, notes=$extra_notes");
                }

                $notesStmt->close();
            }
        }
    }
}

// Staff-specific logic for table orders
$sid = $input['sid'] ?? null;
$table_number = $input['table_number'] ?? null;
$table_order_id = null;

if ($sid && $table_number) {
    $tableStmt = $conn->prepare("
        INSERT INTO table_orders (table_number, oid, staff_id, status)
        VALUES (?, ?, ?, 'ordering')
    ");
    if (!$tableStmt) {
        error_log("Prepare failed for table_orders: " . $conn->error);
        echo json_encode(["error" => "Failed to prepare table order insert"]);
        $conn->close();
        exit;
    }

    $tableStmt->bind_param("iii", $table_number, $order_id, $sid);

    if (!$tableStmt->execute()) {
        error_log("Execute failed for table_orders: " . $tableStmt->error);
        echo json_encode(["error" => "Failed to save table order"]);
        $tableStmt->close();
        $conn->close();
        exit;
    }

    $table_order_id = $tableStmt->insert_id;
    error_log("Table order saved with ID: $table_order_id");
    $tableStmt->close();
}

// Final response
$response = ["success" => true, "order_id" => $order_id];
if ($table_order_id !== null) {
    $response["table_order_id"] = $table_order_id;
}
echo json_encode($response);
$conn->close();
?>