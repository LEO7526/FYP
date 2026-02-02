<?php
require_once '../conn.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Only POST requests are allowed']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['item_id'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Missing item_id']);
    exit;
}

$item_id = intval($data['item_id']);

try {
    // Start transaction
    mysqli_autocommit($conn, false);

    // 1. Delete menu tag associations
    $stmt = $conn->prepare("DELETE FROM menu_tag WHERE item_id = ?");
    $stmt->bind_param("i", $item_id);
    if (!$stmt->execute()) {
        throw new Exception("Failed to delete menu tags: " . $stmt->error);
    }
    $stmt->close();

    // 2. Delete multilingual translations
    $stmt = $conn->prepare("DELETE FROM menu_item_translation WHERE item_id = ?");
    $stmt->bind_param("i", $item_id);
    if (!$stmt->execute()) {
        throw new Exception("Failed to delete translation records: " . $stmt->error);
    }
    $stmt->close();

    // 3. Delete main menu item
    $stmt = $conn->prepare("DELETE FROM menu_item WHERE item_id = ?");
    $stmt->bind_param("i", $item_id);
    if (!$stmt->execute()) {
        throw new Exception("Failed to delete menu item: " . $stmt->error);
    }
    $stmt->close();

    // Commit transaction
    mysqli_commit($conn);

    echo json_encode([
        'success' => true,
        'message' => 'Dish deleted successfully'
    ]);

} catch (Exception $e) {
    // Rollback transaction
    mysqli_rollback($conn);
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Failed to delete dish: ' . $e->getMessage()
    ]);
} finally {
    // Restore autocommit
    mysqli_autocommit($conn, true);
}
?>