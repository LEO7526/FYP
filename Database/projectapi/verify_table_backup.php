<?php
error_reporting(E_ALL);
ini_set('display_errors', 0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

set_error_handler(function($errno, $errstr, $errfile, $errline) {
    http_response_code(500);
    echo json_encode(['success' => false, 'valid' => false, 'message' => 'Error: ' . $errstr]);
    exit;
});

$host = 'localhost';
$user = 'root';
$pass = '';
$dbname = 'projectdb';

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(['success' => false, 'valid' => false, 'message' => 'Connection failed: ' . $conn->connect_error]);
    exit;
}

$conn->set_charset("utf8mb4");

$table_id = isset($_POST['table_id']) ? intval($_POST['table_id']) : (isset($_GET['table_id']) ? intval($_GET['table_id']) : 0);

if ($table_id <= 0) {
    http_response_code(400);
    echo json_encode(['success' => false, 'valid' => false, 'message' => 'Invalid table_id']);
    $conn->close();
    exit;
}

$sql = "SELECT tid, capacity, status FROM seatingChart WHERE tid = ? LIMIT 1";
$stmt = $conn->prepare($sql);

if ($stmt === false) {
    http_response_code(500);
    echo json_encode(['success' => false, 'valid' => false, 'message' => $conn->error]);
    $conn->close();
    exit;
}

$stmt->bind_param("i", $table_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    http_response_code(404);
    echo json_encode(['success' => true, 'valid' => false, 'message' => 'Table not found', 'table_id' => $table_id]);
    $stmt->close();
    $conn->close();
    exit;
}

$row = $result->fetch_assoc();
$tid = intval($row['tid']);
$capacity = intval($row['capacity']);
$seating_status = intval($row['status']);

$sqlStatus = "SELECT status FROM table_orders WHERE table_number = ? ORDER BY created_at DESC LIMIT 1";
$stmtStatus = $conn->prepare($sqlStatus);

if ($stmtStatus === false) {
    http_response_code(500);
    echo json_encode(['success' => false, 'valid' => false, 'message' => $conn->error]);
    $conn->close();
    exit;
}

$stmtStatus->bind_param("i", $table_id);
$stmtStatus->execute();
$resultStatus = $stmtStatus->get_result();

$current_status = 'available';
$is_available = true;

if ($resultStatus->num_rows > 0) {
    $statusRow = $resultStatus->fetch_assoc();
    $order_status = $statusRow['status'];
    
    if ($order_status === 'ordering' || $order_status === 'seated' || $order_status === 'ready_to_pay') {
        $current_status = 'occupied';
        $is_available = false;
    } elseif ($order_status === 'reserved') {
        $current_status = 'reserved';
        $is_available = false;
    }
}

if ($seating_status == 1 && $current_status === 'available') {
    $current_status = 'reserved';
    $is_available = false;
}

http_response_code(200);
echo json_encode([
    'success' => true,
    'valid' => true,
    'table_id' => $tid,
    'capacity' => $capacity,
    'status' => $current_status,
    'available' => $is_available,
    'message' => $is_available ? 'Table is available for ordering' : 'Table is not available'
]);

$stmt->close();
$stmtStatus->close();
$conn->close();
