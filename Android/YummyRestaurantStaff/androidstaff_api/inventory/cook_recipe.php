<?php
header('Content-Type: application/json');
require_once 'db_config.php';

$response = ['success' => false, 'message' => 'Invalid request.'];
$data = json_decode(file_get_contents('php://input'));

if (isset($data->item_id) && isset($data->quantity_cooked)) {
    $itemId = (int)$data->item_id;
    $quantityCooked = (int)$data->quantity_cooked;

    // 1. 開始交易 (Transaction)
    mysqli_begin_transaction($conn);

    try {
        // 2. 獲取這道菜的食譜 (注意：表名改為 recipe_materials, 欄位改為 mid)
        $sql_recipe = "SELECT mid, quantity FROM recipe_materials WHERE item_id = ?";
        $stmt_recipe = mysqli_prepare($conn, $sql_recipe);
        mysqli_stmt_bind_param($stmt_recipe, "i", $itemId);
        mysqli_stmt_execute($stmt_recipe);
        $result_recipe = mysqli_stmt_get_result($stmt_recipe);

        if (mysqli_num_rows($result_recipe) == 0) {
            throw new Exception("No recipe found for this item ID: " . $itemId);
        }

        // 3. 遍歷原料並扣除
        while ($ingredient = mysqli_fetch_assoc($result_recipe)) {
            $mid = $ingredient['mid'];
            $totalConsumed = $ingredient['quantity'] * $quantityCooked;
            
            // 更新 materials 表 (欄位名 mqty)
            $sql_update = "UPDATE materials SET mqty = mqty - ? WHERE mid = ? AND mqty >= ?";
            $stmt_update = mysqli_prepare($conn, $sql_update);
            mysqli_stmt_bind_param($stmt_update, "did", $totalConsumed, $mid, $totalConsumed);
            mysqli_stmt_execute($stmt_update);
            
            if (mysqli_stmt_affected_rows($stmt_update) == 0) {
                // 如果扣除失敗（可能是庫存不足），拋出異常
                throw new Exception("Insufficient stock for Material ID: " . $mid);
            }
        }

        mysqli_commit($conn);
        $response['success'] = true;
        $response['message'] = "Stock deducted successfully!";

    } catch (Exception $e) {
        mysqli_rollback($conn);
        $response['message'] = $e->getMessage();
    }
}

mysqli_close($conn);
echo json_encode($response);
?>