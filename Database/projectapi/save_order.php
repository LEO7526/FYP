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
                error_log("      Detail type: " . gettype($custom) . ", Keys: " . implode(',', array_keys($custom)));
                
                $option_id = intval($custom['option_id'] ?? 0);
                $option_name = $custom['option_name'] ?? '';
                $choice_names = '';
                $text_value = $custom['text_value'] ?? '';
                $additional_cost = floatval($custom['additional_cost'] ?? 0);

                // 🔴 DEBUG: 檢查 selected_choices 字段
                error_log("      selected_choices exists: " . (isset($custom['selected_choices']) ? 'YES' : 'NO'));
                error_log("      selected_choices value: " . (isset($custom['selected_choices']) ? json_encode($custom['selected_choices']) : 'NULL'));
                error_log("      selected_choices type: " . (isset($custom['selected_choices']) ? gettype($custom['selected_choices']) : 'undefined'));

                // 處理selectedChoices陣列 - 🔴 改進：更詳細的檢查
                if (isset($custom['selected_choices'])) {
                    if (is_array($custom['selected_choices']) && !empty($custom['selected_choices'])) {
                        // ✅ 轉換為 JSON 陣列格式（資料庫需要 JSON）
                        $choice_names = json_encode($custom['selected_choices']);
                        error_log("      ✅ Converted selected_choices array to JSON: $choice_names");
                    } elseif (is_string($custom['selected_choices']) && !empty($custom['selected_choices'])) {
                        // ✅ 若已是字符串，包裝為 JSON 陣列
                        $choice_names = json_encode([$custom['selected_choices']]);
                        error_log("      ✅ Using selected_choices string (wrapped as JSON): $choice_names");
                    } else {
                        error_log("      ⚠️ selected_choices exists but is empty or invalid type");
                    }
                }
                
                // 也嘗試 choice_names（以防萬一）
                if (empty($choice_names) && !empty($custom['choice_names'])) {
                    $choice_names = json_encode(is_array($custom['choice_names']) ? $custom['choice_names'] : [$custom['choice_names']]);
                    error_log("      Using choice_names directly (as JSON): $choice_names");
                }

                error_log("      Final values: option_id=$option_id, name=$option_name, choices=$choice_names, text=$text_value, cost=$additional_cost");

                // 只有當有選擇或文本時才保存
                if (!empty($choice_names) || !empty($text_value) || $additional_cost > 0) {
                    $customStmt = $conn->prepare("
                        INSERT INTO order_item_customizations 
                        (oid, item_id, option_id, option_name, choice_names, text_value, additional_cost)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                    ");

                    if (!$customStmt) {
                        error_log("      ❌ Prepare failed for customization: " . $conn->error);
                        continue;
                    }

                    $customStmt->bind_param("iiissdi", 
                        $order_id, $item_id, $option_id, 
                        $option_name, $choice_names, $text_value, $additional_cost
                    );

                    if ($customStmt->execute()) {
                        error_log("      ✅ Customization SAVED: item=$item_id, option=$option_name, choices=$choice_names, cost=$additional_cost");
                    } else {
                        error_log("      ❌ Execute failed for customization: " . $customStmt->error);
                    }

                    $customStmt->close();
                } else {
                    error_log("      ⚠️ Skipped: no choices, text, or cost");
                }
            }
        } else {
            error_log("   ❌ NO customization_details found in customizations object");
            error_log("   Available keys: " . json_encode(array_keys($customizations)));
        }

        // ✅ 新增：保存特殊要求
        if (!empty($customizations['extra_notes'])) {
            $extra_notes = $customizations['extra_notes'];
            error_log("   📝 Processing extra_notes: $extra_notes");
            
            $notesStmt = $conn->prepare("
                INSERT INTO order_item_customizations 
                (oid, item_id, oiid, option_id, option_name, text_value)
                VALUES (?, ?, ?, ?, ?, ?)
            ");

            if (!$notesStmt) {
                error_log("      ❌ Prepare failed for notes: " . $conn->error);
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
                    error_log("✅ Special instructions SAVED: item=$item_id, notes=$extra_notes");
                }

                $notesStmt->close();
            }
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