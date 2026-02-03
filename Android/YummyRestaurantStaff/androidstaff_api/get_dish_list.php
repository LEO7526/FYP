<?php
// get_dish_list.php
require_once 'db_connect.php';
header('Content-Type: application/json');

$dishes = [];

// 抓取菜色 (連結分類表、翻譯表)
// 這裡我們只抓英文名稱當作列表標題
$sql = "SELECT m.item_id, m.item_price, m.image_url, m.is_available, 
               mc.category_name, t.item_name
        FROM menu_item m
        JOIN menu_category mc ON m.category_id = mc.category_id
        LEFT JOIN menu_item_translation t ON m.item_id = t.item_id AND t.language_code = 'en'
        ORDER BY m.item_id DESC"; // 最新的在最上面

$result = $conn->query($sql);

while ($row = $result->fetch_assoc()) {
    $dishes[] = [
        "id" => $row['item_id'],
        "name" => $row['item_name'] ?? "No Name",
        "price" => $row['item_price'],
        "category" => $row['category_name'],
        "image" => $row['image_url'],
        "available" => $row['is_available']
    ];
}

echo json_encode(["status" => "success", "data" => $dishes]);
$conn->close();
?>