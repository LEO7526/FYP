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
        $items[] = [
            "item_id" => (int)$itemRow['item_id'],
            "name" => $itemRow['item_name'],
            "quantity" => $quantity,
            "itemPrice" => $itemPrice,
            "itemCost" => $itemPrice * $quantity
        ];
    }

    $order['items'] = $items;
    $orders[] = $order;

    $itemStmt->close();
}

$stmt->close();
$conn->close();

echo json_encode($orders);
?>