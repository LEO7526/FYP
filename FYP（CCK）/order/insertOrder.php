<?php
session_start();

// 從 session 取出訂單資料
$item_ids = $_SESSION['order']['item_ids'] ?? [];
$quantities = $_SESSION['order']['quantities'] ?? [];
$spice_levels = $_SESSION['order']['spice_levels'] ?? [];
$remarks = $_SESSION['order']['remarks'] ?? [];
$couponId = $_SESSION['coupon'] ?? null;
$totalPrice = $_SESSION['total_price'] ?? 0;
$cid = $_COOKIE['cid'] ?? 0;
$order_type = $_POST['order_type'] ?? 0;

$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    if ($couponId !== null) {
        if ($cid > 0) {
            $stmt = $pdo->prepare("UPDATE customer SET coupon_point = coupon_point + ? WHERE cid = ?");
            $stmt->execute([$totalPrice, $cid]);
            $stmt = $pdo->prepare("UPDATE coupon_redemptions SET is_used = 1 WHERE cid = ? AND coupon_id = ?");
            $stmt->execute([$cid, $couponId]);
        }
    }

        $odate = date('Y-m-d H:i:s');
        $orderRef = uniqid("ORD");
        $stmt = $pdo->prepare("INSERT INTO orders (odate, cid, ostatus, orderRef, order_type, table_number) VALUES (?, ?, ?, ?, ?, ?)");
        $stmt->execute([$odate, $cid, 1, $orderRef, $order_type, 5]);
        $orderId = $pdo->lastInsertId();

        for ($i = 0; $i < count($item_ids); $i++) {
            $stmt = $pdo->prepare("INSERT INTO order_items (oid, item_id, qty) VALUES (?, ?, ?)");
            $stmt->execute([$orderId, $item_ids[$i], $quantities[$i]]);
        }


}catch (PDOException $e){
    die("Database error: " . $e->getMessage());
}
