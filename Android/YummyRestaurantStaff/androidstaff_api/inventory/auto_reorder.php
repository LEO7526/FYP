<?php
header('Content-Type: application/json');
require_once 'db_config.php';

$response = ['success' => false, 'message' => ''];
$reordered_items = [];

// 1. 找出庫存低於或等於 reorderLevel 的原料
$sql = "SELECT mid, mname, mqty, reorderLevel, unit FROM materials WHERE mqty <= reorderLevel";
$result = mysqli_query($conn, $sql);

if ($result && mysqli_num_rows($result) > 0) {
    mysqli_begin_transaction($conn);
    try {
        while ($m = mysqli_fetch_assoc($result)) {
            $mid = $m['mid'];
            $mname = $m['mname'];
            $unit = $m['unit'];
            
            // 2. 補貨邏輯：補到安全水位的兩倍
            $addAmount = $m['reorderLevel'] * 2;
            
            // 3. 更新庫存
            $updSql = "UPDATE materials SET mqty = mqty + ? WHERE mid = ?";
            $updStmt = mysqli_prepare($conn, $updSql);
            mysqli_stmt_bind_param($updStmt, "di", $addAmount, $mid);
            mysqli_stmt_execute($updStmt);
            
            // 4. 寫入消耗歷史紀錄 (配合新 Schema 包含 mid)
            $details = "Auto-reorder: Added $addAmount $unit for $mname";
            $logSql = "INSERT INTO consumption_history (mid, log_date, log_type, details) VALUES (?, CURDATE(), 'Reorder', ?)";
            $logStmt = mysqli_prepare($conn, $logSql);
            mysqli_stmt_bind_param($logStmt, "is", $mid, $details);
            mysqli_stmt_execute($logStmt);
            
            $reordered_items[] = $mname;
        }
        mysqli_commit($conn);
        $response['success'] = true;
        $response['message'] = "Automatically reordered: " . implode(", ", $reordered_items);
    } catch (Exception $e) {
        mysqli_rollback($conn);
        $response['message'] = "Error: " . $e->getMessage();
    }
} else {
    $response['success'] = true;
    $response['message'] = "All items are in safe stock levels.";
}

mysqli_close($conn);
echo json_encode($response);
?>