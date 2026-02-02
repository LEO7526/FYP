<?php
require_once '../conn.php';
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    exit(json_encode(['success' => false, 'message' => 'Method not allowed']));
}

$action = $_GET['action'] ?? '';

try {
    if ($action === 'get_categories') {
        // Get all material categories
        $sql = "SELECT * FROM materials_category ORDER BY category_name";
        $result = mysqli_query($conn, $sql);
        $data = mysqli_fetch_all($result, MYSQLI_ASSOC);
        echo json_encode(['success' => true, 'data' => $data]);
    }
    elseif ($action === 'get_materials_by_category') {
        // Get materials for specific category
        $catId = intval($_GET['category_id']);
        $sql = "SELECT mid, mname, unit FROM materials WHERE category_id = ? ORDER BY mname";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $catId);
        $stmt->execute();
        $result = $stmt->get_result();
        $data = mysqli_fetch_all($result, MYSQLI_ASSOC);
        echo json_encode(['success' => true, 'data' => $data]);
    }
    elseif ($action === 'get_all_materials') {
        // Get all materials with category information
        $sql = "SELECT m.mid, m.mname, m.unit, c.category_name 
                FROM materials m 
                LEFT JOIN materials_category c ON m.category_id = c.category_id 
                ORDER BY c.category_name, m.mname";
        $result = mysqli_query($conn, $sql);
        $data = mysqli_fetch_all($result, MYSQLI_ASSOC);
        echo json_encode(['success' => true, 'data' => $data]);
    }
    else {
        throw new Exception('Invalid action');
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
?>