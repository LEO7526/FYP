<?php
header('Content-Type: application/json');
require_once 'db_config.php';

$yesterday = date('Y-m-d', strtotime('-1 day'));
$total_consumption = [];

// 1. 找出昨天的銷售
$sql_sales = "SELECT oi.item_id, SUM(oi.qty) as total_sold 
              FROM orders o JOIN order_items oi ON o.oid = oi.oid 
              WHERE DATE(o.odate) = ? GROUP BY oi.item_id";

$stmt = mysqli_prepare($conn, $sql_sales);
mysqli_stmt_bind_param($stmt, "s", $yesterday);
mysqli_stmt_execute($stmt);
$result = mysqli_stmt_get_result($stmt);

while ($row = mysqli_fetch_assoc($result)) {
    // 2. 查找食譜 (recipe_materials)
    $sql_rec = "SELECT mid, quantity FROM recipe_materials WHERE item_id = ?";
    $rStmt = mysqli_prepare($conn, $sql_rec);
    mysqli_stmt_bind_param($rStmt, "i", $row['item_id']);
    mysqli_stmt_execute($rStmt);
    $rRes = mysqli_stmt_get_result($rStmt);

    while ($ing = mysqli_fetch_assoc($rRes)) {
        $mid = $ing['mid'];
        $consumed = $row['total_sold'] * $ing['quantity'];
        if (!isset($total_consumption[$mid])) $total_consumption[$mid] = 0;
        $total_consumption[$mid] += $consumed;
    }
}

// 3. 寫入歷史日誌
if (!empty($total_consumption)) {
    foreach ($total_consumption as $mid => $qty) {
        $details = "Daily system deduction based on sales";
        $log = "INSERT INTO consumption_history (mid, log_date, log_type, details) VALUES (?, ?, 'Deduction', ?)";
        $lStmt = mysqli_prepare($conn, $log);
        $msg = $details . ": consumed $qty";
        mysqli_stmt_bind_param($lStmt, "iss", $mid, $yesterday, $msg);
        mysqli_stmt_execute($lStmt);
    }
}
echo json_encode(['success' => true, 'message' => 'Daily summary processed.']);
?>