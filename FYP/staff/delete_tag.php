<?php
require_once '../conn.php';

header('Content-Type: application/json');

// Only POST requests are allowed
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Only POST requests are allowed']);
    exit;
}

// Get input data
$data = json_decode(file_get_contents('php://input'), true);

// Validate input
if (!isset($data['tag_id'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Missing tag_id']);
    exit;
}

$tag_id = $data['tag_id'];

try {
    // Delete the tag
    $sql = "DELETE FROM tag WHERE tag_id = ?";
    $stmt = mysqli_prepare($conn, $sql);
    mysqli_stmt_bind_param($stmt, "i", $tag_id);

    if (mysqli_stmt_execute($stmt)) {
        echo json_encode(['success' => true, 'message' => 'Tag deleted successfully']);
    } else {
        throw new Exception('Deletion failed');
    }

    mysqli_stmt_close($stmt);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Failed to delete tag: ' . $e->getMessage()]);
}
?>