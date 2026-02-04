<?php
// get_cash_payment_tables.php - 獲取待確認現金支付的桌位
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

// Database connection
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "ProjectDB";

try {
    $conn = new mysqli($servername, $username, $password, $dbname);
    
    if ($conn->connect_error) {
        throw new Exception("Database connection failed: " . $conn->connect_error);
    }
    
    // 查詢現金支付且狀態為待付款的訂單 (ostatus=0)
    $sql = "SELECT DISTINCT
                o.oid,
                o.table_number,
                o.odate,
                c.cname as customer_name,
                -- 計算訂單總金額
                (SELECT SUM(mi.item_price * oi.qty) 
                 FROM order_items oi 
                 JOIN menu_item mi ON oi.item_id = mi.item_id 
                 WHERE oi.oid = o.oid) as total_amount,
                -- 取得菜色摘要
                (SELECT GROUP_CONCAT(
                    CONCAT(
                        COALESCE(mit.item_name, CONCAT('Item #', mi.item_id)), 
                        ' x', oi.qty
                    ) 
                    ORDER BY oi.item_id 
                    SEPARATOR ', '
                 )
                 FROM order_items oi 
                 JOIN menu_item mi ON oi.item_id = mi.item_id
                 LEFT JOIN menu_item_translation mit 
                    ON oi.item_id = mit.item_id 
                    AND mit.language_code = 'en'
                 WHERE oi.oid = o.oid) as items_summary
            FROM orders o
            LEFT JOIN customer c ON o.cid = c.cid
            WHERE o.payment_method = 'cash'
              AND o.ostatus = 0  -- 待付款狀態
              AND o.table_number IS NOT NULL
            ORDER BY o.odate ASC";
    
    $result = $conn->query($sql);
    
    if (!$result) {
        throw new Exception("查詢失敗: " . $conn->error);
    }
    
    $tables = [];
    while ($row = $result->fetch_assoc()) {
        $tables[] = [
            'table_number' => (int)$row['table_number'],
            'oid' => (int)$row['oid'],
            'customer_name' => $row['customer_name'] ?: '匿名客戶',
            'order_time' => $row['odate'],
            'total_amount' => (float)$row['total_amount'],
            'items_summary' => $row['items_summary'] ?: '無菜色資訊'
        ];
    }
    
    echo json_encode([
        'status' => 'success',
        'data' => $tables,
        'count' => count($tables)
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        'status' => 'error',
        'message' => $e->getMessage()
    ]);
}

if (isset($conn)) {
    $conn->close();
}
?>