<?php
header('Content-Type: application/json');

// Connect to MySQL
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    error_log("DB Connection failed: " . $conn->connect_error);
    echo json_encode(["error" => "Connection failed"]);
    exit;
}

// Get order_id from query string
$order_id = $_GET['order_id'] ?? null;
$language = $_GET['lang'] ?? 'en'; // default to English

if (!$order_id) {
    error_log("Missing order_id");
    echo json_encode(["error" => "Missing order_id"]);
    exit;
}

// Query order_items with menu_item and translation
$sql = "
    SELECT 
        oi.pid,
        mi.item_price,
        mit.item_name,
        oi.oqty AS quantity
    FROM order_items oi
    JOIN menu_item mi ON oi.pid = mi.item_id
    JOIN menu_item_translation mit ON mi.item_id = mit.item_id AND mit.language_code = ?
    WHERE oi.oid = ?
";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    error_log("Prepare failed: " . $conn->error);
    echo json_encode(["error" => "Failed to prepare query"]);
    exit;
}

$stmt->bind_param("si", $language, $order_id);
$stmt->execute();
$result = $stmt->get_result();

$items = [];
while ($row = $result->fetch_assoc()) {
    $itemPrice = (float)$row['item_price'];
    $quantity = (int)$row['quantity'];
    $items[] = [
        "pid" => (int)$row['pid'],
        "pname" => $row['item_name'],
        "quantity" => $quantity,
        "itemPrice" => $itemPrice,
        "itemCost" => $itemPrice * $quantity
    ];
}

$stmt->close();
$conn->close();

echo json_encode($items);