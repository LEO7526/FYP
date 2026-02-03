<?php
// get_order_details_by_id.php
require_once 'db_connect.php';
header('Content-Type: application/json');

$oid = $_GET['oid'] ?? 0;

if ($oid == 0) {
    echo json_encode(["status" => "error", "message" => "Missing Order ID"]);
    exit();
}

// 1. 抓取訂單基本資料 (包含備註 note)
$sql = "SELECT o.oid, o.odate, o.note, c.cname 
        FROM orders o
        LEFT JOIN customer c ON o.cid = c.cid
        WHERE o.oid = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $oid);
$stmt->execute();
$result = $stmt->get_result();
$orderData = $result->fetch_assoc();

if (!$orderData) {
    echo json_encode(["status" => "error", "message" => "Order not found"]);
    exit();
}

// 2. 抓取菜色內容
$items = [];
$sqlItems = "SELECT mit.item_name, oi.qty 
             FROM order_items oi
             JOIN menu_item_translation mit ON oi.item_id = mit.item_id
             WHERE oi.oid = ? AND mit.language_code = 'en'";
$stmtItems = $conn->prepare($sqlItems);
$stmtItems->bind_param("i", $oid);
$stmtItems->execute();
$resItems = $stmtItems->get_result();

while ($row = $resItems->fetch_assoc()) {
    $items[] = $row['qty'] . "x " . $row['item_name'];
}

// 3. 抓取套餐內容 (如果有)
$sqlPkg = "SELECT mp.package_name, op.qty 
           FROM order_packages op
           JOIN menu_package mp ON op.package_id = mp.package_id
           WHERE op.oid = ?";
$stmtPkg = $conn->prepare($sqlPkg);
$stmtPkg->bind_param("i", $oid);
$stmtPkg->execute();
$resPkg = $stmtPkg->get_result();

while ($pkg = $resPkg->fetch_assoc()) {
    $items[] = $pkg['qty'] . "x [Set] " . $pkg['package_name'];
}

echo json_encode([
    "status" => "success",
    "data" => [
        "oid" => $orderData['oid'],
        "customer" => $orderData['cname'] ?? "Walk-in",
        "time" => $orderData['odate'],
        "note" => $orderData['note'], // 這是重點，顯示顧客備註
        "items" => $items
    ]
]);

$conn->close();
?>