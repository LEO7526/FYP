<?php
require_once '../auth_check.php';
check_customer_auth();

include '../conn.php';

// Fetch products from database
$products = [];
$sql = "SELECT pid, pname, pcost FROM product";
$result = mysqli_query($conn, $sql);
while ($row = mysqli_fetch_assoc($result)) {
    $products[] = $row;
}
mysqli_close($conn);
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Products Page</title>
    <link rel="stylesheet" href="../CSS/index.css">
    <link rel="stylesheet" href="../CSS/header.css">
    <script>
        function addToCart(pid) {
            // Store product ID in sessionStorage
            sessionStorage.setItem('selectedProduct', pid);
            // Redirect to MakeOrder page
            window.location.href = 'MakeOrder.php';
        }
    </script>
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

<main class="product-grid">
    <?php foreach ($products as $product): ?>
        <div class="product-card">
            <div class="product-image">
                <img src="../Sample Images/product/<?= $product['pid'] ?>.jpg" alt="<?= htmlspecialchars($product['pname']) ?>">
            </div>
            <div class="product-title"><?= htmlspecialchars($product['pname']) ?></div>
            <div class="product-ID">id:<?= str_pad($product['pid'], 3, '0', STR_PAD_LEFT) ?></div>
            <div class="product-price">$<?= number_format($product['pcost'], 2) ?></div>
            <button class="add-to-cart" onclick="addToCart(<?= $product['pid'] ?>)">Add</button>
        </div>
    <?php endforeach; ?>
</main>
</body>
</html>