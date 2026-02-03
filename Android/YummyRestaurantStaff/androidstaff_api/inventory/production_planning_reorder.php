<?php
require_once 'db_config.php';
// 注意：這裡假設你已經執行了上面的 ALTER TABLE 語句添加了 min_producible_qty
$sql = "SELECT item_id, min_producible_qty FROM menu_item WHERE is_available = 1";
$res = mysqli_query($conn, $sql);

while ($item = mysqli_fetch_assoc($res)) {
    $itemId = $item['item_id'];
    // 檢查 recipe_materials
    $checkSql = "SELECT rm.mid, rm.quantity, m.mqty, m.mname 
                 FROM recipe_materials rm JOIN materials m ON rm.mid = m.mid 
                 WHERE rm.item_id = ?";
    $stmt = mysqli_prepare($conn, $checkSql);
    mysqli_stmt_bind_param($stmt, "i", $itemId);
    mysqli_stmt_execute($stmt);
    $ingredients = mysqli_stmt_get_result($stmt);

    // 計算當前原料還能做多少份... (後續邏輯同上)
}
echo "Production planning scan finished.";
?>