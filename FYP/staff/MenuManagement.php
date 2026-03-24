<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yummy Restaurant - Menu Management</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/MenuManagement.css">
    <link rel="stylesheet" href="../CSS/staff-index-fix.css">
</head>

<body>
<?php include 'header.php'; ?>

<div class="container">
    <h1>Menu Management</h1>
    <div class="menu-cards">
        <a href="newCoupon.php" class="menu-card">
            <div class="card-image">
                <img src="https://raw.githubusercontent.com/LEO7526/FYP/main/Image/logo/coupon.jpg" alt="Create New Coupon">
            </div>
            <div class="card-content">
                <h3>Create New Coupon</h3>
                <p>Design exclusive promotions and discounts.</p>
            </div>
        </a>

        <a href="newDishes.php" class="menu-card">
            <div class="card-image">
                <img src="https://raw.githubusercontent.com/LEO7526/FYP/main/Image/logo/dish.jpg" alt="Add New Dish">
            </div>
            <div class="card-content">
                <h3>Add New Dish</h3>
                <p>Introduce delicious dishes to your menu.</p>
            </div>
        </a>

        <a href="newPackage.php" class="menu-card">
            <div class="card-image">
                <img src="https://raw.githubusercontent.com/LEO7526/FYP/main/Image/logo/package.jpg" alt="Create New Package">
            </div>
            <div class="card-content">
                <h3>Create New Package</h3>
                <p>Bundle meals for greater value.</p>
            </div>
        </a>
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