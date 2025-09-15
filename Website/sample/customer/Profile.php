<?php
require_once '../auth_check.php';
check_customer_auth(); // Check customer authentication status

include '../conn.php'; // Database connection

// Get current logged-in customer ID from session
$cid = $_SESSION['customer']['cid'];

// Fetch customer details from database
$sql = "SELECT * FROM customer WHERE cid = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 1) {
    $customer = $result->fetch_assoc();

    // Calculate membership duration
    $joinDate = new DateTime(isset($customer['created_at']) ? $customer['created_at'] : 'now');
    $currentDate = new DateTime();
    $interval = $joinDate->diff($currentDate);
    $membershipDuration = $interval->format('%y years, %m months');
} else {
    // If customer not found, redirect to login
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
    <title>Profile | Smile & Sunshine Toy</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/form.css">
    <link rel="stylesheet" href="../CSS/profile.css">
</head>
<body>
<header>
    <div class="logo">
        <a href="Index.php">Smile &amp; Sunshine Toy</a>
    </div>
    <div class="user-actions">
        <a href="Index.php">Home</a>
        <a href="MyOrder.php">My Orders</a>
        <a href="Profile.php">My Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>

<div class="container">
    <div class="profile-header">
        <div class="profile-info">
            <h2><?php echo htmlspecialchars($customer['cname']); ?></h2>
        </div>
    </div>

    <div class="form-section">
        <h3>Account Information</h3>
        <form id="profileForm">
            <div class="form-group">
                <label class="form-label">Customer ID</label>
                <p class="static-field"><?php echo htmlspecialchars($customer['cid']); ?></p>
            </div>

            <div class="form-group">
                <label class="form-label">Full Name</label>
                <p class="static-field"><?php echo htmlspecialchars($customer['cname']); ?></p>
            </div>


            <div class="form-group">
                <label class="form-label">Company Name</label>
                <p class="static-field"><?php echo isset($customer['company']) ? htmlspecialchars($customer['company']) : 'Not provided'; ?></p>
            </div>

            <div class="form-group">
                <label class="form-label">Phone Number</label>
                <p class="static-field"><?php echo isset($customer['ctel']) ? htmlspecialchars($customer['ctel']) : 'Not provided'; ?></p>
            </div>

            <div class="form-group">
                <label class="form-label">Delivery Address</label>
                <p class="static-field"><?php echo isset($customer['caddr']) ? htmlspecialchars($customer['caddr']) : 'Not provided'; ?></p>
            </div>

            <div class="form-actions">
                <a href="UpdateProfile.php" class="btn-submit">Update</a>
            </div>
        </form>
    </div>
</div>
</body>
</html>