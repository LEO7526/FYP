<?php
session_start();

// 檢查會員是否登入
if (!isset($_SESSION['cid'])) {
    echo "<p>Please <a href='../login/login2.php'>login</a> to redeem coupons.</p>";
    exit;
}

$cid = $_SESSION['cid'];

$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // 接收 coupon_id
    // 接收 coupon_id 和數量
    $couponId = $_POST['coupon'] ?? null;
    $quantity = isset($_POST['quantity']) ? (int)$_POST['quantity'] : 1;
    if (!$couponId || $quantity < 1) {
        die("Invalid coupon or quantity.");
    }

// 撈出 coupon 所需積分
    $stmt = $pdo->prepare("SELECT points_required FROM coupons WHERE coupon_id = ?");
    $stmt->execute([$couponId]);
    $coupon = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$coupon) {
        die("Coupon not found.");
    }

    $requiredPoints = (int)$coupon['points_required'] * $quantity;

// 撈出會員目前積分
    $stmt = $pdo->prepare("SELECT coupon_point FROM customer WHERE cid = ?");
    $stmt->execute([$cid]);
    $member = $stmt->fetch(PDO::FETCH_ASSOC);

    $currentPoints = (int)$member['coupon_point'];

    if ($currentPoints >= $requiredPoints) {
        // 扣除積分
        $stmt = $pdo->prepare("UPDATE customer SET coupon_point = coupon_point - ? WHERE cid = ?");
        $stmt->execute([$requiredPoints, $cid]);

        // 新增兌換紀錄（可記錄數量）
        for ($i=0; $i<$quantity; ++$i) {
            $stmt = $pdo->prepare("INSERT INTO coupon_redemptions (cid, coupon_id, redeemed_at) VALUES (?, ?, NOW())");
            $stmt->execute([$cid, $couponId]);
        }

        echo "<div style='text-align:center; margin-top:50px;'>";
        echo "<h2>Coupon Redeemed Successfully!</h2>";
        echo "<p>Quantity: <strong>{$quantity}</strong></p>";
        echo "<p>Total Points deducted: <strong>{$requiredPoints}</strong></p>";
        echo "<p>Your remaining points: <strong>".($currentPoints - $requiredPoints)."</strong></p>";
        echo "<a href='coupon.php' style='display:inline-block; margin-top:20px; padding:10px 20px; background:#3498db; color:#fff; text-decoration:none; border-radius:6px;'>Back to Coupons</a>";
        echo "</div>";
    } else {
        echo "<div style='text-align:center; margin-top:50px;'>";
        echo "<h2>Not Enough Points</h2>";
        echo "<p>You need <strong>{$requiredPoints}</strong> points to redeem {$quantity} coupons.</p>";
        echo "<p>Your current points: <strong>{$currentPoints}</strong></p>";
        echo "<a href='coupon.php' style='display:inline-block; margin-top:20px; padding:10px 20px; background:#e74c3c; color:#fff; text-decoration:none; border-radius:6px;'>Back to Coupons</a>";
        echo "</div>";
    }


} catch (PDOException $e) {
    die("Database error: " . $e->getMessage());
}
?>
