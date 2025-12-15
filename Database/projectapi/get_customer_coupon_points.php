<?php
/**
 * 獲取客戶的優惠券點數
 * 
 * 請求：GET /get_customer_coupon_points.php?cid=1
 * 回應：
 * {
 *   "success": true,
 *   "coupon_points": 150
 * }
 */
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json; charset=utf-8');

$cid = isset($_GET['cid']) ? intval($_GET['cid']) : null;

if (!$cid) {
    http_response_code(400);
    echo json_encode(['success' => false, 'error' => 'cid parameter required']);
    exit;
}

$conn = new mysqli('localhost', 'root', '', 'ProjectDB');
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(['success' => false, 'error' => 'Database connection failed: ' . $conn->connect_error]);
    exit;
}

$query = "SELECT coupon_point FROM customer WHERE cid = ?";
$stmt = $conn->prepare($query);

if (!$stmt) {
    http_response_code(500);
    echo json_encode(['success' => false, 'error' => 'Query prepare failed: ' . $conn->error]);
    $conn->close();
    exit;
}

$stmt->bind_param('i', $cid);
if (!$stmt->execute()) {
    http_response_code(500);
    echo json_encode(['success' => false, 'error' => 'Query execute failed: ' . $stmt->error]);
    $stmt->close();
    $conn->close();
    exit;
}

$result = $stmt->get_result();

if ($result && $result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode([
        'success' => true,
        'coupon_points' => intval($row['coupon_point'])
    ]);
} else {
    echo json_encode([
        'success' => false,
        'error' => 'Customer not found',
        'coupon_points' => 0
    ]);
}

$stmt->close();
$conn->close();
?>
