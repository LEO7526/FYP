<?php
require_once '../conn.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Only POST requests are allowed']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);

$required = ['tag_name_en', 'tag_name_zh_cn', 'tag_name_zh_tw', 'tag_category', 'tag_color'];
foreach ($required as $field) {
    if (empty($data[$field])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => "Missing required field: $field"]);
        exit;
    }
}

$tag_name_en   = trim($data['tag_name_en']);
$tag_name_zh_cn= trim($data['tag_name_zh_cn']);
$tag_name_zh_tw= trim($data['tag_name_zh_tw']);
$tag_category  = trim($data['tag_category']);
$tag_color     = $data['tag_color'];

try {
    $check_sql = "SELECT COUNT(*) as count FROM tag WHERE tag_name = ?";
    $check_stmt = mysqli_prepare($conn, $check_sql);
    mysqli_stmt_bind_param($check_stmt, "s", $tag_name_en);
    mysqli_stmt_execute($check_stmt);
    $check_result = mysqli_stmt_get_result($check_stmt);
    $check_row = mysqli_fetch_assoc($check_result);
    if ($check_row['count'] > 0) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Tag name (English) already exists']);
        exit;
    }
    mysqli_stmt_close($check_stmt);

    mysqli_autocommit($conn, false);

    $sql = "INSERT INTO tag (tag_name, tag_category, tag_bg_color) VALUES (?, ?, ?)";
    $stmt = mysqli_prepare($conn, $sql);
    mysqli_stmt_bind_param($stmt, "sss", $tag_name_en, $tag_category, $tag_color);
    if (!mysqli_stmt_execute($stmt)) {
        throw new Exception('Failed to insert tag');
    }
    $new_tag_id = mysqli_insert_id($conn);
    mysqli_stmt_close($stmt);

    $sql_tw = "INSERT INTO tag_translation (tag_id, language_code, tag_name) VALUES (?, 'zh-TW', ?)";
    $stmt_tw = mysqli_prepare($conn, $sql_tw);
    mysqli_stmt_bind_param($stmt_tw, "is", $new_tag_id, $tag_name_zh_tw);
    if (!mysqli_stmt_execute($stmt_tw)) {
        throw new Exception('Failed to insert Traditional Chinese translation');
    }
    mysqli_stmt_close($stmt_tw);

    $sql_cn = "INSERT INTO tag_translation (tag_id, language_code, tag_name) VALUES (?, 'zh-CN', ?)";
    $stmt_cn = mysqli_prepare($conn, $sql_cn);
    mysqli_stmt_bind_param($stmt_cn, "is", $new_tag_id, $tag_name_zh_cn);
    if (!mysqli_stmt_execute($stmt_cn)) {
        throw new Exception('Failed to insert Simplified Chinese translation');
    }
    mysqli_stmt_close($stmt_cn);

    mysqli_commit($conn);
    mysqli_autocommit($conn, true);

    echo json_encode([
        'success' => true,
        'message' => 'Tag added successfully',
        'tag' => [
            'tag_id'        => $new_tag_id,
            'tag_name'      => $tag_name_en,
            'tag_category'  => $tag_category,
            'tag_bg_color'  => $tag_color
        ]
    ]);

} catch (Exception $e) {
    mysqli_rollback($conn);
    mysqli_autocommit($conn, true);
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Failed to add tag: ' . $e->getMessage()]);
}
?>