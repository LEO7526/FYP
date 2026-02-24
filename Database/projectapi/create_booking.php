<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB connection failed"]);
    exit();
}


$data = json_decode(file_get_contents("php://input"), true);

$cid = $data['cid'] ?? null;
$bkcname = $data['bkcname'] ?? '';
$bktel = $data['bktel'] ?? '';
$tid = $data['tid'] ?? 0;
$bdate = $data['bdate'] ?? '';
$btime = $data['btime'] ?? '';
$pnum = $data['pnum'] ?? 0;
$purpose = $data['purpose'] ?? null;
$remark = $data['remark'] ?? null;

if (empty($bkcname) || empty($bktel) || $tid <= 0 || empty($bdate) || empty($btime) || $pnum <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Required booking information is missing."]);
    exit();
}

// Validate selected table and enforce booking rules
$checkTableSql = "SELECT capacity FROM seatingChart WHERE tid = ? LIMIT 1";
$checkStmt = $conn->prepare($checkTableSql);
$checkStmt->bind_param("i", $tid);
$checkStmt->execute();
$checkResult = $checkStmt->get_result();

if ($checkResult->num_rows === 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid table selected."]);
    $checkStmt->close();
    exit();
}

$tableRow = $checkResult->fetch_assoc();
$tableCapacity = (int)$tableRow['capacity'];
$checkStmt->close();

// Business rule: parties of 3 or less cannot book 8-person tables
if ($pnum <= 3 && $tableCapacity >= 8) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "For 3 or fewer guests, 8-person tables cannot be booked."]);
    exit();
}


$sql = "INSERT INTO booking (cid, bkcname, bktel, tid, bdate, btime, pnum, purpose, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("isiississ", $cid, $bkcname, $bktel, $tid, $bdate, $btime, $pnum, $purpose, $remark);

if ($stmt->execute()) {
    http_response_code(201);
    echo json_encode(["success" => true, "message" => "Booking submitted successfully. Please wait for confirmation."]);
} else {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Failed to create booking."]);
}

$stmt->close();
$conn->close();
?>