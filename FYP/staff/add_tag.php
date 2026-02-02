
<?php
require_once '../conn.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Only POST requests are allowed']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['tag_name']) || !isset($data['tag_category']) || !isset($data['tag_color'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

$tag_name = trim($data['tag_name']);
$tag_category = trim($data['tag_category']);
$tag_color = $data['tag_color'];

try {
    // Check again if tag name already exists
    $check_sql = "SELECT COUNT(*) as count FROM tag WHERE tag_name = ?";
    $check_stmt = mysqli_prepare($conn, $check_sql);
    mysqli_stmt_bind_param($check_stmt, "s", $tag_name);
    mysqli_stmt_execute($check_stmt);
    $check_result = mysqli_stmt_get_result($check_stmt);
    $check_row = mysqli_fetch_assoc($check_result);

    if ($check_row['count'] > 0) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Tag name already exists']);
        exit;
    }

    mysqli_stmt_close($check_stmt);

    // Insert new tag
    $sql = "INSERT INTO tag (tag_name, tag_category, tag_bg_color) VALUES (?, ?, ?)";
    $stmt = mysqli_prepare($conn, $sql);
    mysqli_stmt_bind_param($stmt, "sss", $tag_name, $tag_category, $tag_color);

    if (mysqli_stmt_execute($stmt)) {
        $new_tag_id = mysqli_insert_id($conn);

        echo json_encode([
            'success' => true,
            'message' => 'Tag added successfully',
            'tag' => [
                'tag_id' => $new_tag_id,
                'tag_name' => $tag_name,
                'tag_category' => $tag_category,
                'tag_bg_color' => $tag_color
            ]
        ]);
    } else {
        throw new Exception('Failed to add tag');
    }

    mysqli_stmt_close($stmt);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Failed to add tag: ' . $e->getMessage()]);
}
?>