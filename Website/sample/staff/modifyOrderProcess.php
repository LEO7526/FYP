<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Get form data
    $oid = intval($_POST['oid']);
    $originalQty = intval($_POST['original_qty']);
    $newQty = intval($_POST['newOrderQuantity']);
    $unitCost = floatval($_POST['unit_cost']);

    // Get form data
    mysqli_begin_transaction($conn);

    try {
        // 1. Get order information
        $sql = "SELECT pid, ostatus FROM orders WHERE oid = $oid";
        $order = mysqli_fetch_assoc(mysqli_query($conn, $sql));
        $pid = $order['pid'];

        // 2. Update the reserved quantities for all materials（mrqty）
        $updateReservesSql = "UPDATE material m
                              JOIN (
                                  SELECT mid, SUM(pmqty * order_qty) AS total_reserve
                                  FROM (
                                      SELECT pm.mid, pm.pmqty, o.oqty AS order_qty
                                      FROM prodmat pm
                                      JOIN orders o ON pm.pid = o.pid
                                      WHERE o.ostatus != 0
                                  ) AS reserve_data
                                  GROUP BY mid
                              ) AS reserves ON m.mid = reserves.mid
                              SET m.mrqty = reserves.total_reserve";
        mysqli_query($conn, $updateReservesSql);

        // 3. Calculate the maximum available quantity
        $maxQuantity = PHP_INT_MAX;
        $sql = "SELECT m.mid, m.mqty, m.mrqty, pm.pmqty
                FROM prodmat pm
                JOIN material m ON pm.mid = m.mid
                WHERE pm.pid = $pid";
        $materialsResult = mysqli_query($conn, $sql);

        while ($material = mysqli_fetch_assoc($materialsResult)) {
            $available = $material['mqty'] - $material['mrqty'];
            $maxForMaterial = floor($available / $material['pmqty']) + $originalQty;

            if ($maxForMaterial < $maxQuantity) {
                $maxQuantity = $maxForMaterial;
            }
        }

        // 4. Verify that the new quantity is valid
        if ($newQty < 1 || $newQty > $maxQuantity) {
            throw new Exception("Invalid quantity. Maximum available: $maxQuantity");
        }

        // 5. Update order quantity
        $newTotalCost = $unitCost * $newQty;
        $updateOrderSql = "UPDATE orders 
                           SET oqty = $newQty, 
                               ocost = $newTotalCost 
                           WHERE oid = $oid";
        mysqli_query($conn, $updateOrderSql);

        // 6. Update material reservation quantity（mrqty）
        mysqli_query($conn, $updateReservesSql);

        // Committing a transaction
        mysqli_commit($conn);

        // Redirect to success page
        header("Location: StaffComplete.php?action=modify&oid=$oid");
        exit();
    } catch (Exception $e) {
        // Rollback Transaction
        mysqli_rollback($conn);

        // Store error information and redirect back to the edit page
        session_start();
        $_SESSION['modify_order_error'] = $e->getMessage();
        header("Location: ModifyOrder.php?oid=$oid");
        exit();
    }
}
?>