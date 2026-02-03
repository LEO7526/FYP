<?php
// get_coupon_metadata.php
require_once 'db_connect.php';
header('Content-Type: application/json');

$response = [
    "categories" => [],
    "items" => [],
    "packages" => []
];

// 1. 獲取分類
$sql = "SELECT category_id, category_name FROM menu_category";
$result = $conn->query($sql);
while($row = $result->fetch_assoc()) {
    $response["categories"][] = $row;
}

// 2. 獲取菜品 (只抓英文名供內部選擇用)
$sql = "SELECT mi.item_id, mit.item_name 
        FROM menu_item mi 
        JOIN menu_item_translation mit ON mi.item_id = mit.item_id 
        WHERE mit.language_code = 'en' AND mi.is_available = 1";
$result = $conn->query($sql);
while($row = $result->fetch_assoc()) {
    $response["items"][] = $row;
}

// 3. 獲取套餐
$sql = "SELECT package_id, package_name FROM menu_package";
$result = $conn->query($sql);
while($row = $result->fetch_assoc()) {
    $response["packages"][] = $row;
}

echo json_encode(["status" => "success", "data" => $response]);
$conn->close();
?>