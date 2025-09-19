<?php
require_once '../auth_check.php';
check_staff_auth(); // Check staff authentication status

$page = isset($_GET['page']) ? $_GET['page'] : 'staffIndex.php';
$action = isset($_GET['action']) ? $_GET['action'] : '';
$bid = isset($_GET['bid']) ? $_GET['bid'] : '';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Complete Page</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/complete.css">
</head>
<body>
<header>
    <div class="logo">
        <a href="staffIndex.php">Yummy Restaurant</a>
    </div>

    <nav class="main-nav">
        <a href="MenuManagement.php" class="nav-button insert-items">Menu Management</a>
        <a href="Inventory.php" class="nav-button insert-materials">Inventory</a>
        <a href="ReservationList.php" class="nav-button order-list">Reservations</a>
        <a href="SalesReport.php" class="nav-button report">Sales Reports</a>
        <a href="PurchaseReturn.php" class="nav-button delete">Purchase & Return</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php">Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>
<div class="container">
    <div class="complete-text">Complete</div>
    <div class="thank-you-text">Data already updated</div>
    <div class="link-text">Click <a href="<?= htmlspecialchars($page) ?>" class="back-link-text">here</a> to go back to <?php echo $page === 'bookingList.php' ? 'Booking List' : 'home page' ?></div>
</div>
</body>
</html>