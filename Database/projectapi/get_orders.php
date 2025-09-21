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

$sql = "
    SELECT 
        o.oid,
        o.odate,
        o.ocost,
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
    $orders[] = $row;
}
echo json_encode($orders);

$conn->close();
?>