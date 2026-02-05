<?php
session_start();
$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

$orderRef = $_GET['orderRef'] ?? null;
$cid = $_COOKIE['cid'] ?? 0;
$totalPrice = $_SESSION['totalPrice'] ?? 0;
$item_ids = $_SESSION['order']['item_ids'] ?? [];
$quantities = $_SESSION['order']['quantities'] ?? [];
$couponId = $_POST['coupon'] ?? null;
$order_type = $_SESSION['order_type'] ?? null;

if ($order_type == 'takeaway') {
    $table_number = null;
}else{
    $table_number = 5;
}

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    if ($orderRef) {
        // 更新訂單狀態為 paid
        $odate = date('Y-m-d H:i:s');
        $orderRef = uniqid("ORD");
        $stmt = $pdo->prepare("INSERT INTO orders (odate, cid, ostatus, orderRef, order_type, table_number) VALUES (?, ?, ?, ?, ?, ?)");
        $stmt->execute([$odate, $cid, 1, $orderRef, $order_type, $table_number]);
        $orderId = $pdo->lastInsertId();

        for ($i = 0; $i < count($item_ids); $i++) {
            $stmt = $pdo->prepare("INSERT INTO order_items (oid, item_id, qty) VALUES (?, ?, ?)");
            $stmt->execute([$orderId, $item_ids[$i], $quantities[$i]]);
        }
        if ($couponId) {
            $stmt = $pdo->prepare("UPDATE coupon_redemptions SET is_used = 1 WHERE cid = ? AND coupon_id = ?");
            $stmt->execute([$cid, $couponId]);
        }
        if ($cid > 0){
            $stmt = $pdo->prepare("UPDATE customer SET coupon_point = coupon_point + ? WHERE cid = ?");
            $stmt->execute([$totalPrice, $cid]);
        }
    }else{
        $odate = date('Y-m-d H:i:s');
        $orderRef = uniqid("ORD");
        $stmt = $pdo->prepare("INSERT INTO orders (odate, cid, ostatus, orderRef, order_type, table_number) VALUES (?, ?, ?, ?, ?, ?)");
        $stmt->execute([$odate, $cid, 0, $orderRef, $order_type, $table_number]);
        $orderId = $pdo->lastInsertId();

        for ($i = 0; $i < count($item_ids); $i++) {
            $stmt = $pdo->prepare("INSERT INTO order_items (oid, item_id, qty) VALUES (?, ?, ?)");
            $stmt->execute([$orderId, $item_ids[$i], $quantities[$i]]);
        }
    }
}catch (PDOException $e) {
    die("Database error: " . $e->getMessage());
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Order Paid</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; margin-top: 50px; }
        .paid { color: green; font-size: 24px; font-weight: bold; }
        .order-info { margin-top: 20px; font-size: 18px; }
    </style>
    <link rel="stylesheet" href="finalize_order.css">
</head>
<body>
<?php
echo "<h3>Order Details:</h3>";
echo "<ul>";
    for ($i = 0; $i < count($item_ids); $i++) {
    $stmt = $pdo->prepare("SELECT mit.item_name, mi.item_price
    FROM menu_item mi
    JOIN menu_item_translation mit ON mi.item_id = mit.item_id
    WHERE mi.item_id = ? AND mit.language_code = 'en'");
    $stmt->execute([$item_ids[$i]]);
    $item = $stmt->fetch(PDO::FETCH_ASSOC);
    if ($item) {
    $qty = (int)$quantities[$i];
    $price = (float)$item['item_price'];
    $lineTotal = $price * $qty;
    echo "<li>{$item['item_name']} × {$qty} = $" . number_format($lineTotal, 2) . "</li>";
    }
    }
    echo "</ul>";

?>
</body>
</html>

