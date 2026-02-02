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
if (!isset($data['tag_name'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Missing tag_name']);
    exit;
}

$tag_name = trim($data['tag_name']);

try {
    // Check if the tag name already exists
    $sql = "SELECT COUNT(*) as count FROM tag WHERE tag_name = ?";
    $stmt = mysqli_prepare($conn, $sql);
    mysqli_stmt_bind_param($stmt, "s", $tag_name);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);
    $row = mysqli_fetch_assoc($result);

    $exists = $row['count'] > 0;

    echo json_encode([
        'success' => true,
        'exists' => $exists,
        'message' => $exists ? 'Tag name already exists' : 'Tag name is available'
    ]);

    mysqli_stmt_close($stmt);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error checking tag: ' . $e->getMessage()]);
}
?>