<?php
// get_table_status.php - For staff table management
require_once '../../conn.php';
header('Content-Type: application/json');

$currentTime = date('H:i:s');
$currentDate = date('Y-m-d');

$sql = "SELECT 
            sc.tid as table_number, 
            o.ostatus,
            (SELECT COUNT(*) FROM booking b 
             WHERE b.tid = sc.tid 
             AND b.bdate = '$currentDate' 
             AND b.status IN (1, 2) 
             AND b.btime BETWEEN SUBTIME('$currentTime', '00:30:00') AND ADDTIME('$currentTime', '00:30:00')
            ) as booking_count
        FROM seatingChart sc
        LEFT JOIN (
             SELECT table_number, oid 
             FROM table_orders 
             WHERE toid IN (SELECT MAX(toid) FROM table_orders GROUP BY table_number)
        ) t ON sc.tid = t.table_number
        LEFT JOIN orders o ON t.oid = o.oid
        ORDER BY sc.tid ASC";

$result = $conn->query($sql);

$tables = array();
while ($row = $result->fetch_assoc()) {
    $orderStatus = $row['ostatus'];
    $bookingCount = intval($row['booking_count']);

    if ($orderStatus == 1 || $orderStatus == 2) {
        $status = 1; // Red
        $text = "Occupied";
    } else if ($bookingCount > 0) {
        $status = 2; // Yellow  
        $text = "Booked";
    } else {
        $status = 0; // Gray
        $text = "Available";
    }

    $tables[] = ["id" => $row['table_number'], "status" => $status, "status_text" => $text];
}
echo json_encode(["status" => "success", "data" => $tables]);
$conn->close();
?>