<?php
// get_takeaway_cash_orders.php - 獲取待確認現金支付的外帶訂單
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
    
    // 查詢現金支付且狀態為待付款的外帶訂單 (ostatus=0, order_type='takeaway')
    $sql = "SELECT DISTINCT
                o.oid,
                o.orderRef,
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
              AND o.order_type = 'takeaway'  -- 只取外帶訂單
            ORDER BY o.odate ASC";
    
    $result = $conn->query($sql);
    
    if ($result === false) {
        throw new Exception("Query failed: " . $conn->error);
    }
    
    $orders = [];
    while ($row = $result->fetch_assoc()) {
        $orders[] = [
            'oid' => intval($row['oid']),
            'orderRef' => $row['orderRef'],
            'customer_name' => $row['customer_name'] ?? 'Guest',
            'order_time' => $row['odate'],
            'total_amount' => floatval($row['total_amount'] ?? 0),
            'items_summary' => $row['items_summary'] ?? 'No items'
        ];
    }
    
    $response = [
        'status' => 'success',
        'message' => 'Takeaway cash orders retrieved successfully',
        'data' => $orders,
        'count' => count($orders)
    ];
    
    error_log("get_takeaway_cash_orders.php: Found " . count($orders) . " pending takeaway cash orders");
    
} catch (Exception $e) {
    error_log("get_takeaway_cash_orders.php error: " . $e->getMessage());
    $response = [
        'status' => 'error',
        'message' => $e->getMessage(),
        'data' => []
    ];
}

// Close connection
if (isset($conn)) {
    $conn->close();
}

echo json_encode($response);
?>