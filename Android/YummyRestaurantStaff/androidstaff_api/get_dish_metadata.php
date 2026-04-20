<?php
// get_dish_metadata.php
require_once 'db_connect.php';
header('Content-Type: application/json');

$response = [
    "categories" => [],
    "materials" => []
];

// 1. 獲取菜單分類 (Menu Categories)
$sql = "SELECT category_id, category_name FROM menu_category";
$result = $conn->query($sql);
while($row = $result->fetch_assoc()) {
    $response["categories"][] = $row;
}

// 2. 獲取庫存原料 (Materials) - 供食譜使用
$sql = "SELECT mid, mname, unit FROM materials ORDER BY mname ASC";
$result = $conn->query($sql);
while($row = $result->fetch_assoc()) {
    $response["materials"][] = $row;
}

echo json_encode(["status" => "success", "data" => $response]);
$conn->close();
?>