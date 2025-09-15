<?php
require_once '../auth_check.php';
check_staff_auth(); // Check staff authentication status

include '../conn.php'; // Database connection

// Get current logged-in staff ID
$sid = $_SESSION['staff']['sid'];

// Fetch staff details from database
$sql = "SELECT * FROM staff WHERE sid = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $sid);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 1) {
    $staff = $result->fetch_assoc();
} else {
    // If staff not found, redirect to login
    header('Location: ../loginChoose.html');
    exit();
}

$stmt->close();
$conn->close();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile - Smile & Sunshine Toy</title>
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/profile.css">
    <link rel="stylesheet" href="../CSS/header.css">
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
    <h1>Profile</h1>
    <div class="profile-header">
        <div class="profile-info">
            <h2><?php echo htmlspecialchars($staff['sname']); ?></h2>
            <p><?php echo htmlspecialchars($staff['srole']); ?></p>
        </div>
    </div>
    <div class="form-section">
        <h3>User Details</h3>
        <div class="form-group">
            <label>User ID:</label>
            <p><?php echo htmlspecialchars($staff['sid']); ?></p>
        </div>
        <div class="form-group">
            <label>Name:</label>
            <p><?php echo htmlspecialchars($staff['sname']); ?></p>
        </div>
        <div class="form-group">
            <label>Role:</label>
            <p><?php echo htmlspecialchars($staff['srole']); ?></p>
        </div>
        <div class="form-group">
            <label>Contact Number:</label>
            <p><?php echo htmlspecialchars($staff['stel']); ?></p>
        </div>
    </div>
</div>
</body>
</html>