<?php
// process_cash_payment.php - 處理現金支付確認
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Database connection
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "ProjectDB";

// 讀取 JSON 輸入
$input = json_decode(file_get_contents("php://input"), true);

try {
    $conn = new mysqli($servername, $username, $password, $dbname);
    
    if ($conn->connect_error) {
        throw new Exception("Database connection failed: " . $conn->connect_error);
    }
    
    $oid = intval($input['order_id'] ?? 0);
    $staff_id = intval($input['staff_id'] ?? 0);
    
    if ($oid <= 0) {
        throw new Exception("無效的訂單ID");
    }
    
    // 開始交易
    $conn->begin_transaction();
    
    // 1. 檢查訂單狀態
    $checkStmt = $conn->prepare("
        SELECT ostatus, payment_method, table_number 
        FROM orders 
        WHERE oid = ?
    ");
    $checkStmt->bind_param("i", $oid);
    $checkStmt->execute();
    $result = $checkStmt->get_result();
    
    if ($result->num_rows === 0) {
        throw new Exception("訂單不存在");
    }
    
    $order = $result->fetch_assoc();
    
    // 檢查是否為現金支付且狀態為待付款
    if ($order['payment_method'] !== 'cash') {
        throw new Exception("此訂單不是現金支付");
    }
    
    if ($order['ostatus'] != 0) {
        throw new Exception("訂單狀態不正確，無法確認");
    }
    
    // 2. 更新訂單狀態為正式訂單 (ostatus=1)
    $updateStmt = $conn->prepare("
        UPDATE orders 
        SET ostatus = 1, 
            note = CONCAT(
                IFNULL(note, ''), 
                CASE WHEN note IS NOT NULL AND note != '' THEN '\n' ELSE '' END,
                'Cash payment confirmed - Staff ID: ', ?, ' Time: ', NOW()
            )
        WHERE oid = ?
    ");
    $updateStmt->bind_param("ii", $staff_id, $oid);
    
    if (!$updateStmt->execute()) {
        throw new Exception("更新訂單狀態失敗");
    }
    
    // 3. 更新 table_orders 表（如果存在）
    $table_number = $order['table_number'];
    if ($table_number > 0) {
        // 檢查是否已存在 table_orders 記錄
        $checkTableStmt = $conn->prepare("
            SELECT toid FROM table_orders 
            WHERE table_number = ? AND oid = ?
        ");
        $checkTableStmt->bind_param("ii", $table_number, $oid);
        $checkTableStmt->execute();
        $tableResult = $checkTableStmt->get_result();
        
        if ($tableResult->num_rows === 0) {
            // 如果不存在，創建新記錄
            $insertTableStmt = $conn->prepare("
                INSERT INTO table_orders (table_number, oid, staff_id, created_at) 
                VALUES (?, ?, ?, NOW())
            ");
            $insertTableStmt->bind_param("iii", $table_number, $oid, $staff_id);
            $insertTableStmt->execute();
        } else {
            // 如果存在，更新員工ID
            $updateTableStmt = $conn->prepare("
                UPDATE table_orders 
                SET staff_id = ? 
                WHERE table_number = ? AND oid = ?
            ");
            $updateTableStmt->bind_param("iii", $staff_id, $table_number, $oid);
            $updateTableStmt->execute();
        }
    }
    
    // 提交交易
    $conn->commit();
    
    echo json_encode([
        'success' => true,
        'message' => '現金支付確認成功',
        'oid' => $oid,
        'table_number' => $table_number
    ]);
    
} catch (Exception $e) {
    // 回滾交易
    if (isset($conn)) {
        $conn->rollback();
    }
    
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
}

if (isset($conn)) {
    $conn->close();
}
?>