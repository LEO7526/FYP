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

// Get the latest order for customer 1
$sql = "
    SELECT 
        o.oid,
        o.odate,
        o.ostatus,
        c.cname,
        COUNT(DISTINCT oi.item_id) as item_count,
        COUNT(DISTINCT op.package_id) as package_count
    FROM orders o
    LEFT JOIN customer c ON o.cid = c.cid
    LEFT JOIN order_items oi ON o.oid = oi.oid
    LEFT JOIN order_packages op ON o.oid = op.oid
    WHERE o.cid = 1
    GROUP BY o.oid
    ORDER BY o.odate DESC
    LIMIT 1
";

$result = $conn->query($sql);

if ($result && $result->num_rows > 0) {
    $order = $result->fetch_assoc();
    echo json_encode($order);
} else {
    echo json_encode(['error' => 'No orders found']);
}

$conn->close();
?>
