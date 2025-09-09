<?php
session_start();
require_once '../auth_check.php';
check_staff_auth(); // Verify staff authentication

if (!isset($_GET['pid'])) {
    $_SESSION['delete_error'] = "Invalid product ID";
    header('Location: Delete.php');
    exit();
}

$pid = $_GET['pid'];

include '../conn.php';

// Check if product has orders
$checkSql = "SELECT COUNT(*) as order_count FROM orders WHERE pid = '$pid'";
$checkResult = mysqli_query($conn, $checkSql);
$row = mysqli_fetch_assoc($checkResult);

if ($row['order_count'] > 0) {
    $_SESSION['delete_error'] = "Cannot delete product with existing orders";
    header('Location: Delete.php');
    exit();
}

// Start transaction to ensure data integrity
mysqli_begin_transaction($conn);


// First, delete related records in prodmat table
$deleteProdmatSql = "DELETE FROM prodmat WHERE pid = '$pid'";
if (!mysqli_query($conn, $deleteProdmatSql)) {
    throw new Exception("Error deleting product materials: " . mysqli_error($conn));
}

// Then delete the product itself
$deleteProductSql = "DELETE FROM product WHERE pid = '$pid'";
if (!mysqli_query($conn, $deleteProductSql)) {
    throw new Exception("Error deleting product: " . mysqli_error($conn));
}

// Commit the transaction if both queries succeed
mysqli_commit($conn);
header('Location: StaffComplete.php');
exit();



mysqli_close($conn);
?>