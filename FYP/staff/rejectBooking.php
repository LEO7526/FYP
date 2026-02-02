<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

if (isset($_GET['oid'])) {
    $oid = intval($_GET['oid']);

    // Get order details
    $sql = "SELECT pid, oqty FROM orders WHERE oid = $oid";
    $order = mysqli_fetch_assoc(mysqli_query($conn, $sql));
    $pid = $order['pid'];
    $oqty = $order['oqty'];

    // Get materials used in this product
    $sql = "SELECT mid, pmqty FROM prodmat WHERE pid = $pid";
    $materials = mysqli_query($conn, $sql);

    // Release reserved materials
    while ($material = mysqli_fetch_assoc($materials)) {
        $mid = $material['mid'];
        $reserveQty = $oqty * $material['pmqty'];

        $updateSql = "UPDATE material 
                      SET mrqty = mrqty - $reserveQty 
                      WHERE mid = $mid";
        mysqli_query($conn, $updateSql);
    }

    // Update order status to Rejected (status 4)
    $sql = "UPDATE orders SET ostatus = 4 WHERE oid = $oid";
    mysqli_query($conn, $sql);

    // Redirect to complete page with success message
    header("Location: StaffComplete.php?action=reject&oid=$oid");
    exit();
}
?>