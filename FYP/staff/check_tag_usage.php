<?php
require_once '../conn.php';

header('Content-Type: application/json');

// Allow only GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Only GET requests are allowed']);
    exit;
}

// Check if tag_id is provided
if (!isset($_GET['tag_id'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Missing tag_id']);
    exit;
}

$tag_id = $_GET['tag_id'];

// Check if the tag is used by any menu item
$sql = "SELECT COUNT(*) as usage_count FROM menu_tag WHERE tag_id = ?";
$stmt = mysqli_prepare($conn, $sql);
mysqli_stmt_bind_param($stmt, "i", $tag_id);
mysqli_stmt_execute($stmt);
$result = mysqli_stmt_get_result($stmt);
$row = mysqli_fetch_assoc($result);

$is_used = $row['usage_count'] > 0;

echo json_encode(['is_used' => $is_used]);

mysqli_stmt_close($stmt);
?>