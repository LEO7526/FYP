<?php
require_once '../auth_check.php';
check_staff_auth(); // Check staff authentication status
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
        <a href="staffIndex.php">Smile & Sunshine Toy</a>
    </div>

    <nav class="main-nav">
        <a href="InsertItems.php" class="nav-button insert-items">Insert Items</a>
        <a href="InsertMaterials.php" class="nav-button insert-materials">Insert Materials</a>
        <a href="OrderList.php" class="nav-button order-list">Order List</a>
        <a href="Report.php" class="nav-button report">Report</a>
        <a href="Delete.php" class="nav-button delete">Delete</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php">Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>
<div class="container">
    <div class="complete-text">Complete</div>
    <div class="thank-you-text">Data already updated</div>
    <div class="link-text">Click <a href="staffIndex.php" class="back-link-text">here</a> to go back to home page</div>
</div>
</body>
</html>