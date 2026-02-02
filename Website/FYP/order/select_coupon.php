<?php
session_start();

if (!isset($_COOKIE['cid'])) {
    echo "<p>Please <a href='../login/login2.php'>login</a> to select coupons.</p>";
    exit;
}

$cid = $_COOKIE['cid'];
$item_ids = $_SESSION['order']['item_ids'] ?? [];
$quantities = $_SESSION['order']['quantities'] ?? [];
$spice_levels = $_SESSION['order']['spice_levels'] ?? [];
$remarks = $_SESSION['order']['remarks'] ?? [];
$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $stmt = $pdo->prepare(" SELECT cr.coupon_id, ct.title, ct.description FROM coupon_redemptions cr JOIN coupon_translation ct ON cr.coupon_id = ct.coupon_id WHERE cr.cid = ? AND ct.language_code = 'en'");
    $stmt->execute([$cid]);
    $couponRedemptions = $stmt->fetchAll(PDO::FETCH_ASSOC);


} catch (PDOException $e) {
    die("Database error: " . $e->getMessage());
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Select Coupon - XXX Restaurant</title>
    <link rel="stylesheet" href="../select_coupon.css">
</head>
<body>
<div class="container">
    <h2>Select a Coupon</h2>
    <form method="post" action="customer_finalize_order.php">
        <?php foreach ($item_ids as $index => $id): ?>
            <input type="hidden" name="item_ids[]" value="<?= htmlspecialchars($id) ?>">
            <input type="hidden" name="quantities[]" value="<?= htmlspecialchars($quantities[$index]) ?>">
        <?php endforeach; ?>

        <?php if ($couponRedemptions): ?>
            <?php foreach ($couponRedemptions as $c): ?>
                <label style="display:block; margin:8px 0;">
                    <input type="radio" name="coupon" value="<?= htmlspecialchars($c['coupon_id']) ?>" required>
                    <strong><?= htmlspecialchars($c['title']) ?></strong> - <?= htmlspecialchars($c['description']) ?>
                </label>
            <?php endforeach; ?>
            <button type="submit" style="margin-top:10px; padding:8px 16px; background:#f39c12; color:#fff; border:none; border-radius:6px;">Confirm Coupon</button>
        <?php else: ?>
            <p>No available coupons.</p>
            <button type="submit" style="margin-top:10px; padding:8px 16px; background:#2ecc71; color:#fff; border:none; border-radius:6px;">Checkout Without Coupon</button>
        <?php endif; ?>
    </form>
</div>
</body>
</html>
