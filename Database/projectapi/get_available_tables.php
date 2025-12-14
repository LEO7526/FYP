<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");


$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB connection failed"]);
    exit();
}

// --- 2. Get Input Parameters ---
$date = $_GET['date'] ?? '';
$time = $_GET['time'] ?? '';
$pnum = $_GET['pnum'] ?? 0;

if (empty($date) || empty($time) || $pnum <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Date, time, and number of people are required."]);
    exit();
}

/
$startTime = date('H:i:s', strtotime($time . ' -2 hours'));
$endTime = date('H:i:s', strtotime($time . ' +2 hours'));

$sql_booked = "SELECT tid FROM booking WHERE bdate = ? AND btime BETWEEN ? AND ? AND status != 0";
$stmt_booked = $conn->prepare($sql_booked);
$stmt_booked->bind_param("sss", $date, $startTime, $endTime);
$stmt_booked->execute();
$result_booked = $stmt_booked->get_result();
$booked_tids = [];
while ($row = $result_booked->fetch_assoc()) {
    $booked_tids[] = $row['tid'];
}
$stmt_booked->close();


$sql_available = "SELECT tid, capacity FROM seatingChart WHERE capacity >= ?";
if (count($booked_tids) > 0) {
  
    $placeholders = implode(',', array_fill(0, count($booked_tids), '?'));
    $sql_available .= " AND tid NOT IN ($placeholders)";
    $types = "i" . str_repeat('i', count($booked_tids));
    $params = array_merge([$pnum], $booked_tids);
} else {
    /
    $types = "i";
    $params = [$pnum];
}

$stmt_available = $conn->prepare($sql_available);

$stmt_available->bind_param($types, ...$params);
$stmt_available->execute();
$result_available = $stmt_available->get_result();
$available_tables = $result_available->fetch_all(MYSQLI_ASSOC);
$stmt_available->close();


echo json_encode($available_tables);
$conn->close();
?>