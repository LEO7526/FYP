<?php
header('Content-Type: application/json');
require_once 'db_config.php';
$response = ['success' => false, 'data' => []];
$sql = "SELECT mid, mname, mqty, munit, mreorderqty FROM material ORDER BY mname ASC";
$result = mysqli_query($conn, $sql);
if ($result) {
    $response['success'] = true;
    while ($row = mysqli_fetch_assoc($result)) { $response['data'][] = $row; }
} else { $response['message'] = "Failed to fetch materials."; }
mysqli_close($conn);
echo json_encode($response);
?>