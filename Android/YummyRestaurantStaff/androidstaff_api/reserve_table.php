<?php
// reserve_table.php
require_once 'db_connect.php';
header('Content-Type: application/json');

$tableNumber = $_POST['table_number'] ?? '';

if (empty($tableNumber)) {
    echo json_encode(["status" => "error", "message" => "Missing table number"]);
    exit();
}

// 檢查該桌是否已經有人 (避免重複入座)
$checkSql = "SELECT status FROM table_orders WHERE table_number = ? ORDER BY toid DESC LIMIT 1";
$stmt = $conn->prepare($checkSql);
$stmt->bind_param("i", $tableNumber);
$stmt->execute();
$res = $stmt->get_result();

if ($row = $res->fetch_assoc()) {
    $currentStatus = $row['status'];
    if ($currentStatus != 'available' && $currentStatus != 'paid') {
        echo json_encode(["status" => "error", "message" => "Table is already occupied!"]);
        exit();
    }
}

// 插入新狀態 'reserved' (代表已訂位/入座)
// 你也可以根據需求改成 'seated'
$insertSql = "INSERT INTO table_orders (table_number, status, created_at) VALUES (?, 'reserved', NOW())";
$stmt2 = $conn->prepare($insertSql);
$stmt2->bind_param("i", $tableNumber);

if ($stmt2->execute()) {
    echo json_encode(["status" => "success"]);
} else {
    echo json_encode(["status" => "error", "message" => "Update failed"]);
}

$conn->close();
?>