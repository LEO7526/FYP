<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

if (isset($_GET['oid']) && isset($_GET['action'])) {
    $oid = intval($_GET['oid']);
    $action = $_GET['action'];

    // Get order details
    $sql = "SELECT pid, oqty, ostatus FROM orders WHERE oid = $oid";
    $order = mysqli_fetch_assoc(mysqli_query($conn, $sql));
    $pid = $order['pid'];
    $oqty = $order['oqty'];
    $currentStatus = $order['ostatus'];

    if ($action === 'accept') {
        // Check if material inventory is sufficient
        $sql = "SELECT m.mid, m.mname, m.mqty, m.mrqty, pm.pmqty 
                FROM prodmat pm
                JOIN material m ON pm.mid = m.mid
                WHERE pm.pid = $pid";
        $materials = mysqli_query($conn, $sql);

        $materialChecks = [];

        while ($material = mysqli_fetch_assoc($materials)) {
            $mid = $material['mid'];
            $pmqty = $material['pmqty'];

            $sql = "SELECT SUM(o.oqty * pm.pmqty) AS total_reserve
                    FROM orders o
                    JOIN prodmat pm ON o.pid = pm.pid
                    WHERE o.ostatus != 0 
                    AND pm.mid = $mid
                    AND o.oid != $oid"; // 排除當前訂單
            $totalReserveResult = mysqli_fetch_assoc(mysqli_query($conn, $sql));
            $otherReserves = $totalReserveResult['total_reserve'] ?: 0;

            $newReserve = ($currentStatus != 0) ? $oqty * $pmqty : 0;
            $totalReserveNeeded = $otherReserves + $newReserve;

            if ($totalReserveNeeded > $material['mqty']) {
                $materialChecks[] = [
                    'name' => $material['mname'],
                    'needed' => $totalReserveNeeded,
                    'available' => $material['mqty'],
                    'unit' => $material['munit']
                ];
            }
        }

        if (!empty($materialChecks)) {
            $errorMessage = "Cannot accept order due to insufficient material stock:<br>";
            foreach ($materialChecks as $material) {
                $errorMessage .= "- {$material['name']}: Needed {$material['needed']} {$material['unit']}, ";
                $errorMessage .= "Available {$material['available']} {$material['unit']}<br>";
            }

            session_start();
            $_SESSION['accept_order_error'] = $errorMessage;
            header("Location: StaffOrderDetails.php?oid=$oid");
            exit();
        }

        // Update order status to 3 (Approved)
        $updateSql = "UPDATE orders SET ostatus = 3 WHERE oid = $oid";
        mysqli_query($conn, $updateSql);

        header("Location: StaffComplete.php?action=accept&oid=$oid");
        exit();
    } elseif ($action === 'reject') {
        // Release reserved materials
        $sql = "SELECT mid, pmqty FROM prodmat WHERE pid = $pid";
        $materials = mysqli_query($conn, $sql);

        while ($material = mysqli_fetch_assoc($materials)) {
            $mid = $material['mid'];
            $reserveQty = $oqty * $material['pmqty'];

            $updateSql = "UPDATE material 
                          SET mrqty = mrqty - $reserveQty 
                          WHERE mid = $mid";
            mysqli_query($conn, $updateSql);
        }

        // Update order status to 0 (Rejected)
        $updateSql = "UPDATE orders SET ostatus = 0 WHERE oid = $oid";
        mysqli_query($conn, $updateSql);

        header("Location: StaffComplete.php?action=reject&oid=$oid");
        exit();
    }
}

// If the required parameters are not provided, an error is returned.
header("Location: OrderList.php");
exit();
?>