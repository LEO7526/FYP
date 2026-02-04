<?php
// update_order_status.php - For updating order status in kitchen
require_once '../../conn.php';
header('Content-Type: application/json');

$oid = $_POST['oid'] ?? '';
$newStatus = $_POST['status'] ?? '';

if (empty($oid) || empty($newStatus)) {
    echo json_encode(["status" => "error", "message" => "Missing data"]);
    exit();
}

// Update orders table
$sql = "UPDATE orders SET ostatus = ? WHERE oid = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $newStatus, $oid);

if ($stmt->execute()) {
    
    // Sync table_orders status if needed
    // If order becomes Cooking (2) -> table status 'ordering'
    // If order becomes Delivered (3) -> table status 'available'
    if ($newStatus == 2) {
        $updateTableSql = "UPDATE table_orders SET status = 'ordering' WHERE oid = ?";
    } else if ($newStatus == 3) {
        $updateTableSql = "UPDATE table_orders SET status = 'available' WHERE oid = ?";
    }
    
    if (isset($updateTableSql)) {
        $stmtTable = $conn->prepare($updateTableSql);
        $stmtTable->bind_param("i", $oid);
        $stmtTable->execute();
    }
    
    echo json_encode(["status" => "success", "message" => "Order updated successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to update order"]);
}

$conn->close();
?>