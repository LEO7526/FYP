<?php
require_once '../auth_check.php';
check_customer_auth();

include '../conn.php';

// Initialize variables
$error = '';
$success = '';
$pid = isset($_GET['pid']) ? (int)$_GET['pid'] : 0;
if (!$pid && isset($_SESSION['selected_product'])) {
    $pid = (int)$_SESSION['selected_product'];
}

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Validate inputs
    $quantity = isset($_POST['quantity']) ? (int)$_POST['quantity'] : 0;
    $password = isset($_POST['password']) ? trim($_POST['password']) : '';
    $cid = $_SESSION['customer']['cid'];

    // Verify password with legacy support
    $sql = "SELECT cpassword FROM customer WHERE cid = ?";
    $stmt = $conn->prepare($sql);

    if ($stmt === false) {
        die(json_encode(['success' => false, 'message' => 'Database prepare failed: ' . $conn->error]));
    }

    $stmt->bind_param("i", $cid);
    if (!$stmt->execute()) {
        $error = "Database error: " . $stmt->error;
    } else {
        $result = $stmt->get_result();

        if ($result->num_rows === 0) {
            $error = "Customer not found.";
        } else {
            $customer = $result->fetch_assoc();
            $storedPassword = $customer['cpassword'];

            // Check both hashed and plaintext passwords
            $passwordValid = false;

            if (password_verify($password, $storedPassword)) {
                // Modern hashed password
                $passwordValid = true;
            } elseif ($storedPassword === $password) {
                // Legacy plaintext password
                $passwordValid = true;

                // Upgrade to hashed password
                $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
                $updateSql = "UPDATE customer SET cpassword = ? WHERE cid = ?";
                $updateStmt = $conn->prepare($updateSql);
                $updateStmt->bind_param("si", $hashedPassword, $cid);
                $updateStmt->execute();
                $updateStmt->close();
            }

            if (!$passwordValid) {
                $error = "Incorrect password. Please try again.";
            } elseif ($quantity < 1) {
                $error = "Invalid quantity. Please enter a valid quantity.";
            } else {
                // Get product price
                $sql = "SELECT pcost FROM product WHERE pid = ?";
                $stmt = $conn->prepare($sql);
                $stmt->bind_param("i", $pid);
                $stmt->execute();
                $result = $stmt->get_result();
                $product = $result->fetch_assoc();
                $stmt->close();

                if (!$product) {
                    $error = "Product not found.";
                } else {
                    // Calculate total cost
                    $total_cost = $product['pcost'] * $quantity;

                    // Start transaction
                    $conn->begin_transaction();

                    try {
                        // Insert order into database
                        $sql = "INSERT INTO orders (cid, pid, oqty, ocost, ostatus, odate) 
                                VALUES (?, ?, ?, ?, 1, NOW())"; // 1 = Pending status
                        $stmt = $conn->prepare($sql);
                        $stmt->bind_param("iiid", $cid, $pid, $quantity, $total_cost);

                        if (!$stmt->execute()) {
                            throw new Exception("Error placing order: " . $stmt->error);
                        }

                        $order_id = $conn->insert_id;

                        // Update reserved quantities in materials
                        $sql = "UPDATE material m
                                JOIN prodmat pm ON m.mid = pm.mid
                                SET m.mrqty = m.mrqty + (pm.pmqty * ?)
                                WHERE pm.pid = ?";
                        $stmt = $conn->prepare($sql);
                        $stmt->bind_param("ii", $quantity, $pid);

                        if (!$stmt->execute()) {
                            throw new Exception("Error updating materials: " . $stmt->error);
                        }

                        $conn->commit();
                        $success = "Order #$order_id has been placed successfully!";
                    } catch (Exception $e) {
                        $conn->rollback();
                        $error = $e->getMessage();
                    }
                }
            }
        }
    }
    $stmt->close();
}

// Get product info (only if not submitting form or if there was an error)
if ($pid > 0 && ($_SERVER['REQUEST_METHOD'] !== 'POST' || $error)) {
    // Get product info
    $sql = "SELECT * FROM product WHERE pid = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $pid);
    $stmt->execute();
    $result = $stmt->get_result();
    $product = $result->fetch_assoc();
    $stmt->close();

    // Get customer info
    $cid = $_SESSION['customer']['cid'];
    $sql = "SELECT * FROM customer WHERE cid = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $cid);
    $stmt->execute();
    $result = $stmt->get_result();
    $customer = $result->fetch_assoc();
    $stmt->close();

    // Calculate maximum available quantity
    $maxAvailable = PHP_INT_MAX;
    $sql = "SELECT m.mqty - m.mrqty AS available, pm.pmqty AS required_per_unit
            FROM prodmat pm
            JOIN material m ON pm.mid = m.mid
            WHERE pm.pid = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $pid);
    $stmt->execute();
    $materials = $stmt->get_result();

    while ($material = $materials->fetch_assoc()) {
        $availableForMaterial = floor($material['available'] / $material['required_per_unit']);
        $maxAvailable = min($maxAvailable, $availableForMaterial);
    }
    $stmt->close();

    // Ensure at least 1 is available
    $maxAvailable = max(1, $maxAvailable);
}

mysqli_close($conn);
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Make Order</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/order.css">
    <link rel="stylesheet" href="../CSS/form.css">
    <style>
        .availability-box {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 5px;
            padding: 15px;
            margin-bottom: 20px;
        }
        .availability-value {
            font-size: 1.2em;
            font-weight: bold;
            color: #28a745;
        }
        .quantity-controls {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .quantity-controls input {
            text-align: center;
            width: 60px;
        }
        .success-message {
            color: green;
            margin-bottom: 15px;
            padding: 10px;
            background-color: #e6ffe6;
            border: 1px solid #a3e8a3;
            border-radius: 4px;
        }
        .error-message {
            color: red;
            margin-bottom: 15px;
            padding: 10px;
            background-color: #ffebeb;
            border: 1px solid #ffb3b3;
            border-radius: 4px;
        }

    </style>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const urlParams = new URLSearchParams(window.location.search);
            if (!urlParams.has('pid') && sessionStorage.getItem('selectedProduct')) {
                window.location.href = `MakeOrder.php?pid=${sessionStorage.getItem('selectedProduct')}`;
            }

            let unitPrice = 0;
            let maxAvailable = 0;

            <?php if (isset($product) && isset($maxAvailable)) { ?>
            unitPrice = <?= $product['pcost'] ?>;
            maxAvailable = <?= $maxAvailable ?>;
            <?php } ?>

            const quantityInput = document.getElementById('productQuantity');
            const totalDisplay = document.getElementById('totalPriceDisplay');
            const availableDisplay = document.getElementById('availableQuantity');

            if (quantityInput) {
                quantityInput.addEventListener('input', function() {
                    let quantity = parseInt(this.value) || 0;
                    if (quantity > maxAvailable) {
                        quantity = maxAvailable;
                        this.value = maxAvailable;
                    } else if (quantity < 1) {
                        quantity = 1;
                        this.value = 1;
                    }
                    const totalPrice = quantity * unitPrice;
                    totalDisplay.textContent = `$${totalPrice.toFixed(2)}`;
                });
            }

            if (quantityInput && totalDisplay && availableDisplay) {
                quantityInput.max = maxAvailable;
                availableDisplay.textContent = maxAvailable;
                totalDisplay.textContent = `$${(unitPrice * 1).toFixed(2)}`;
            }
        });
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

<div class="container">
    <h1>Make Order</h1>

    <?php if (!empty($success)): ?>
        <div class="success-message"><?= htmlspecialchars($success) ?></div>
        <a href="Index.php" class="btn-back">Back to Products</a>
        <a href="MyOrder.php" class="btn-submit">View My Orders</a>
    <?php elseif (empty($product)): ?>
        <div class="error-message">No product selected. Please go back and select a product.</div>
        <a href="Index.php" class="btn-back">Back to Products</a>
    <?php else: ?>
        <?php if (!empty($error)): ?>
            <div class="error-message"><?= htmlspecialchars($error) ?></div>
        <?php endif; ?>

        <form id="productForm" action="MakeOrder.php?pid=<?= $pid ?>" method="post">
            <input type="hidden" name="pid" value="<?= $pid ?>">

            <div class="availability-box">
                <h3>Available Quantity</h3>
                <p>You can order up to <span id="availableQuantity" class="availability-value"><?= isset($maxAvailable) ? $maxAvailable : 0 ?></span> units of this product.</p>
            </div>

            <div class="form-group">
                <h2 class="form-label">Product ID:</h2>
                <input type="text" class="display" value="<?= str_pad($pid, 3, '0', STR_PAD_LEFT) ?>" readonly>
            </div>

            <div class="form-group">
                <img src="../Sample Images/product/<?= $pid ?>.jpg" alt="Product Image" class="product-image">
                <p class="product-title"><?= htmlspecialchars($product['pname']) ?></p>
                <p class="product-price">Price per unit: $<?= number_format($product['pcost'], 2) ?></p>
            </div>

            <div class="form-group">
                <h2 class="form-label">Order Quantity</h2>
                <div class="quantity-controls">
                    <input type="number" id="productQuantity" name="quantity"
                           min="1" max="<?= isset($maxAvailable) ? $maxAvailable : 0 ?>" value="<?= isset($_POST['quantity']) ? htmlspecialchars($_POST['quantity']) : 1 ?>" required>
                    <span>(Max: <?= isset($maxAvailable) ? $maxAvailable : 0 ?>)</span>
                </div>
            </div>

            <div class="form-group">
                <h2 class="form-label">Total Price:</h2>
                <div id="totalPriceDisplay" class="price-display">
                    $<?= number_format($product['pcost'] * (isset($_POST['quantity']) ? (int)$_POST['quantity'] : 1), 2) ?>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">Customer ID</label>
                <input type="text" class="display" value="<?= isset($customer['cid']) ? htmlspecialchars($customer['cid']) : '' ?>" readonly>
            </div>

            <div class="form-group">
                <h2 class="form-label">Customer Name</h2>
                <input type="text" class="display" value="<?= isset($customer['cname']) ? htmlspecialchars($customer['cname']) : '' ?>" readonly>
            </div>

            <div class="form-group">
                <h2 class="form-label">Phone Number</h2>
                <input type="text" class="display" value="<?= isset($customer['ctel']) ? htmlspecialchars($customer['ctel']) : '' ?>" readonly>
            </div>

            <div class="form-group">
                <label class="form-label">Delivery Address</label>
                <textarea class="display" rows="3" readonly><?= isset($customer['caddr']) ? htmlspecialchars($customer['caddr']) : '' ?></textarea>
            </div>

            <div class="form-group">
                <label class="form-label">Company Name</label>
                <input type="text" class="display" value="<?= isset($customer['company']) ? htmlspecialchars($customer['company']) : '' ?>" readonly>
            </div>

            <div class="form-section">
                <div class="form-group">
                    <label class="form-label">Password</label>
                    <input type="password" id="currentPassword" name="password" required>
                    <small class="form-note">Enter your password to confirm the order</small>
                </div>
            </div>

            <button type="submit" class="btn-submit">Place Order</button>
            <a href="Index.php" class="btn-reset">Cancel</a>
        </form>
    <?php endif; ?>
</div>
</body>
</html>