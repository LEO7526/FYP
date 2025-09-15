<?php
require_once '../auth_check.php';
check_customer_auth();

include '../conn.php';

$oid = isset($_GET['oid']) ? intval($_GET['oid']) : 0;
$cid = $_SESSION['customer']['cid'];

if ($oid==0) {
    echo '<script>
        alert("Please open with order list.");
        window.location.href = "MyOrder.php";
    </script>';
    exit();
}

// Fetch order details
$sql = "SELECT o.*, p.pname, p.pcost, c.cname, c.ctel, c.caddr, c.company
        FROM orders o
        JOIN product p ON o.pid = p.pid
        JOIN customer c ON o.cid = c.cid
        WHERE o.oid = $oid AND o.cid = $cid";
$result = mysqli_query($conn, $sql);

if (!$result || mysqli_num_rows($result) === 0) {
    header('Location: MyOrder.php');
    exit();
}

$order = mysqli_fetch_assoc($result);

// Status mapping
$statusMap = [
    0 => 'Rejected',
    1 => 'Pending',
    2 => 'Processing',
    3 => 'Completed'
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
        <link rel="stylesheet" href="../CSS/order_details.css">
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

    <div class="order-details">
        <div class="back-link">
            <a href="MyOrder.php">&lt; Back to My Orders</a>
        </div>

        <h1>Order Details</h1>

        <div class="detail-section">
            <h2>Order Information</h2>
            <div class="detail-row">
                <div class="detail-label">Order ID</div>
                <div class="detail-value"><?= htmlspecialchars($order['oid']) ?></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Order Date</div>
                <div class="detail-value"><?= htmlspecialchars(date('Y-m-d', strtotime($order['odate']))) ?></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Order Status</div>
                <div class="detail-value highlight"><?= htmlspecialchars($status) ?></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Delivery Date</div>
                <div class="detail-value"><?= $order['odeliverdate'] ? htmlspecialchars(date('Y-m-d', strtotime($order['odeliverdate']))) : 'N/A' ?></div>
            </div>
        </div>

        <div class="detail-section">
            <h2>Product Information</h2>
            <div class="detail-row">
                <div class="detail-label">Product ID</div>
                <div class="detail-value"><?= htmlspecialchars($order['pid']) ?></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Product Name</div>
                <div class="detail-value"><?= htmlspecialchars($order['pname']) ?></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Order Quantity</div>
                <div class="detail-value"><?= htmlspecialchars($order['oqty']) ?> units</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Total Amount (USD)</div>
                <div class="detail-value">$<?= htmlspecialchars(number_format($order['ocost'], 2)) ?></div>
            </div>
        </div>

        <div class="detail-section">
            <h2>Customer Information</h2>
            <div class="detail-row">
                <div class="detail-label">Contact Name</div>
                <div class="detail-value"><?= htmlspecialchars($order['cname']) ?></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Company</div>
                <div class="detail-value"><?= htmlspecialchars($order['company']) ?></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Contact Number</div>
                <div class="detail-value"><?= htmlspecialchars($order['ctel']) ?></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Delivery Address</div>
                <div class="detail-value"><?= htmlspecialchars($order['caddr']) ?></div>
            </div>
        </div>

        <div class="order-actions">
            <?php if ($order['ostatus'] == 1 || $order['ostatus'] == 2): ?>
                <button onclick="confirmCancel(<?= $oid ?>)" class="btn-reject">Cancel Order</button>
            <?php endif; ?>
        </div>

        <script>
            function confirmCancel(oid) {
                const confirmed = confirm('Are you sure you want to cancel this order?\n\n' +
                    'Note: Orders can only be cancelled at least 2 days before the delivery date.');

                if (confirmed) {
                    window.location.href = 'cancelOrder.php?oid=' + oid;
                }
            }
        </script>
    </div>
    </body>
    </html>
<?php mysqli_close($conn); ?>