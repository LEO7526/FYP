<?php
require_once '../auth_check.php';
check_staff_auth();

include '../conn.php';

// 修正後的 SQL 查詢 (移除 p.pimage)
$sql = "SELECT p.pid, p.pname, COUNT(o.oid) as order_count 
        FROM product p 
        LEFT JOIN orders o ON p.pid = o.pid 
        GROUP BY p.pid";

$result = mysqli_query($conn, $sql);

// 添加錯誤處理
if (!$result) {
    die("Query failed: " . mysqli_error($conn));
}

$products = mysqli_fetch_all($result, MYSQLI_ASSOC);
?>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Delete Item</title>
        <link rel="stylesheet" href="../CSS/header.css">
        <link rel="stylesheet" href="../CSS/common.css">
        <link rel="stylesheet" href="../CSS/delete.css">
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
        <h1>Delete Item</h1>

        <?php if (isset($_SESSION['delete_error'])): ?>
            <div class="error-message" style="color:red; margin-bottom:15px;">
                <?php echo $_SESSION['delete_error']; unset($_SESSION['delete_error']); ?>
            </div>
        <?php endif; ?>

        <table id="productTable">
            <thead>
            <tr>
                <th>Product ID</th>
                <th>Product Name</th>
                <th>Product Image</th>
                <th>Existing Orders</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <?php foreach ($products as $product): ?>
                <tr>
                    <td><?php echo htmlspecialchars($product['pid']); ?></td>
                    <td><?php echo htmlspecialchars($product['pname']); ?></td>
                    <td>
                        <?php
                        $imagePath = "../Sample Images/product/" . $product['pid'] . ".jpg";
                        if (file_exists($imagePath)): ?>
                            <img src="<?php echo $imagePath; ?>"
                                 alt="Product Image" class="product-image">
                        <?php else: ?>
                            <div class="no-image">No Image</div>
                        <?php endif; ?>
                    </td>
                    <td><?php echo (int)$product['order_count']; ?></td>
                    <td>
                        <?php if ($product['order_count'] == 0): ?>
                            <a href="#" class="btn-delete"
                               onclick="confirmDelete('<?php echo $product['pid']; ?>')">Delete</a>
                        <?php else: ?>
                            <a href="#" class="btn-delete-disabled">Delete</a>
                        <?php endif; ?>
                    </td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
    </div>
    <script>
        function confirmDelete(pid) {
            if (confirm("Are you sure you want to delete this product?")) {
                window.location.href = "deleteProcess.php?pid=" + pid;
            }
        }
    </script>
    </body>
    </html>
<?php
mysqli_close($conn);
?>