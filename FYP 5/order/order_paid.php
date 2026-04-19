<?php
session_start();
$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

$orderRef = $_GET['orderRef'] ?? null;
$cid = $_COOKIE['cid'] ?? 0;
$totalPrice = $_SESSION['total_price'] ?? 0;
$item_ids = $_SESSION['order']['item_ids'] ?? [];
$quantities = $_SESSION['order']['quantities'] ?? [];
$spice_levels = $_SESSION['order']['spice_levels'] ?? [];
$remarks = $_SESSION['order']['remarks'] ?? [];
$package_items = $_SESSION['order']['package_items'] ?? [];
$package_item_modifiers = $_SESSION['order']['package_item_modifiers'] ?? [];
$package_id = $_SESSION['order']['package_id'] ?? null;
$couponId = $_SESSION['coupon'] ?? null;
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

        // 插入套餐項目
        if ($package_id) {
            foreach ($package_items as $pid) {
                // 檢查 item_id 是否已經在 item_ids 中，避免重複插入
                if (!in_array($pid, $item_ids)) {
                    $stmt = $pdo->prepare("INSERT INTO order_items (oid, item_id, qty) VALUES (?, ?, ?)");
                    $stmt->execute([$orderId, $pid, 1]); // qty設為1，因為套餐項目是選中的
                }
            }
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

        // 插入套餐項目
        if ($package_id) {
            foreach ($package_items as $pid) {
                // 檢查 item_id 是否已經在 item_ids 中，避免重複插入
                if (!in_array($pid, $item_ids)) {
                    $stmt = $pdo->prepare("INSERT INTO order_items (oid, item_id, qty) VALUES (?, ?, ?)");
                    $stmt->execute([$orderId, $pid, 1]); // qty設為1，因為套餐項目是選中的
                }
            }
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
    echo "<li>{$item['item_name']} × {$qty}</li>";

    }
    }
    // 顯示套餐
    if ($package_id) {
        $stmt = $pdo->prepare("SELECT package_name FROM menu_package WHERE package_id = ?");
        $stmt->execute([$package_id]);
        $package = $stmt->fetch();
        if ($package) {
            echo "<li>{$package['package_name']} × 1";
            echo "<ul>";
            foreach ($package_items as $index => $pid) {
                $stmt = $pdo->prepare("SELECT mit.item_name, ppt.type_name 
                               FROM menu_item mi 
                               JOIN menu_item_translation mit ON mi.item_id = mit.item_id 
                               JOIN package_dish pd ON pd.item_id = mi.item_id
                               JOIN package_type_translation ppt ON pd.type_id = ppt.type_id AND ppt.type_language_code = 'en'
                               WHERE mi.item_id = ? AND mit.language_code = 'en' AND pd.package_id = ?");
                $stmt->execute([$pid, $package_id]);
                $item = $stmt->fetch();
                if ($item) {
                    $name = htmlspecialchars($item['item_name']);
                    $type = htmlspecialchars($item['type_name']);
                    
                    // 获取价格修改器
                    $modifier = isset($package_item_modifiers[$index]) ? (float)$package_item_modifiers[$index] : 0;
                    if ($modifier > 0) {
                        $modifierDisplay = ' (+$' . number_format($modifier, 2) . ')';
                    } elseif ($modifier < 0) {
                        $modifierDisplay = ' (-$' . number_format(abs($modifier), 2) . ')';
                    } else {
                        $modifierDisplay = '';
                    }
                    
                    echo "<li>$type: $name$modifierDisplay</li>";
                }
            }
            echo "</ul>";
            echo "</li>";
        }
    }
    echo "</ul>";
    echo "<p>Total Price: $" . number_format($totalPrice, 2) . "</p>";
    if($cid>0){
        echo "<p>You get {$totalPrice} points</p>";
    }

?>
</body>
</html>

