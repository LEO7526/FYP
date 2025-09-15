
<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

if (isset($_GET['oid'])) {
    $oid = intval($_GET['oid']);

    $sql = "SELECT pid, oqty FROM orders WHERE oid = $oid";
    $order = mysqli_fetch_assoc(mysqli_query($conn, $sql));
    $pid = $order['pid'];
    $oqty = $order['oqty'];

    $sql = "SELECT m.mid, m.mname, m.mqty, pm.pmqty 
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
                WHERE o.ostatus != 4 
                AND pm.mid = $mid";
        $totalReserveResult = mysqli_fetch_assoc(mysqli_query($conn, $sql));
        $totalReserveNeeded = $totalReserveResult['total_reserve'] ?: 0;

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

    $sql = "UPDATE orders SET ostatus = 2 WHERE oid = $oid";
    mysqli_query($conn, $sql);

    header("Location: StaffComplete.php?action=accept&oid=$oid");
    exit();
}
?>