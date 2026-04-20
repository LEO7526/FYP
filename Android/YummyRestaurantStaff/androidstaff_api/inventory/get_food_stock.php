<?php
header('Content-Type: application/json');
require_once 'db_config.php';

$response = ['success' => false, 'data' => []];

// 1. 獲取所有菜品 (硬編碼 min_producible_qty 為 10 作為預警門檻)
$sql_items = "SELECT mi.item_id, tr.item_name, 10 AS min_producible_qty 
              FROM menu_item mi 
              JOIN menu_item_translation tr ON mi.item_id = tr.item_id 
              WHERE tr.language_code = 'en' AND mi.is_available = 1";
$res_items = mysqli_query($conn, $sql_items);

// 2. 獲取原料庫存映射
$materials_stock = [];
$res_m = mysqli_query($conn, "SELECT mid, mqty FROM materials");
while($row = mysqli_fetch_assoc($res_m)) {
    $materials_stock[$row['mid']] = $row['mqty'];
}

$food_stock_data = [];

while ($item = mysqli_fetch_assoc($res_items)) {
    $itemId = $item['item_id'];
    
    // 3. 查詢新表名 recipe_materials 
    $sql_rec = "SELECT mid, quantity FROM recipe_materials WHERE item_id = $itemId";
    $res_rec = mysqli_query($conn, $sql_rec);
    
    $producible = PHP_INT_MAX;
    $has_recipe = false;

    while ($ing = mysqli_fetch_assoc($res_rec)) {
        $has_recipe = true;
        $stock = $materials_stock[$ing['mid']] ?? 0;
        $needed = $ing['quantity'];
        if ($needed > 0) {
            $producible = min($producible, floor($stock / $needed));
        }
    }

    if ($has_recipe) {
        $producible_count = ($producible == PHP_INT_MAX) ? 0 : (int)$producible;
        $min_required = (int)$item['min_producible_qty'];

        // 【關鍵優化】：只有當「能做的份數」低於「最低門檻」時才加入列表
        if ($producible_count < $min_required) {
            $food_stock_data[] = [
                'item_id' => $itemId,
                'item_name' => $item['item_name'],
                'producible_qty' => $producible_count,
                'min_producible_qty' => $min_required
            ];
        }
    }
}

$response['success'] = true;
$response['data'] = $food_stock_data;
echo json_encode($response);
?>