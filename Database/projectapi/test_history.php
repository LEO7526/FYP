<?php
header('Content-Type: application/json');

$conn = new mysqli('localhost', 'root', '', 'ProjectDB');
if ($conn->connect_error) {
    echo json_encode(['error' => $conn->connect_error]);
    exit;
}

// Check current points
$result = $conn->query('SELECT coupon_point FROM customer WHERE cid = 1');
$row = $result->fetch_assoc();
$currentPoints = $row['coupon_point'] ?? 0;

// Check history
$history = $conn->query('SELECT cph_id, cid, coupon_id, delta, resulting_points, action, note, created_at FROM coupon_point_history WHERE cid = 1 ORDER BY created_at DESC LIMIT 10');
$historyData = [];
while ($h = $history->fetch_assoc()) {
    $historyData[] = $h;
}

echo json_encode([
    'customer_1_current_points' => $currentPoints,
    'history_count' => count($historyData),
    'history' => $historyData
], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);

$conn->close();
?>
