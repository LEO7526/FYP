<?php
require_once '../auth_check.php';
check_customer_auth();
include '../conn.php';

$oid = isset($_GET['oid']) ? (int)$_GET['oid'] : 0;
$cid = $_SESSION['customer']['cid'];

if (!$oid) {
    $_SESSION['error'] = "Invalid Order ID";
    header('Location: MyOrder.php');
    exit();
}

// Start transaction
$conn->begin_transaction();

try {
    // Verify order belongs to customer and get details
    $sql = "SELECT o.*, p.pid 
            FROM orders o
            JOIN product p ON o.pid = p.pid
            WHERE o.oid = ? AND o.cid = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ii", $oid, $cid);
    $stmt->execute();
    $result = $stmt->get_result();
    $order = $result->fetch_assoc();
    $stmt->close();

    if (!$order) {
        throw new Exception("Order not found or doesn't belong to you");
    }

    // Check if order can be cancelled (2 days before delivery)
    if ($order['odeliverdate']) {
        $deliveryDate = new DateTime($order['odeliverdate']);
        $currentDate = new DateTime();
        $diff = $currentDate->diff($deliveryDate);

        if ($diff->days <= 2 && $diff->invert == 0) {
            throw new Exception("Order can only be cancelled at least 2 days before delivery date");
        }
    }

    // Get materials used in this order
    $sql = "SELECT m.mid, pm.pmqty 
            FROM prodmat pm
            JOIN material m ON pm.mid = m.mid
            WHERE pm.pid = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $order['pid']);
    $stmt->execute();
    $materials = $stmt->get_result();
    $stmt->close();

    // Update material quantities (remove reserved quantities)
    while ($material = $materials->fetch_assoc()) {
        $usedQty = $order['oqty'] * $material['pmqty'];

        $updateSql = "UPDATE material 
                     SET mrqty = mrqty - ?
                     WHERE mid = ?";
        $stmt = $conn->prepare($updateSql);
        $stmt->bind_param("ii", $usedQty, $material['mid']);
        $stmt->execute();
        $stmt->close();
    }

    // DELETE the order record (not just update status)
    $deleteSql = "DELETE FROM orders WHERE oid = ?";
    $stmt = $conn->prepare($deleteSql);
    $stmt->bind_param("i", $oid);
    $stmt->execute();

    if ($stmt->affected_rows === 0) {
        throw new Exception("Failed to delete order");
    }
    $stmt->close();

    $conn->commit();

    $_SESSION['success'] = "Order #$oid has been successfully cancelled";
    header('Location: MyOrder.php');
    exit();

} catch (Exception $e) {
    $conn->rollback();
    $_SESSION['error'] = $e->getMessage();
    header('Location: OrderDetails_cus.php?oid='.$oid);
    exit();
}

$conn->close();
?>