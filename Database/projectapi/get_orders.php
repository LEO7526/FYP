<?php
header('Content-Type: application/json');

$host = 'localhost';
$user = 'root';
$pass = '';
$dbname = 'ProjectDB';

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(['error' => $conn->connect_error]);
    exit;
}

$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
$language = $_GET['lang'] ?? 'en'; // default to English

// ✅ FIX: Include ALL orders (including unpaid cash orders with ostatus=2)
// Removed any status filters - display all orders regardless of payment status
$sql = "
    SELECT 
        o.oid,
        o.odate,
        o.ostatus,
        c.cname,
        t.table_number,
        s.sname AS staff_name
    FROM orders o
    LEFT JOIN customer c ON o.cid = c.cid
    LEFT JOIN table_orders t ON o.oid = t.oid
    LEFT JOIN staff s ON t.staff_id = s.sid
    WHERE o.cid = ? AND o.ostatus != 4
    ORDER BY o.odate DESC
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result();

$orders = [];

while ($row = $result->fetch_assoc()) {
    $order = $row;
    $oid = $order['oid'];

    // Fetch items for this order
    $itemSql = "
        SELECT 
            oi.item_id,
            mi.item_price,
            mit.item_name,
            mi.image_url,
            oi.qty AS quantity
        FROM order_items oi
        JOIN menu_item_translation mit ON oi.item_id = mit.item_id
        JOIN menu_item mi ON mit.item_id = mi.item_id
        WHERE mit.language_code = ? AND oi.oid = ?
    ";

    $itemStmt = $conn->prepare($itemSql);
    $itemStmt->bind_param("si", $language, $oid);
    $itemStmt->execute();
    $itemResult = $itemStmt->get_result();

    $items = [];
    while ($itemRow = $itemResult->fetch_assoc()) {
        $item_id = (int)$itemRow['item_id'];
        $itemPrice = (float)$itemRow['item_price'];
        $quantity = (int)$itemRow['quantity'];
        $imagUrl = $itemRow['image_url'];
        
        // Check if this item_id corresponds to a package
        $package_check_sql = "SELECT package_id FROM menu_package WHERE package_id = ?";
        $package_stmt = $conn->prepare($package_check_sql);
        $package_stmt->bind_param("i", $item_id);
        $package_stmt->execute();
        $package_result = $package_stmt->get_result();
        
        if ($package_result->num_rows > 0) {
            // This is a package - first add the package itself
            // Get package name
            $package_name_sql = "SELECT package_name FROM menu_package WHERE package_id = ?";
            $package_name_stmt = $conn->prepare($package_name_sql);
            $package_name_stmt->bind_param("i", $item_id);
            $package_name_stmt->execute();
            $package_name_result = $package_name_stmt->get_result();
            $package_name = "Package #" . $item_id;
            if ($package_name_result->num_rows > 0) {
                $package_row = $package_name_result->fetch_assoc();
                $package_name = $package_row['package_name'];
            }
            $package_name_stmt->close();
            
            // Get all items in the package for expansion
            $package_items_sql = "
                SELECT 
                    pd.item_id,
                    mi.item_price,
                    mit.item_name,
                    mi.image_url,
                    pd.price_modifier
                FROM package_dish pd
                JOIN menu_item_translation mit ON pd.item_id = mit.item_id
                JOIN menu_item mi ON pd.item_id = mi.item_id
                WHERE pd.package_id = ? AND mit.language_code = ?
                ORDER BY pd.item_id
            ";
            
            $package_items_stmt = $conn->prepare($package_items_sql);
            $package_items_stmt->bind_param("is", $item_id, $language);
            $package_items_stmt->execute();
            $package_items_result = $package_items_stmt->get_result();
            
            $package_items = [];
            while ($package_item = $package_items_result->fetch_assoc()) {
                $package_item_id = (int)$package_item['item_id'];
                $package_item_price = (float)$package_item['item_price'];
                
                // Fetch customizations for this package item (if any)
                $customSql = "
                    SELECT 
                        option_id,
                        group_id,
                        selected_value_ids,
                        selected_values,
                        text_value
                    FROM order_item_customizations
                    WHERE oid = ? AND item_id = ?
                ";
                
                $customStmt = $conn->prepare($customSql);
                $customStmt->bind_param("ii", $oid, $package_item_id);
                $customStmt->execute();
                $customResult = $customStmt->get_result();
                
                $customizations = [];
                while ($customRow = $customResult->fetch_assoc()) {
                    // Process customizations (same logic as before)
                    $valueIds = json_decode($customRow['selected_value_ids'] ?? '[]', true);
                    $selectedValues = json_decode($customRow['selected_values'] ?? '[]', true);
                    
                    $fixedValueIds = [];
                    if (is_array($valueIds)) {
                        foreach ($valueIds as $valueId) {
                            if (is_numeric($valueId)) {
                                $fixedValueIds[] = (int)$valueId;
                            }
                        }
                    }
                    
                    $customizations[] = [
                        'option_id' => (int)$customRow['option_id'],
                        'group_id' => (int)$customRow['group_id'],
                        'selected_value_ids' => $fixedValueIds,
                        'selected_values' => is_array($selectedValues) ? $selectedValues : [],
                        'text_value' => $customRow['text_value']
                    ];
                }
                
                $package_items[] = [
                    "item_id" => $package_item_id,
                    "name" => $package_item['item_name'],
                    "quantity" => $quantity,
                    "itemPrice" => $package_item_price,
                    "image_url" => $package_item['image_url'],
                    "customizations" => $customizations,
                    "isPackageItem" => true,
                    "parentPackageId" => $item_id
                ];
            }
            
            // Add package as main item with nested items
            $items[] = [
                "item_id" => $item_id,
                "name" => $package_name,
                "quantity" => $quantity,
                "itemPrice" => $itemPrice,
                "itemCost" => $itemPrice * $quantity,
                "image_url" => $imagUrl,
                "customizations" => [],
                "isPackage" => true,
                "packageId" => $item_id,
                "packageItems" => $package_items
            ];
            
            $package_items_stmt->close();
        } else {
            // This is a regular individual item
            $customSql = "
                SELECT 
                    option_id,
                    group_id,
                    selected_value_ids,
                    selected_values,
                    text_value
                FROM order_item_customizations
                WHERE oid = ? AND item_id = ?
            ";
            
            $customStmt = $conn->prepare($customSql);
            $customStmt->bind_param("ii", $oid, $item_id);
            $customStmt->execute();
            $customResult = $customStmt->get_result();
            
            $customizations = [];
            while ($customRow = $customResult->fetch_assoc()) {
                error_log("DEBUG get_orders: customRow = " . json_encode($customRow));
                
                $valueIds = json_decode($customRow['selected_value_ids'] ?? '[]', true);
                $selectedValues = json_decode($customRow['selected_values'] ?? '[]', true);
                
                error_log("DEBUG get_orders: valueIds=" . json_encode($valueIds) . ", selectedValues=" . json_encode($selectedValues));
            
                // 🔴 CRITICAL FIX: Ensure selected_value_ids only contains integers
                // If it contains strings (from legacy data), use selected_values instead
                $cleanValueIds = [];
                if (is_array($valueIds)) {
                    foreach ($valueIds as $val) {
                        if (is_numeric($val)) {
                            $cleanValueIds[] = (int)$val;
                        }
                    }
                }
                
                // If we couldn't get valid integer IDs, use an empty array
                // The selected_values will still show the names for display
                if (empty($cleanValueIds) && !empty($selectedValues)) {
                    $cleanValueIds = [];  // Empty array but selected_values has the display names
                }
                
                $customizations[] = [
                    "option_id" => (int)$customRow['option_id'],
                    "group_id" => (int)$customRow['group_id'],
                    "selected_value_ids" => $cleanValueIds,
                    "selected_values" => $selectedValues,
                    "text_value" => $customRow['text_value']
                ];
            }
            $customStmt->close();
            
            error_log("DEBUG get_orders: Found " . count($customizations) . " customizations for item_id=$item_id");
            
            $items[] = [
                "item_id" => $item_id,
                "name" => $itemRow['item_name'],
                "quantity" => $quantity,
                "itemPrice" => $itemPrice,
                "itemCost" => $itemPrice * $quantity,
                "image_url" => $imagUrl,
                "customizations" => $customizations,
                "isFromPackage" => false
            ];
        }
        
        $package_stmt->close();
    }

    $order['items'] = $items;
    
    // Fetch packages for this order
    $packageSql = "
        SELECT 
            op.package_id,
            mp.package_name,
            mp.amounts AS package_price,
            op.qty AS quantity,
            op.note
        FROM order_packages op
        JOIN menu_package mp ON op.package_id = mp.package_id
        WHERE op.oid = ?
    ";

    $packageStmt = $conn->prepare($packageSql);
    if (!$packageStmt) {
        error_log("Prepare failed for packages: " . $conn->error);
    } else {
        $packageStmt->bind_param("i", $oid);
        $packageStmt->execute();
        $packageResult = $packageStmt->get_result();

        $packages = [];
        while ($packageRow = $packageResult->fetch_assoc()) {
            $package_id = (int)$packageRow['package_id'];
            $package_name = $packageRow['package_name'];
            $package_price = (float)$packageRow['package_price'];
            $qty = (int)$packageRow['quantity'];
            
            // ✅ 修改：只查詢實際保存的菜品（order_items），而不是 package_dish 中的所有選項
            $dishSql = "
                SELECT 
                    oi.item_id,
                    mit.item_name,
                    mi.item_price,
                    oi.qty,
                    oi.note
                FROM order_items oi
                JOIN menu_item mi ON oi.item_id = mi.item_id
                JOIN menu_item_translation mit ON mi.item_id = mit.item_id
                WHERE oi.oid = ? AND mit.language_code = ?
            ";
            
            $dishStmt = $conn->prepare($dishSql);
            if (!$dishStmt) {
                error_log("Prepare failed for order items: " . $conn->error);
                continue;
            }
            
            $dishStmt->bind_param("is", $oid, $language);
            $dishStmt->execute();
            $dishResult = $dishStmt->get_result();
            
            $dishes = [];
            while ($dishRow = $dishResult->fetch_assoc()) {
                $dish_item_id = (int)$dishRow['item_id'];
                
                // Parse customizations from note column
                $customizations = [];
                if (!empty($dishRow['note'])) {
                    $decoded = json_decode($dishRow['note'], true);
                    if (json_last_error() === JSON_ERROR_NONE && is_array($decoded)) {
                        // The note contains customizations as JSON array
                        foreach ($decoded as $cust) {
                            $customizations[] = [
                                "option_id" => (int)($cust['option_id'] ?? 0),
                                "group_id" => (int)($cust['group_id'] ?? 0),
                                "group_name" => $cust['group_name'] ?? '',
                                "selected_value_ids" => $cust['selected_value_ids'] ?? [],
                                "selected_values" => $cust['selected_values'] ?? [],
                                "text_value" => $cust['text_value'] ?? ''
                            ];
                        }
                        error_log("Parsed " . count($customizations) . " customizations for package dish item_id=$dish_item_id");
                    } else {
                        error_log("Failed to parse customizations JSON for item_id=$dish_item_id: " . json_last_error_msg());
                    }
                }
                
                $dishes[] = [
                    "item_id" => $dish_item_id,
                    "name" => $dishRow['item_name'],
                    "price" => (float)$dishRow['item_price'],
                    "quantity" => (int)$dishRow['qty'],
                    "customizations" => $customizations
                ];
            }
            $dishStmt->close();
            
            $packages[] = [
                "package_id" => $package_id,
                "package_name" => $package_name,
                "package_price" => $package_price,
                "quantity" => $qty,
                "note" => $packageRow['note'],
                "dishes" => $dishes,
                "packageCost" => $package_price * $qty
            ];
        }
        $packageStmt->close();
        
        $order['packages'] = $packages;
    }
    $orders[] = $order;

    $itemStmt->close();
}

$stmt->close();
$conn->close();

echo json_encode($orders);
?>