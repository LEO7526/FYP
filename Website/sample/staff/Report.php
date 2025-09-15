
<?php
require_once '../auth_check.php';
check_staff_auth(); // Verify staff authentication status

// Database connection
include '../conn.php';

// Fetch report data from database
$sql = "SELECT 
            p.pid AS product_id,
            p.pname AS product_name,
            SUM(o.oqty) AS total_quantity,
            SUM(o.oqty * p.pcost) AS total_sales
        FROM orders o
        JOIN product p ON o.pid = p.pid
        GROUP BY p.pid
        ORDER BY total_sales DESC";

$result = mysqli_query($conn, $sql);

// Initialize reports array
$reports = [];
if ($result && mysqli_num_rows($result) > 0) {
    while ($row = mysqli_fetch_assoc($result)) {
        $reports[] = $row;
    }
}

// Close database connection
mysqli_close($conn);
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sales Report</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/order.css">
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
<div class="back-link">
    <a href="staffIndex.php" class="back-link-text"> &#60; Back</a>
</div>
<div class="container">
    <h1>Sales Report</h1>

    <?php if (count($reports) > 0): ?>
        <!-- Sales summary -->
        <div class="report-summary">
            <?php
            $totalSales = array_sum(array_column($reports, 'total_sales'));
            $totalItems = array_sum(array_column($reports, 'total_quantity'));
            echo "Total Sales(USD): <span class='sales-amount'>$" . number_format($totalSales, 2) . "</span> | ";
            echo "Total Items Sold: " . number_format($totalItems);
            ?>
        </div>

        <!-- Report table -->
        <table id="reportTable">
            <thead>
            <tr>
                <th>Product ID</th>
                <th>Product Name</th>
                <th>Product Image</th>
                <th>Total Quantity</th>
                <th>Total Sales Amount ($USD)</th>
            </tr>
            </thead>
            <tbody>
            <?php foreach ($reports as $report): ?>
                <tr>
                    <td><?php echo htmlspecialchars($report['product_id']); ?></td>
                    <td><?php echo htmlspecialchars($report['product_name']); ?></td>
                    <td>
                        <?php
                        $imagePath = "../Sample Images/product/" . $report['product_id'] . ".jpg";
                        if (file_exists($imagePath)): ?>
                            <img src="<?php echo $imagePath; ?>" alt="Product Image" class="product-image">
                        <?php else: ?>
                            <div class="no-image">No Image</div>
                        <?php endif; ?>
                    </td>
                    <td><?php echo number_format($report['total_quantity']); ?></td>
                    <td class="sales-amount">$<?php echo number_format($report['total_sales'], 2); ?></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
    <?php else: ?>
        <div class="no-data">
            No sales data available.
        </div>
    <?php endif; ?>
</div>
</body>
</html>