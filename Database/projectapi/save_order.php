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

// ✅ Extract order type and table number
$order_type = $input['order_type'] ?? 'dine_in'; // Default to dine_in
$table_number = $input['table_number'] ?? null;

// ✅ Extract payment method (card or cash)
$payment_method = $input['payment_method'] ?? 'card';
$payment_intent_id = $input['payment_intent_id'] ?? null;

// ✅ Validate ostatus based on payment method
// ostatus: 1=Pending, 2=Done (unpaid cash orders), 3=Paid (card orders), 4=Cancelled
if (empty($ostatus) || $ostatus < 1 || $ostatus > 4) {
    // Auto-determine ostatus based on payment method if not provided
    $ostatus = ("cash" === $payment_method) ? 2 : 3;
    error_log("Auto-determined ostatus=$ostatus based on payment_method=$payment_method");
}

// ✅ 確保 cid 是整數
$cid = intval($cid);
$ostatus = intval($ostatus);

if ($cid === null || $cid === 0 || empty($items)) {
    error_log("Missing required fields: cid=$cid, items count=" . count($items));
    echo json_encode(["error" => "Missing required fields"]);
    exit;
}

// Insert into orders table
$orderRef = $input['orderRef'] ?? 'order_' . time() . '_' . rand(1000, 9999);

$stmt = $conn->prepare("
    INSERT INTO orders (odate, cid, ostatus, orderRef, order_type, table_number)
    VALUES (?, ?, ?, ?, ?, ?)
");
if (!$stmt) {
    error_log("Prepare failed for orders: " . $conn->error);
    echo json_encode(["error" => "Failed to prepare order insert"]);
    exit;
}

$table_num_int = $table_number !== null ? intval($table_number) : null;

$stmt->bind_param("siissi", 
    $odate, $cid, $ostatus, $orderRef, 
    $order_type, 
    $table_num_int
);

if (!$stmt->execute()) {
    error_log("Execute failed for orders: odate=$odate, cid=$cid, ostatus=$ostatus, orderRef=$orderRef, order_type=$order_type, table_number=$table_num_int, error=" . $stmt->error);
    echo json_encode(["error" => "Failed to save order header", "details" => $stmt->error]);
    $stmt->close();
    $conn->close();
    exit;
}

$order_id = $stmt->insert_id;
error_log("✅ Order header saved with ID: $order_id (type: $order_type, table: $table_num_int, ostatus: $ostatus)");
$stmt->close();

// Insert each item into order_items (adjusted for your schema)
foreach ($items as $item) {
    $item_id = $item['item_id'] ?? null;
    $qty = $item['qty'] ?? null;
    $category = $item['category'] ?? null;
    $customizations = $item['customization'] ?? null;  // ✅ 新增：提取自訂信息

    error_log("==========================================");
    error_log("PROCESSING ITEM #" . ($item['item_id'] ?? 'UNKNOWN'));
    error_log("Processing item: item_id=$item_id, qty=$qty, category=$category");
    error_log("Customizations field exists: " . (isset($item['customization']) ? 'YES' : 'NO'));
    
    if ($customizations) {
        error_log("Customization object structure: " . json_encode($customizations));
        error_log("  Keys in customizations: " . implode(', ', array_keys($customizations)));
        if (!empty($customizations['customization_details'])) {
            error_log("  Found " . count($customizations['customization_details']) . " details in customization_details");
        } else {
            error_log("  ❌ NO customization_details key found");
        }
    } else {
        error_log("  Customizations object is NULL or empty");
    }

    // Check if this is a package marker (category = "PACKAGE")
    if (!empty($category) && strtoupper($category) === "PACKAGE") {
        // This is a package - handle it separately
        $package_id = $item_id;  // item_id is actually package_id for packages
        $package_name = $item['name'] ?? 'Package';
        
        error_log("Processing package: package_id=$package_id, name=$package_name");
        
        // Insert into order_packages table
        $packageStmt = $conn->prepare("
            INSERT INTO order_packages (oid, package_id, qty, note)
            VALUES (?, ?, ?, ?)
        ");
        
        if (!$packageStmt) {
            error_log("Prepare failed for package: " . $conn->error);
            continue;
        }
        
        $note = $item['note'] ?? '';
        $packageStmt->bind_param("iiis", $order_id, $package_id, $qty, $note);
        
        if (!$packageStmt->execute()) {
            error_log("Execute failed for package: package_id=$package_id, qty=$qty — " . $packageStmt->error);
            $packageStmt->close();
            continue;
        }
        
        $order_package_id = $packageStmt->insert_id;
        error_log("Package saved: order_id=$order_id, package_id=$package_id, qty=$qty, order_package_id=$order_package_id");
        $packageStmt->close();
        
        // ✅ 保存用戶實際選擇的套餐菜品到 order_items（來自 packageItems 陣列）
        if (!empty($item['packageItems']) && is_array($item['packageItems'])) {
            error_log("Saving " . count($item['packageItems']) . " items for package");
            foreach ($item['packageItems'] as $packageItem) {
                $pkg_item_id = $packageItem['id'] ?? null;
                $pkg_item_qty = $packageItem['qty'] ?? 1;
                
                if (!$pkg_item_id) {
                    continue;
                }
                
                // ✅ Prepare customization note for package item with group_name (v4.6)
                $pkg_item_note = '';
                if (!empty($packageItem['customizations']) && is_array($packageItem['customizations'])) {
                    error_log("Processing customizations for package item: item_id=$pkg_item_id");
                    
                    $pkg_note_customizations = [];
                    foreach ($packageItem['customizations'] as $custom) {
                        $group_id = intval($custom['group_id'] ?? 0);
                        
                        // Fetch group_name from database
                        $group_name = '';
                        if ($group_id > 0) {
                            $groupStmt = $conn->prepare("SELECT group_name FROM customization_option_group WHERE group_id = ?");
                            if ($groupStmt) {
                                $groupStmt->bind_param("i", $group_id);
                                $groupStmt->execute();
                                $groupStmt->bind_result($group_name);
                                $groupStmt->fetch();
                                $groupStmt->close();
                            }
                        }
                        
                        // Build customization with group_name
                        $pkg_note_customizations[] = [
                            'option_id' => intval($custom['option_id'] ?? 0),
                            'group_id' => $group_id,
                            'group_name' => $group_name,
                            'selected_value_ids' => $custom['selected_value_ids'] ?? [],
                            'selected_values' => $custom['selected_values'] ?? [],
                            'text_value' => $custom['text_value'] ?? ''
                        ];
                    }
                    
                    $pkg_item_note = json_encode($pkg_note_customizations);
                    error_log("Package item customizations JSON with group_name: " . $pkg_item_note);
                }
                
                // Insert package item into order_items with customizations in note column
                $pkgItemStmt = $conn->prepare("
                    INSERT INTO order_items (oid, item_id, qty, note)
                    VALUES (?, ?, ?, ?)
                ");
                
                if (!$pkgItemStmt) {
                    error_log("Prepare failed for package item: " . $conn->error);
                    continue;
                }
                
                $pkgItemStmt->bind_param("iiis", $order_id, $pkg_item_id, $pkg_item_qty, $pkg_item_note);
                
                if (!$pkgItemStmt->execute()) {
                    error_log("Execute failed for package item: item_id=$pkg_item_id, error=" . $pkgItemStmt->error);
                } else {
                    error_log("Package item saved with customizations: order_id=$order_id, item_id=$pkg_item_id, qty=$pkg_item_qty, note_length=" . strlen($pkg_item_note));
                }
                
                $pkgItemStmt->close();
            }
        }
        
        continue;
    }

    if (!$item_id || !$qty) {
        error_log("Skipping invalid item: " . json_encode($item));
        continue;
    }

    // ✅ Build customization note for individual items (v4.6)
    $item_note = '';
    $note_customizations = [];
    
    if ($customizations && is_array($customizations)) {
        error_log("📝 Processing customizations for item_id=$item_id");
        error_log("   Customizations keys: " . implode(', ', array_keys($customizations)));
        
        if (!empty($customizations['customization_details'])) {
            error_log("   ✅ Found " . count($customizations['customization_details']) . " customization details");
            
            foreach ($customizations['customization_details'] as $idx => $custom) {
                $option_id = intval($custom['option_id'] ?? 0);
                $group_id = intval($custom['group_id'] ?? 0);
                $text_value = $custom['text_value'] ?? '';
                
                // Fetch group_name from database
                $group_name = '';
                if ($group_id > 0) {
                    $groupStmt = $conn->prepare("SELECT group_name FROM customization_option_group WHERE group_id = ?");
                    if ($groupStmt) {
                        $groupStmt->bind_param("i", $group_id);
                        $groupStmt->execute();
                        $groupStmt->bind_result($group_name);
                        $groupStmt->fetch();
                        $groupStmt->close();
                        error_log("      Fetched group_name='$group_name' for group_id=$group_id");
                    }
                }
                
                // Extract selected values
                $selected_value_ids = [];
                $selected_values = [];
                
                if (isset($custom['selected_value_ids']) && !empty($custom['selected_value_ids'])) {
                    if (is_array($custom['selected_value_ids'])) {
                        $selected_value_ids = array_map('intval', $custom['selected_value_ids']);
                    } else {
                        $selected_value_ids = [intval($custom['selected_value_ids'])];
                    }
                }
                
                if (isset($custom['selected_values']) && !empty($custom['selected_values'])) {
                    if (is_array($custom['selected_values'])) {
                        $selected_values = $custom['selected_values'];
                    } else {
                        $selected_values = [$custom['selected_values']];
                    }
                } elseif (isset($custom['selected_choices']) && is_array($custom['selected_choices'])) {
                    // Legacy support for selected_choices
                    $selected_values = $custom['selected_choices'];
                }
                
                // Build customization object for note column with group_name
                if (!empty($selected_value_ids) || !empty($selected_values) || !empty($text_value)) {
                    $note_customizations[] = [
                        'option_id' => $option_id,
                        'group_id' => $group_id,
                        'group_name' => $group_name,
                        'selected_value_ids' => $selected_value_ids,
                        'selected_values' => $selected_values,
                        'text_value' => $text_value
                    ];
                }
            }
        }
        
        // Add extra_notes if present
        if (!empty($customizations['extra_notes'])) {
            error_log("   Found extra_notes: " . $customizations['extra_notes']);
        }
    }
    
    // Convert customizations to JSON for note column
    if (!empty($note_customizations)) {
        $item_note = json_encode($note_customizations);
        error_log("Built customization note for item_id=$item_id: " . $item_note);
    }

    $itemStmt = $conn->prepare("
        INSERT INTO order_items (oid, item_id, qty, note)
        VALUES (?, ?, ?, ?)
    ");
    if (!$itemStmt) {
        error_log("Prepare failed for item: " . $conn->error);
        continue;
    }

    $itemStmt->bind_param("iiis", $order_id, $item_id, $qty, $item_note);

    if (!$itemStmt->execute()) {
        error_log("Execute failed for item: item_id=$item_id, qty=$qty — " . $itemStmt->error);
        $itemStmt->close();
        continue;
    }

    $oiid = $itemStmt->insert_id;
    error_log("✅ Item saved: order_id=$order_id, item_id=$item_id, qty=$qty, note_length=" . strlen($item_note));
    $itemStmt->close();

    // ✅ Also save to order_item_customizations table for backward compatibility
    if ($customizations && is_array($customizations) && !empty($customizations['customization_details'])) {
        foreach ($customizations['customization_details'] as $idx => $custom) {
            $option_id = intval($custom['option_id'] ?? 0);
            $group_id = intval($custom['group_id'] ?? 0);
            $text_value = $custom['text_value'] ?? '';
            
            $selected_value_ids = null;
            $selected_values = null;
            
            if (isset($custom['selected_value_ids']) && !empty($custom['selected_value_ids'])) {
                if (is_array($custom['selected_value_ids'])) {
                    $selected_value_ids = json_encode(array_map('intval', $custom['selected_value_ids']));
                } else {
                    $selected_value_ids = json_encode([intval($custom['selected_value_ids'])]);
                }
            }
            
            if (isset($custom['selected_values']) && !empty($custom['selected_values'])) {
                if (is_array($custom['selected_values'])) {
                    $selected_values = json_encode($custom['selected_values']);
                } else {
                    $selected_values = json_encode([$custom['selected_values']]);
                }
            } elseif (isset($custom['selected_choices']) && is_array($custom['selected_choices']) && !empty($custom['selected_choices'])) {
                $selected_values = json_encode($custom['selected_choices']);
            }

            if ($selected_value_ids !== null || $selected_values !== null || !empty($text_value)) {
                $customStmt = $conn->prepare("
                    INSERT INTO order_item_customizations 
                    (oid, item_id, option_id, group_id, selected_value_ids, selected_values, text_value)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                ");

                if ($customStmt) {
                    $customStmt->bind_param("iiiisss", 
                        $order_id, $item_id, $option_id, $group_id,
                        $selected_value_ids, $selected_values, $text_value
                    );

                    if ($customStmt->execute()) {
                        error_log("      ✅ Customization SAVED to order_item_customizations: item=$item_id, group=$group_id");
                    } else {
                        error_log("      ❌ Execute failed for customization: " . $customStmt->error);
                    }

                    $customStmt->close();
                }
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

// ✅ Handle coupon points for card payments (ostatus = 3)
if ($ostatus == 3) {
    // Calculate total order amount for coupon points
    $amountStmt = $conn->prepare("
        SELECT COALESCE(SUM(mi.item_price * oi.qty), 0) as total
        FROM order_items oi
        JOIN menu_item mi ON oi.item_id = mi.item_id
        WHERE oi.oid = ?
    ");
    
    if ($amountStmt) {
        $amountStmt->bind_param("i", $order_id);
        $amountStmt->execute();
        $amountResult = $amountStmt->get_result();
        $amountRow = $amountResult->fetch_assoc();
        $totalAmount = intval($amountRow['total']);
        $amountStmt->close();
        
        // Calculate coupon points: HK$1 = 1 point
        $couponPointsToAdd = $totalAmount;
        
        if ($couponPointsToAdd > 0) {
            error_log("Card payment detected. Adding $couponPointsToAdd coupon points to cid=$cid for order_id=$order_id");
            
            // Start transaction for coupon point update
            $conn->begin_transaction();
            
            try {
                // Add coupon points to customer
                $pointsStmt = $conn->prepare("UPDATE customer SET coupon_point = coupon_point + ? WHERE cid = ?");
                if (!$pointsStmt) {
                    throw new Exception("Failed to prepare points update: " . $conn->error);
                }
                
                $pointsStmt->bind_param("ii", $couponPointsToAdd, $cid);
                if (!$pointsStmt->execute()) {
                    throw new Exception("Failed to add coupon points: " . $pointsStmt->error);
                }
                $pointsStmt->close();
                
                // Get the new coupon point balance
                $balanceStmt = $conn->prepare("SELECT coupon_point FROM customer WHERE cid = ?");
                if (!$balanceStmt) {
                    throw new Exception("Failed to prepare balance query: " . $conn->error);
                }
                
                $balanceStmt->bind_param("i", $cid);
                $balanceStmt->execute();
                $balanceResult = $balanceStmt->get_result();
                $balanceRow = $balanceResult->fetch_assoc();
                $newBalance = $balanceRow['coupon_point'];
                $balanceStmt->close();
                
                // Insert into coupon_point_history for tracking
                $note = "Payment for order #" . $order_id . " (Amount: HK$" . $totalAmount . ")";
                $historyStmt = $conn->prepare("INSERT INTO coupon_point_history (cid, coupon_id, delta, resulting_points, action, note) VALUES (?, NULL, ?, ?, 'earn', ?)");
                if (!$historyStmt) {
                    throw new Exception("Failed to prepare history insert: " . $conn->error);
                }
                
                $historyStmt->bind_param("iiis", $cid, $couponPointsToAdd, $newBalance, $note);
                if (!$historyStmt->execute()) {
                    throw new Exception("Failed to insert history: " . $historyStmt->error);
                }
                $historyStmt->close();
                
                // Commit transaction
                $conn->commit();
                error_log("✅ Coupon points added successfully: cid=$cid, points=$couponPointsToAdd, new_balance=$newBalance");
                
            } catch (Exception $e) {
                $conn->rollback();
                error_log("❌ Error adding coupon points: " . $e->getMessage());
            }
        }
    }
} else if ($ostatus == 2) {
    // Cash payment - no coupon points added yet
    error_log("Cash payment detected (ostatus=2). Coupon points will be added after staff confirms payment.");
}

// Final response
$response = ["success" => true, "order_id" => $order_id];
if ($table_order_id !== null) {
    $response["table_order_id"] = $table_order_id;
}

// ✅ Debug: Count packages and items in this order
$pkgCount = $conn->query("SELECT COUNT(*) as cnt FROM order_packages WHERE oid = $order_id")->fetch_assoc()['cnt'];
$itemCount = $conn->query("SELECT COUNT(*) as cnt FROM order_items WHERE oid = $order_id")->fetch_assoc()['cnt'];
$response["debug"] = ["packages_count" => $pkgCount, "items_count" => $itemCount];
error_log("Order $order_id saved: packages=$pkgCount, items=$itemCount");

echo json_encode($response);
$conn->close();
?>