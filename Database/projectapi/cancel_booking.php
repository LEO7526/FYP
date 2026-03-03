<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
date_default_timezone_set("Asia/Hong_Kong");

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "DB connection failed"]);
    exit();
}

$data = json_decode(file_get_contents("php://input"), true);

$bid = isset($data['bid']) ? (int)$data['bid'] : 0;
$cid = isset($data['cid']) ? (int)$data['cid'] : 0;

if ($bid <= 0 || $cid <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "bid and cid are required."]);
    exit();
}

$bookingSql = "SELECT bid, cid, bdate, btime, status FROM booking WHERE bid = ? AND cid = ? LIMIT 1";
$bookingStmt = $conn->prepare($bookingSql);
$bookingStmt->bind_param("ii", $bid, $cid);
$bookingStmt->execute();
$bookingResult = $bookingStmt->get_result();

if ($bookingResult->num_rows === 0) {
    http_response_code(404);
    echo json_encode(["success" => false, "message" => "Booking not found."]);
    $bookingStmt->close();
    $conn->close();
    exit();
}

$booking = $bookingResult->fetch_assoc();
$bookingStmt->close();

if ((int)$booking['status'] === 0) {
    http_response_code(409);
    echo json_encode(["success" => false, "message" => "Booking is already cancelled."]);
    $conn->close();
    exit();
}

$bookingDateTime = DateTime::createFromFormat('Y-m-d H:i:s', $booking['bdate'] . ' ' . $booking['btime']);
if (!$bookingDateTime) {
    $bookingDateTime = DateTime::createFromFormat('Y-m-d H:i', $booking['bdate'] . ' ' . $booking['btime']);
}
if (!$bookingDateTime) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Invalid booking datetime in database."]);
    $conn->close();
    exit();
}

$now = new DateTime();
$hoursToBooking = ($bookingDateTime->getTimestamp() - $now->getTimestamp()) / 3600;

if ($hoursToBooking < 5) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Cancellation is not allowed within 5 hours of booking time."
    ]);
    $conn->close();
    exit();
}

$cancelSql = "UPDATE booking SET status = 0 WHERE bid = ? AND cid = ?";
$cancelStmt = $conn->prepare($cancelSql);
$cancelStmt->bind_param("ii", $bid, $cid);

if ($cancelStmt->execute()) {
    echo json_encode(["success" => true, "message" => "Booking cancelled successfully."]);
} else {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Failed to cancel booking."]);
}

$cancelStmt->close();
$conn->close();
?>
