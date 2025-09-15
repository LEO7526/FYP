<?php
require_once '../auth_check.php';
check_staff_auth(); // Check staff authentication status
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Dashboard - Smile & Sunshine Toy</title>
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/dashboard.css">
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
    <h1>Staff Dashboard</h1>
    <div class="dashboard-buttons">
        <a href="InsertItems.php" class="dashboard-button" style="background-color: #4CAF50;">
            <span>Insert Items</span>
        </a>
        <a href="InsertMaterials.php" class="dashboard-button" style="background-color: #2196F3;">
            <span>Insert Materials</span>
        </a>
        <a href="OrderList.php" class="dashboard-button" style="background-color: #FFC107;">
            <span>Order List</span>
        </a>
        <a href="Report.php" class="dashboard-button" style="background-color: #FF5722;">
            <span>Report</span>
        </a>
        <a href="Delete.php" class="dashboard-button" style="background-color: #f44336;">
            <span>Delete</span>
        </a>
    </div>
</div>
</body>
</html>