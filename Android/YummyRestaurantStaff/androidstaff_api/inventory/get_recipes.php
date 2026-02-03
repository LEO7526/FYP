<?php
// C:\xampp\htdocs\androidstaff_api\inventory\get_recipes.php
header('Content-Type: application/json');
require_once 'db_connect.php';

$response = ['success' => false, 'data' => []];

// 獲取有食譜的菜色 (假設 recipes 表有 item_id 欄位)
$sql = "SELECT DISTINCT t.item_id, t.item_name 
        FROM menu_item_translation t
        JOIN menu_item m ON t.item_id = m.item_id
        WHERE t.language_code = 'en'"; // 確保只抓英文名

$result = $conn->query($sql);

if ($result) {
    $response['success'] = true;
    while ($row = $result->fetch_assoc()) {
        $row['item_id'] = (int)$row['item_id'];
        $response['data'][] = $row;
    }
} else {
    $response['message'] = "Failed to fetch recipes: " . $conn->error;
}

$conn->close();
echo json_encode($response);
?>