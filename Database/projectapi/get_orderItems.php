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

// Query order_items with menu_item and translation
$sql = "
    SELECT 
        oi.item_id,
        mi.item_price,
        mit.item_name,
        oi.qty AS quantity
    FROM order_items oi
    JOIN menu_item_translation mit ON oi.item_id = mit.item_id
    JOIN menu_item mi ON mit.item_id = mi.item_id
    WHERE mit.language_code = ? AND oi.oid = ?
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
        "item_id" => (int)$row['item_id'],
        "name" => $row['item_name'],
        "quantity" => $quantity,
        "itemPrice" => $itemPrice,
        "itemCost" => $itemPrice * $quantity
    ];
}

$stmt->close();
$conn->close();

echo json_encode($items);
?>