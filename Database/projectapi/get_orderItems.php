<?php
header('Content-Type: application/json');

// Connect to MySQL
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    error_log("DB Connection failed: " . $conn->connect_error);
    echo json_encode(["error" => "Connection failed"]);
    exit;
}

// Get order_id and language from query string
$order_id = isset($_GET['order_id']) ? intval($_GET['order_id']) : 0;
$language = $_GET['lang'] ?? 'en'; // default to English

if ($order_id === 0) {
    error_log("Missing or invalid order_id");
    echo json_encode(["error" => "Missing or invalid order_id"]);
    exit;
}

$items = [];

// First, get individual items from order_items
$sql_items = "
    SELECT 
        oi.item_id,
        mi.item_price,
        mit.item_name,
        oi.qty AS quantity,
        'item' as type
    FROM order_items oi
    JOIN menu_item_translation mit ON oi.item_id = mit.item_id
    JOIN menu_item mi ON mit.item_id = mi.item_id
    WHERE mit.language_code = ? AND oi.oid = ?
";

$stmt_items = $conn->prepare($sql_items);
if (!$stmt_items) {
    error_log("Prepare failed: " . $conn->error);
    echo json_encode(["error" => "Failed to prepare query"]);
    exit;
}

$stmt_items->bind_param("si", $language, $order_id);
$stmt_items->execute();
$result_items = $stmt_items->get_result();

while ($row = $result_items->fetch_assoc()) {
    $item_id = (int)$row['item_id'];
    $itemPrice = (float)$row['item_price'];
    $quantity = (int)$row['quantity'];
    
    // Check if this item_id corresponds to a package
    $package_check_sql = "SELECT package_id FROM menu_package WHERE package_id = ?";
    $package_stmt = $conn->prepare($package_check_sql);
    $package_stmt->bind_param("i", $item_id);
    $package_stmt->execute();
    $package_result = $package_stmt->get_result();
    
    if ($package_result->num_rows > 0) {
        // This is a package - get all items in the package
        $package_items_sql = "
            SELECT 
                pd.item_id,
                mi.item_price,
                mit.item_name,
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
        
        while ($package_item = $package_items_result->fetch_assoc()) {
            $package_item_price = (float)$package_item['item_price'];
            $items[] = [
                "item_id" => (int)$package_item['item_id'],
                "name" => $package_item['item_name'] . " (Package)",
                "quantity" => $quantity, // Same quantity as the package
                "itemPrice" => $package_item_price,
                "itemCost" => $package_item_price * $quantity,
                "isFromPackage" => true,
                "packageId" => $item_id
            ];
        }
        
        $package_items_stmt->close();
    } else {
        // This is a regular individual item
        $items[] = [
            "item_id" => $item_id,
            "name" => $row['item_name'],
            "quantity" => $quantity,
            "itemPrice" => $itemPrice,
            "itemCost" => $itemPrice * $quantity,
            "isFromPackage" => false
        ];
    }
    
    $package_stmt->close();
}

$stmt_items->close();

// Also check for packages in order_packages table (if used)
$sql_packages = "
    SELECT 
        op.package_id,
        mp.base_price,
        mpt.package_name,
        op.qty AS quantity
    FROM order_packages op
    JOIN menu_package mp ON op.package_id = mp.package_id
    JOIN menu_package_translation mpt ON op.package_id = mpt.package_id
    WHERE mpt.language_code = ? AND op.oid = ?
";

$stmt_packages = $conn->prepare($sql_packages);
if ($stmt_packages) {
    $stmt_packages->bind_param("si", $language, $order_id);
    $stmt_packages->execute();
    $result_packages = $stmt_packages->get_result();
    
    while ($row = $result_packages->fetch_assoc()) {
        $package_id = (int)$row['package_id'];
        $basePrice = (float)$row['base_price'];
        $quantity = (int)$row['quantity'];
        
        // Get all items in this package
        $package_items_sql = "
            SELECT 
                pd.item_id,
                mi.item_price,
                mit.item_name,
                pd.price_modifier
            FROM package_dish pd
            JOIN menu_item_translation mit ON pd.item_id = mit.item_id
            JOIN menu_item mi ON pd.item_id = mi.item_id
            WHERE pd.package_id = ? AND mit.language_code = ?
            ORDER BY pd.item_id
        ";
        
        $package_items_stmt = $conn->prepare($package_items_sql);
        $package_items_stmt->bind_param("is", $package_id, $language);
        $package_items_stmt->execute();
        $package_items_result = $package_items_stmt->get_result();
        
        while ($package_item = $package_items_result->fetch_assoc()) {
            $package_item_price = (float)$package_item['item_price'];
            $items[] = [
                "item_id" => (int)$package_item['item_id'],
                "name" => $package_item['item_name'] . " (from " . $row['package_name'] . ")",
                "quantity" => $quantity,
                "itemPrice" => $package_item_price,
                "itemCost" => $package_item_price * $quantity,
                "isFromPackage" => true,
                "packageId" => $package_id,
                "packageName" => $row['package_name']
            ];
        }
        
        $package_items_stmt->close();
    }
    
    $stmt_packages->close();
}

$conn->close();

echo json_encode($items);
?>