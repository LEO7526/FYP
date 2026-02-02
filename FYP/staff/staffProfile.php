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
    header('Location: ../staffLogin.html');
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
    <div class="hamburger-menu" id="hamburgerMenu">
        <span></span>
        <span></span>
        <span></span>
    </div>
    <div class="logo">
        <a href="staffIndex.php">Yummy Restaurant</a>
    </div>
    <nav class="main-nav">
        <a href="MenuManagement.php" class="nav-button insert-items">Menu Management</a>
        <a href="newInventory.php" class="nav-button insert-materials">Inventory</a>
        <a href="bookingList.php" class="nav-button order-list">Reservations</a>
        <a href="salesReport.php" class="nav-button report">Sales Reports</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php" class="profile-btn">Profile</a>
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
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const hamburger = document.getElementById('hamburgerMenu');
        const mainNav = document.querySelector('.main-nav');
        const userActions = document.querySelector('.user-actions');

        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            mainNav.classList.toggle('active');
            document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
        });

        document.querySelectorAll('.nav-button').forEach(button => {
            button.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        document.querySelectorAll('.user-actions a').forEach(link => {
            link.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        document.addEventListener('click', function(e) {
            if (!hamburger.contains(e.target) && !mainNav.contains(e.target) && !userActions.contains(e.target)) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && mainNav.classList.contains('active')) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
    });
</script>
</body>
</html>