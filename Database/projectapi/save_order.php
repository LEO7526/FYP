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

// ✅ 確保 cid 是整數
$cid = intval($cid);
$ostatus = intval($ostatus);

if ($cid === null || $cid === 0 || empty($items)) {
    error_log("Missing required fields: cid=$cid, items count=" . count($items));
    echo json_encode(["error" => "Missing required fields"]);
    exit;
}

// Insert into orders table (removed ocost and odeliverdate as they don't exist in your schema)
$orderRef = $input['orderRef'] ?? 'order_' . time() . '_' . rand(1000, 9999);

$stmt = $conn->prepare("
    INSERT INTO orders (odate, cid, ostatus, orderRef)
    VALUES (?, ?, ?, ?)
");
if (!$stmt) {
    error_log("Prepare failed for orders: " . $conn->error);
    echo json_encode(["error" => "Failed to prepare order insert"]);
    exit;
}

$stmt->bind_param("siis", $odate, $cid, $ostatus, $orderRef);

if (!$stmt->execute()) {
    error_log("Execute failed for orders: odate=$odate, cid=$cid, ostatus=$ostatus, orderRef=$orderRef, error=" . $stmt->error);
    echo json_encode(["error" => "Failed to save order header", "details" => $stmt->error]);
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
                
                // Insert package item into order_items
                $pkgItemStmt = $conn->prepare("
                    INSERT INTO order_items (oid, item_id, qty)
                    VALUES (?, ?, ?)
                ");
                
                if (!$pkgItemStmt) {
                    error_log("Prepare failed for package item: " . $conn->error);
                    continue;
                }
                
                $pkgItemStmt->bind_param("iii", $order_id, $pkg_item_id, $pkg_item_qty);
                
                if (!$pkgItemStmt->execute()) {
                    error_log("Execute failed for package item: item_id=$pkg_item_id, error=" . $pkgItemStmt->error);
                } else {
                    error_log("Package item saved: order_id=$order_id, item_id=$pkg_item_id, qty=$pkg_item_qty");
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

    // ✅ Extract extra_notes if present in customizations
    $item_note = '';
    if ($customizations && is_array($customizations) && !empty($customizations['extra_notes'])) {
        $item_note = $customizations['extra_notes'];
        error_log("Found extra_notes for item: $item_note");
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

    $oiid = $itemStmt->insert_id;  // ✅ 新增：獲取order_item_id
    error_log("✅ Item saved: order_id=$order_id, item_id=$item_id, qty=$qty, oiid=$oiid");
    $itemStmt->close();

    // ✅ 新增：保存自訂詳情到order_item_customizations
    if ($customizations && is_array($customizations)) {
        error_log("📝 Processing customizations for item_id=$item_id, oiid=$oiid");
        error_log("   Customizations keys: " . implode(', ', array_keys($customizations)));
        
        if (!empty($customizations['customization_details'])) {
            error_log("   ✅ Found " . count($customizations['customization_details']) . " customization details");
            foreach ($customizations['customization_details'] as $idx => $custom) {
                error_log("   Processing detail #$idx: " . json_encode($custom));
                error_log("      Detail type: " . gettype($custom) . ", Keys: " . implode(',', array_keys((array)$custom)));
                error_log("      ⚠️ Full detail object dump: " . var_export($custom, true));
                
                // 🔴 CRITICAL: Log all available fields
                if (is_array($custom)) {
                    foreach ($custom as $key => $val) {
                        error_log("         [$key] = " . json_encode($val) . " (type: " . gettype($val) . ")");
                    }
                }
                
                // ✅ v4.5: 從custom物件提取group_id和selected_value_ids
                $option_id = intval($custom['option_id'] ?? 0);
                $group_id = intval($custom['group_id'] ?? 0);
                $text_value = $custom['text_value'] ?? '';
                
                // ✅ v4.5: 使用selected_value_ids（整數陣列）替代selected_choices（字符串）
                $selected_value_ids = null;
                $selected_values = null;
                
                error_log("      Checking for selected_value_ids...");
                if (isset($custom['selected_value_ids']) && !empty($custom['selected_value_ids'])) {
                    error_log("      Found selected_value_ids: " . json_encode($custom['selected_value_ids']));
                    // 如果已經是陣列，轉換為JSON
                    if (is_array($custom['selected_value_ids'])) {
                        $selected_value_ids = json_encode(array_map('intval', $custom['selected_value_ids']));
                        error_log("      ✅ Converted selected_value_ids array to JSON: $selected_value_ids");
                    } else {
                        // 如果是字符串，嘗試解析
                        $selected_value_ids = json_encode([intval($custom['selected_value_ids'])]);
                        error_log("      ✅ Using selected_value_ids as JSON: $selected_value_ids");
                    }
                }
                
                // ✅ 兼容舊版本：如果使用selected_choices，轉換為對應的value_ids
                error_log("      Checking for selected_choices... (is set: " . (isset($custom['selected_choices']) ? 'YES' : 'NO') . ")");
                if ($selected_value_ids === null && isset($custom['selected_choices'])) {
                    error_log("      Found selected_choices: " . json_encode($custom['selected_choices']));
                    if (is_array($custom['selected_choices']) && !empty($custom['selected_choices'])) {
                        // ⚠️ 只保存到selected_values用於顯示，不能保存到selected_value_ids（應該是數字ID）
                        $selected_values = json_encode($custom['selected_choices']);
                        error_log("      ⚠️ Using selected_choices (legacy) → selected_values: $selected_values");
                    }
                }
                
                // selected_values (用於顯示) - 如果還沒有設置
                error_log("      Checking for selected_values...");
                if ($selected_values === null && isset($custom['selected_values']) && !empty($custom['selected_values'])) {
                    if (is_array($custom['selected_values'])) {
                        $selected_values = json_encode($custom['selected_values']);
                    } else {
                        $selected_values = json_encode([$custom['selected_values']]);
                    }
                    error_log("      ✅ Using selected_values: $selected_values");
                }

                error_log("      Final values: option_id=$option_id, group_id=$group_id, value_ids=" . ($selected_value_ids ?? 'NULL') . ", values=" . ($selected_values ?? 'NULL') . ", text=$text_value");
                error_log("      Condition check: is_null(value_ids)=" . ($selected_value_ids === null ? 'YES' : 'NO') . ", is_null(values)=" . ($selected_values === null ? 'YES' : 'NO') . ", empty(text)=" . (empty($text_value) ? 'YES' : 'NO'));

                // 只有當有選擇或文本時才保存
                if ($selected_value_ids !== null || $selected_values !== null || !empty($text_value)) {
                    // ✅ v4.5: 使用新的schema - group_id和selected_value_ids取代choice_ids/choice_names
                    $customStmt = $conn->prepare("
                        INSERT INTO order_item_customizations 
                        (oid, item_id, option_id, group_id, selected_value_ids, selected_values, text_value)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                    ");

                    if (!$customStmt) {
                        error_log("      ❌ Prepare failed for customization: " . $conn->error);
                        continue;
                    }

                    $customStmt->bind_param("iiiisss", 
                        $order_id, $item_id, $option_id, $group_id,
                        $selected_value_ids, $selected_values, $text_value
                    );

                    if ($customStmt->execute()) {
                        error_log("      ✅ Customization SAVED (v4.5): item=$item_id, group=$group_id, values=$selected_values");
                    } else {
                        error_log("      ❌ Execute failed for customization: " . $customStmt->error);
                    }

                    $customStmt->close();
                } else {
                    error_log("      ⚠️ Skipped: no values or text");
                }
            }
        } else {
            error_log("   ❌ NO customization_details found in customizations object");
            error_log("   Available keys: " . json_encode(array_keys($customizations)));
        }
    } else {
        error_log("No customizations for item_id=$item_id");
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

// ✅ Debug: Count packages and items in this order
$pkgCount = $conn->query("SELECT COUNT(*) as cnt FROM order_packages WHERE oid = $order_id")->fetch_assoc()['cnt'];
$itemCount = $conn->query("SELECT COUNT(*) as cnt FROM order_items WHERE oid = $order_id")->fetch_assoc()['cnt'];
$response["debug"] = ["packages_count" => $pkgCount, "items_count" => $itemCount];
error_log("Order $order_id saved: packages=$pkgCount, items=$itemCount");

echo json_encode($response);
$conn->close();
?>