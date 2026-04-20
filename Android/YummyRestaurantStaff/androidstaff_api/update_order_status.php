<?php
// update_order_status.php
require_once 'db_connect.php';
header('Content-Type: application/json');

$oid = $_POST['oid'] ?? '';
$newStatus = $_POST['status'] ?? ''; // 接收新的狀態 (2 or 3)

if (empty($oid) || empty($newStatus)) {
    echo json_encode(["status" => "error", "message" => "Missing data"]);
    exit();
}

// 更新 orders 表
$sql = "UPDATE orders SET ostatus = ? WHERE oid = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $newStatus, $oid);

if ($stmt->execute()) {
    
    // 同步更新 table_orders 的狀態 (這是為了讓桌位圖也能變色)
    // 如果訂單變成 Cooking (2) -> 桌子狀態 'ordering'
    // 如果訂單變成 Delivered (3) -> 桌子狀態 'ready_to_pay' (或是保持 ordering，看你邏輯)
    
    $tableStatus = '';
    if ($newStatus == 2) $tableStatus = 'ordering';
    if ($newStatus == 3) $tableStatus = 'ready_to_pay'; // 假設上完菜就是等待結帳

    if ($tableStatus != '') {
        $sql2 = "UPDATE table_orders SET status = ? WHERE oid = ?";
        $stmt2 = $conn->prepare($sql2);
        $stmt2->bind_param("si", $tableStatus, $oid);
        $stmt2->execute();
    }

    echo json_encode(["status" => "success"]);
} else {
    echo json_encode(["status" => "error", "message" => "Update failed"]);
}

$stmt->close();
$conn->close();
?>