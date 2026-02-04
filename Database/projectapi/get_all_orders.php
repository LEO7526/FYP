<?php
// get_all_orders.php - For kitchen staff to view all orders
require_once '../../conn.php';
header('Content-Type: application/json');

// Get all orders with status 1, 2, 3 (New, Cooking, Delivered)
$sql = "SELECT oid, odate, ostatus, order_type, table_number 
        FROM orders 
        WHERE ostatus IN (1, 2, 3) 
        ORDER BY odate DESC";

$result = $conn->query($sql);
$orders = array();

while ($row = $result->fetch_assoc()) {
    $oid = $row['oid'];
    
    // Get order items summary
    $itemSql = "SELECT mit.item_name, oi.qty 
                FROM order_items oi
                JOIN menu_item_translation mit ON oi.item_id = mit.item_id
                WHERE oi.oid = ? AND mit.language_code = 'en'";
    $stmtItem = $conn->prepare($itemSql);
    $stmtItem->bind_param("i", $oid);
    $stmtItem->execute();
    $resItem = $stmtItem->get_result();
    
    $summaryList = [];
    while ($itemRow = $resItem->fetch_assoc()) {
        $summaryList[] = $itemRow['qty'] . 'x ' . $itemRow['item_name'];
    }
    
    $summary = !empty($summaryList) ? implode("\n", $summaryList) : "No items found";
    
    $orders[] = [
        "oid" => $row['oid'],
        "table_number" => $row['table_number'] ?? "Takeaway",
        "odate" => $row['odate'],
        "ostatus" => $row['ostatus'],
        "summary" => $summary,
        "type" => $row['order_type'] ?? "dine_in"
    ];
}

echo json_encode([
    "status" => "success", 
    "data" => $orders
]);

$conn->close();
?>