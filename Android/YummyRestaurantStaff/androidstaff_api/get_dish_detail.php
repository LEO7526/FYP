<?php
// get_dish_detail.php
require_once 'db_connect.php';
header('Content-Type: application/json');

$id = $_GET['item_id'] ?? 0;

if ($id == 0) {
    echo json_encode(["status" => "error", "message" => "No ID"]);
    exit();
}

// 1. 抓取基本資料 (包含圖片、價格、分類)
$sql = "SELECT m.image_url, m.item_price, m.spice_level, m.is_available, mc.category_name 
        FROM menu_item m 
        JOIN menu_category mc ON m.category_id = mc.category_id 
        WHERE m.item_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id);
$stmt->execute();
$result = $stmt->get_result();
$dish = $result->fetch_assoc();

if (!$dish) {
    echo json_encode(["status" => "error", "message" => "Dish not found"]);
    exit();
}

// 2. 抓取多語言名稱 (En, zh-CN, zh-TW)
$names = [];
$sqlTrans = "SELECT language_code, item_name, item_description FROM menu_item_translation WHERE item_id = ?";
$stmtTrans = $conn->prepare($sqlTrans);
$stmtTrans->bind_param("i", $id);
$stmtTrans->execute();
$resTrans = $stmtTrans->get_result();
while ($row = $resTrans->fetch_assoc()) {
    $names[$row['language_code']] = [
        "name" => $row['item_name'],
        "desc" => $row['item_description']
    ];
}

// 3. 抓取食譜原料 (Recipe)
$recipe = [];
$sqlRecipe = "SELECT mat.mname, mat.unit, rm.quantity 
              FROM recipe_materials rm 
              JOIN materials mat ON rm.mid = mat.mid 
              WHERE rm.item_id = ?";
$stmtRecipe = $conn->prepare($sqlRecipe);
$stmtRecipe->bind_param("i", $id);
$stmtRecipe->execute();
$resRecipe = $stmtRecipe->get_result();
while ($row = $resRecipe->fetch_assoc()) {
    $recipe[] = $row['mname'] . ": " . $row['quantity'] . " " . $row['unit'];
}

echo json_encode([
    "status" => "success", 
    "data" => [
        "info" => $dish,
        "names" => $names,
        "recipe" => $recipe
    ]
]);

$conn->close();
?>