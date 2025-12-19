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

// Fetch order headers
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
    WHERE o.cid = ?
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
        $itemPrice = (float)$itemRow['item_price'];
        $quantity = (int)$itemRow['quantity'];
        $item_id = (int)$itemRow['item_id'];
        
        // Fetch customizations for this item
        $customSql = "
            SELECT 
                option_id,
                option_name,
                choice_names,
                text_value,
                additional_cost
            FROM order_item_customizations
            WHERE oid = ? AND item_id = ?
        ";
        
        $customStmt = $conn->prepare($customSql);
        $customStmt->bind_param("ii", $oid, $item_id);
        $customStmt->execute();
        $customResult = $customStmt->get_result();
        
        $customizations = [];
        while ($customRow = $customResult->fetch_assoc()) {
            $customizations[] = [
                "option_id" => (int)$customRow['option_id'],
                "option_name" => $customRow['option_name'],
                "choice_names" => $customRow['choice_names'],
                "text_value" => $customRow['text_value'],
                "additional_cost" => (float)$customRow['additional_cost']
            ];
        }
        $customStmt->close();
        
        $items[] = [
            "item_id" => $item_id,
            "name" => $itemRow['item_name'],
            "quantity" => $quantity,
            "itemPrice" => $itemPrice,
            "itemCost" => $itemPrice * $quantity,
            "image_url" => $itemRow['image_url'],
            "customizations" => $customizations
        ];
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
                    oi.qty
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
                $dishes[] = [
                    "item_id" => (int)$dishRow['item_id'],
                    "name" => $dishRow['item_name'],
                    "price" => (float)$dishRow['item_price'],
                    "quantity" => (int)$dishRow['qty']
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