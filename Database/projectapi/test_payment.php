<?php
header('Content-Type: application/json');

$conn = new mysqli('localhost', 'root', '', 'ProjectDB');
if ($conn->connect_error) {
    echo json_encode(['error' => $conn->connect_error]);
    exit;
}

echo json_encode([
    'orders' => $conn->query('SELECT oid, orderRef, cid FROM orders LIMIT 3')->fetch_all(MYSQLI_ASSOC),
    'customer_1_points' => $conn->query('SELECT coupon_point FROM customer WHERE cid = 1')->fetch_assoc()
]);

$conn->close();
?>
