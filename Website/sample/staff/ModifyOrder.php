<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

$oid = isset($_GET['oid']) ? intval($_GET['oid']) : 0;

$errorMessage = isset($_SESSION['modify_order_error']) ? $_SESSION['modify_order_error'] : '';
unset($_SESSION['modify_order_error']); // Clear error message

// ===== 1. Update material reservation quantity (mrqty) =====
// Update the reserved quantity of all materials first
$updateReservesSql = "UPDATE material m
                      JOIN (
                          SELECT mid, SUM(pmqty * order_qty) AS total_reserve
                          FROM (
                              SELECT pm.mid, pm.pmqty, o.oqty AS order_qty
                              FROM prodmat pm
                              JOIN orders o ON pm.pid = o.pid
                              WHERE o.ostatus != 0
                          ) AS reserve_data
                          GROUP BY mid
                      ) AS reserves ON m.mid = reserves.mid
                      SET m.mrqty = reserves.total_reserve";
mysqli_query($conn, $updateReservesSql);

// ===== 2. Get order details =====
$sql = "SELECT o.*, p.pname
        FROM orders o
        JOIN product p ON o.pid = p.pid
        WHERE o.oid = $oid";
$order = mysqli_fetch_assoc(mysqli_query($conn, $sql));

// Get the name of the material used in this product
$sql = "SELECT m.mname, pm.pmqty
        FROM prodmat pm
        JOIN material m ON pm.mid = m.mid
        WHERE pm.pid = {$order['pid']}";
$materialsResult = mysqli_query($conn, $sql);
$materialNames = [];
while ($row = mysqli_fetch_assoc($materialsResult)) {
    $materialNames[] = $row['mname'];
}

// ===== 3. Calculate the maximum available quantity =====
$maxQuantity = PHP_INT_MAX;
$sql = "SELECT m.mid, m.mname, m.mqty, m.mrqty, pm.pmqty
        FROM prodmat pm
        JOIN material m ON pm.mid = m.mid
        WHERE pm.pid = {$order['pid']}";
$materialsResult = mysqli_query($conn, $sql);

while ($material = mysqli_fetch_assoc($materialsResult)) {
    // Calculate available material quantity = Total Inventory - Reserve inventory
    $available = $material['mqty'] - $material['mrqty'];

    // Calculate the production quantity = Amount of material available / Amount of material required per unit of product
    $maxForMaterial = floor($available / $material['pmqty']);

    // Add the current order quantity
    $maxForMaterial += $order['oqty'];

    // Take the minimum value of all materials as the maximum available quantity
    if ($maxForMaterial < $maxQuantity) {
        $maxQuantity = $maxForMaterial;
    }
}

// Make sure the maximum number is at least 1
$maxQuantity = max(1, $maxQuantity);
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Modify Order</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/orderDetail.css">
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

<div class="order-details-container">
    <div class="back-link">
        <a href="StaffOrderDetails.php?oid=<?= $oid ?>" class="back-link-text"> &#60; Back to Order Details</a>
    </div>

    <h1>Modify Order</h1>

    <?php if (!empty($errorMessage)): ?>
        <div class="card error-card">
            <div class="error-message">
                <?= $errorMessage ?>
            </div>
        </div>
    <?php endif; ?>

    <div class="order-grid">
        <!-- Original data card -->
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Original Data</h2>
            </div>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Order ID</div>
                    <div class="info-value"><?= $order['oid'] ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Product ID</div>
                    <div class="info-value"><?= $order['pid'] ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Order Quantity</div>
                    <div class="info-value"><?= $order['oqty'] ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Materials Used</div>
                    <div class="info-value"><?= implode(', ', $materialNames) ?></div>
                </div>
            </div>
        </div>

        <!-- Modify form card -->
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Modify Data</h2>
            </div>
            <form id="modifyOrderForm" method="post" action="modifyOrderProcess.php">
                <input type="hidden" name="oid" value="<?= $oid ?>">
                <input type="hidden" name="original_qty" value="<?= $order['oqty'] ?>">
                <input type="hidden" name="unit_cost" value="<?= $order['ocost'] / $order['oqty'] ?>">

                <div class="form-group">
                    <label for="newOrderQuantity" class="form-label">New Order Quantity</label>
                    <p>Maximum available: <?= $maxQuantity ?></p>
                    <input type="number" id="newOrderQuantity" name="newOrderQuantity"
                           class="input-field"
                           value="<?= $order['oqty'] ?>"
                           min="1"
                           max="<?= $maxQuantity ?>">
                </div>

                <input type="submit" class="btn-update" value="Update Order">
            </form>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const cards = document.querySelectorAll('.order-details-container .card');
        cards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-5px)';
                this.style.boxShadow = '0 6px 16px rgba(0, 0, 0, 0.12)';
            });

            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.08)';
            });
        });

        const buttons = document.querySelectorAll('.btn-update');
        buttons.forEach(button => {
            button.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-3px)';
                this.style.boxShadow = '0 6px 12px rgba(0, 0, 0, 0.15)';
            });

            button.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
            });
        });
    });
</script>
</body>
</html>