<?php
// C:\xampp\htdocs\androidstaff_api\inventory\get_consumption_history.php
header('Content-Type: application/json');
require_once 'db_connect.php';

$response = ['success' => false, 'data' => []];

$sql = "SELECT log_id, log_date, log_type, details FROM consumption_history ORDER BY log_date DESC, log_id DESC";
$result = $conn->query($sql);

if ($result) {
    $response['success'] = true;
    while ($row = $result->fetch_assoc()) {
        $response['data'][] = $row;
    }
} else {
    $response['message'] = "Failed to fetch history";
}

$conn->close();
echo json_encode($response);
?>