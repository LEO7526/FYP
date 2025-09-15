<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

$oid = isset($_GET['oid']) ? intval($_GET['oid']) : 0;


if ($oid==0) {
    echo '<script>
        alert("Please open with order list.");
        window.location.href = "OrderList.php";
    </script>';
    exit();
}


// ===== AUTOMATIC STATUS UPDATE =====
// When viewing an order with status 1 (To be viewed), automatically update to status 2 (To be accepted)
if ($oid > 0) {
    // Check current status of the order
    $statusCheckSql = "SELECT ostatus FROM orders WHERE oid = $oid";
    $statusResult = mysqli_query($conn, $statusCheckSql);

    if ($statusResult && mysqli_num_rows($statusResult) > 0) {
        $statusData = mysqli_fetch_assoc($statusResult);

        // If status is 1 (To be viewed), update to 2 (To be accepted)
        if ($statusData['ostatus'] == 1) {
            $updateSql = "UPDATE orders SET ostatus = 2 WHERE oid = $oid";
            mysqli_query($conn, $updateSql);

            // Re-fetch order data to ensure updated status is displayed
            $sql = "SELECT o.*, p.pname, p.pcost, c.cname, c.ctel, c.caddr, c.company
                    FROM orders o
                    JOIN product p ON o.pid = p.pid
                    JOIN customer c ON o.cid = c.cid
                    WHERE o.oid = $oid";
            $result = mysqli_query($conn, $sql);
            $order = mysqli_fetch_assoc($result);
        }
    }
}
// ===== END STATUS UPDATE =====

// Fetch order details if not already fetched by status update
if (!isset($order)) {
    $sql = "SELECT o.*, p.pname, p.pcost, c.cname, c.ctel, c.caddr, c.company
            FROM orders o
            JOIN product p ON o.pid = p.pid
            JOIN customer c ON o.cid = c.cid
            WHERE o.oid = $oid";
    $result = mysqli_query($conn, $sql);
    $order = mysqli_fetch_assoc($result);
}

// Get materials used in this product
$sql = "SELECT m.mid, m.mname, m.mqty, m.mrqty, m.munit, pm.pmqty
        FROM prodmat pm
        JOIN material m ON pm.mid = m.mid
        WHERE pm.pid = {$order['pid']}";
$materials = mysqli_query($conn, $sql);

// Status mapping
$statusMap = [
    0 => 'Rejected',
    1 => 'To be viewed',
    2 => 'To be accepted',
    3 => 'Approved'
];
$status = isset($statusMap[$order['ostatus']]) ? $statusMap[$order['ostatus']] : 'Unknown';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Order Details</title>
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

<!-- Order details container -->
<div class="order-details-container">
    <div class="back-link">
        <a href="OrderList.php" class="back-link-text"> &#60; Back to Order List</a>
    </div>

    <h1>Order Details</h1>

    <div class="order-grid">
        <!-- Order Information -->
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Order Information</h2>
            </div>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Order ID</div>
                    <div class="info-value"><?= $order['oid'] ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Order Date</div>
                    <div class="info-value"><?= date('Y-m-d', strtotime($order['odate'])) ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Order Status</div>
                    <div class="info-value highlight"><?= $status ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Delivery Date</div>
                    <div class="info-value"><?= $order['odeliverdate'] ? date('Y-m-d', strtotime($order['odeliverdate'])) : 'N/A' ?></div>
                </div>
            </div>
        </div>

        <!-- Product Information -->
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Product Information</h2>
            </div>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Product ID</div>
                    <div class="info-value"><?= $order['pid'] ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Product Name</div>
                    <div class="info-value"><?= $order['pname'] ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Order Quantity</div>
                    <div class="info-value"><?= $order['oqty'] ?> units</div>
                </div>
                <div class="info-item">
                    <div class="info-label">Total Amount (USD)</div>
                    <div class="info-value">$<?= number_format($order['ocost'], 2) ?></div>
                </div>
            </div>
        </div>

        <!-- Customer Information -->
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Customer Information</h2>
            </div>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Contact Name</div>
                    <div class="info-value"><?= $order['cname'] ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Contact Number</div>
                    <div class="info-value"><?= $order['ctel'] ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Company</div>
                    <div class="info-value"><?= $order['company'] ?></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Delivery Address</div>
                    <div class="info-value"><?= $order['caddr'] ?></div>
                </div>
            </div>
        </div>
    </div>

    <!-- Material inventory section -->
    <div class="materials-section">
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Material Stock Status</h2>
            </div>

            <div class="materials-grid">
                <?php while ($material = mysqli_fetch_assoc($materials)):
                    $usedQty = $order['oqty'] * $material['pmqty'];
                    $available = $material['mqty'] - $material['mrqty'];

                    // Calculate maximum available quantity for this material
                    $maxForMaterial = floor($available / $material['pmqty']);

                    // Determine material status
                    if ($available >= $usedQty) {
                        $alertClass = 'safety';
                        $statusText = 'Safety';
                        $statusColor = '#27ae60';
                    } elseif ($available >= ($usedQty * 0.7)) {
                        $alertClass = 'danger';
                        $statusText = 'Danger';
                        $statusColor = '#f39c12';
                    } else {
                        $alertClass = 'insufficient';
                        $statusText = 'Insufficient';
                        $statusColor = '#e74c3c';
                    }
                    ?>
                    <div class="material-card <?= $alertClass ?>">
                        <div class="material-name"><?= $material['mname'] ?></div>
                        <div class="stock-info">
                            <div class="stock-item">
                                <!-- Changed to Material Usage -->
                                <div class="info-label">Material Usage</div>
                                <div class="stock-value"><?= $usedQty ?> <?= $material['munit'] ?></div>
                            </div>
                            <div class="stock-item">
                                <!-- Changed to Available Stock -->
                                <div class="info-label">Available Stock</div>
                                <div class="stock-value"><?= $available ?> <?= $material['munit'] ?></div>
                            </div>
                            <div class="stock-item">
                                <div class="info-label">Physical Stock</div>
                                <div class="stock-value"><?= $material['mqty'] ?> <?= $material['munit'] ?></div>
                            </div>
                            <div class="stock-item">
                                <div class="info-label">Status</div>
                                <div class="stock-value" style="color:<?= $statusColor ?>;"><?= $statusText ?></div>
                            </div>
                        </div>
                    </div>
                <?php endwhile; ?>
            </div>
        </div>
    </div>

    <!-- Action Button -->
    <div class="order-actions">
        <?php if ($order['ostatus'] == 1 || $order['ostatus'] == 2): ?>
            <a href="orderStatus.php?oid=<?= $oid ?>&action=accept" class="btn-accept">Accept</a>
            <a href="orderStatus.php?oid=<?= $oid ?>&action=reject" class="btn-reject">Reject</a>
            <a href="ModifyOrder.php?oid=<?= $oid ?>" class="btn-modify">Modify</a>
        <?php endif; ?>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const buttons = document.querySelectorAll('.order-details-container .btn-accept, .order-details-container .btn-reject, .order-details-container .btn-modify');
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

        // Card hover effect
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
    });
</script>
</body>
</html>