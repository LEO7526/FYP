<?php
header('Content-Type: application/json');
require_once 'db_config.php';

$response = ['success' => false, 'data' => []];

// 從 reorder_history 表中獲取所有紀錄，按時間倒序排列（最新的在最前面）
$sql = "SELECT material_name, reordered_quantity, reorder_timestamp FROM reorder_history ORDER BY reorder_timestamp DESC";
$result = mysqli_query($conn, $sql);

if ($result) {
    $response['success'] = true;
    while ($row = mysqli_fetch_assoc($result)) {
        $response['data'][] = $row;
    }
} else {
    $response['message'] = "Failed to fetch reorder history.";
}

mysqli_close($conn);
echo json_encode($response);
?>