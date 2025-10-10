
<?php
require_once '../conn.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Only POST requests are allowed']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['item_id']) || !isset($data['image_url'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Missing item_id or image_url']);
    exit;
}

$item_id = $data['item_id'];
$image_url = $data['image_url'];

$sql = "UPDATE menu_item SET image_url = ? WHERE item_id = ?";
$stmt = mysqli_prepare($conn, $sql);
mysqli_stmt_bind_param($stmt, "si", $image_url, $item_id);

if (mysqli_stmt_execute($stmt)) {
    echo json_encode(['success' => true, 'message' => 'Image URL updated successfully']);
} else {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Failed to update image URL']);
}

mysqli_stmt_close($stmt);
?>