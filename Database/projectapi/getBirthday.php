<?php
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode([
        "success"    => false,
        "message"    => "DB connection failed",
        "error_code" => "DB_CONNECTION_FAILED"
    ]);
    exit;
}

$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
if ($cid <= 0) {
    echo json_encode([
        "success"    => false,
        "message"    => "Invalid customer ID",
        "error_code" => "INVALID_CUSTOMER_ID"
    ]);
    exit;
}

$stmt = $conn->prepare("SELECT cbirthday FROM customer WHERE cid=?");
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result()->fetch_assoc();
$stmt->close();

if ($result && !empty($result['cbirthday'])) {
    echo json_encode([
        "success"    => true,
        "message"    => "Birthday found",
        "cbirthday"  => $result['cbirthday'], // format: MM-DD
        "error_code" => null
    ]);
} else {
    echo json_encode([
        "success"    => true,
        "message"    => "Birthday not set",
        "cbirthday"  => null,
        "error_code" => "NO_BIRTHDAY_SET"
    ]);
}

$conn->close();
?>