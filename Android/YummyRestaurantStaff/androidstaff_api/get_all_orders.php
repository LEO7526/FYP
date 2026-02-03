<?php
// get_all_orders.php
require_once 'db_connect.php';
header('Content-Type: application/json');

// 抓取 oid, odate, ostatus, order_type, table_number
$sql = "SELECT oid, odate, ostatus, order_type, table_number 
        FROM orders 
        WHERE ostatus IN (1, 2, 3) 
        ORDER BY odate DESC";

$result = $conn->query($sql);
$orders = array();

while ($row = $result->fetch_assoc()) {
    $oid = $row['oid'];
    
    // 抓取菜色
    $itemSql = "SELECT mit.item_name, oi.qty 
                FROM order_items oi
                JOIN menu_item_translation mit ON oi.item_id = mit.item_id
                WHERE oi.oid = ? AND mit.language_code = 'en'";
    $stmtItem = $conn->prepare($itemSql);
    $stmtItem->bind_param("i", $oid);
    $stmtItem->execute();
    $resItem = $stmtItem->get_result();
    
    $summaryList = [];
    while ($item = $resItem->fetch_assoc()) {
        $summaryList[] = $item['qty'] . "x " . $item['item_name'];
    }
    
    $summaryString = implode("\n", $summaryList);
    if (empty($summaryString)) $summaryString = "Checking items...";

    // 處理桌號顯示邏輯
    $tableDisplay = "";
    if ($row['order_type'] == 'takeaway') {
        $tableDisplay = "Takeaway";
    } else {
        $tableDisplay = "Table " . ($row['table_number'] ?? "?");
    }

    $orders[] = [
        "oid" => $row['oid'],
        "table_number" => $tableDisplay, // 直接傳處理好的字串
        "odate" => $row['odate'],
        "ostatus" => $row['ostatus'],
        "type" => $row['order_type'],    // 新增類型
        "summary" => $summaryString
    ];
}

echo json_encode(["status" => "success", "data" => $orders]);
$conn->close();
?>