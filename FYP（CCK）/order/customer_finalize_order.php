<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Finalize Order - XXX Restaurant</title>
    <link rel="stylesheet" href="../Com.css">
    <link rel="stylesheet" href="finalize_order.css">
</head>
<body>
<?php
session_start();

// 取得訂單資料
$item_ids = $_POST['item_ids'] ?? [];
$quantities = $_POST['quantities'] ?? [];
$couponId = $_POST['coupon'] ?? null;

if (count($item_ids) !== count($quantities)) {
    die("Data error. Please resubmit your order.");
}

$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // 判斷會員或訪客
    $cid = isset($_POST['guest_checkout']) ? 0 : ($_SESSION['cid'] ?? ($_COOKIE['cid'] ?? 0));

    // 計算總數量與總金額
    $totalQty = 0;
    $totalPrice = 0;
    for ($i = 0; $i < count($item_ids); $i++) {
        $stmt = $pdo->prepare("SELECT item_price FROM menu_item WHERE item_id = ?");
        $stmt->execute([$item_ids[$i]]);
        $item = $stmt->fetch();
        if ($item) {
            $qty = (int)$quantities[$i];
            $price = (float)$item['item_price'];
            $totalQty += $qty;
            $totalPrice += $price * $qty;
        }
    }

    if ($cid > 0 && $couponId) {
        $stmt = $pdo->prepare("SELECT type, discount_amount, item_category FROM coupons WHERE coupon_id = ?");
        $stmt->execute([$couponId]);
        $coupon = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($coupon) {
            if ($coupon['type'] === 'percent') {
                $discount = $coupon['discount_amount'] / 100;
                echo "<h3>Order Details:</h3>";
                echo "<ul>";
                for ($i = 0; $i < count($item_ids); $i++) {
                    $stmt = $pdo->prepare("SELECT mit.item_name, mi.item_price, mi.image_url FROM menu_item mi JOIN menu_item_translation mit ON mi.item_id = mit.item_id WHERE mi.item_id = ? AND mit.language_code = 'en'");
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
                echo "<p>Total Price : $totalPrice</p>";
                echo "<p>Discount applied</p>";
                $totalPrice -= $totalPrice * $discount;
                echo "<p>New Total Price : $totalPrice</p>";
            } else if($coupon['type'] === 'cash') {
                $discount = $coupon['discount_amount']/100;
                echo "<h3>Order Details:</h3>";
                echo "<ul>";
                for ($i = 0; $i < count($item_ids); $i++) {
                    $stmt = $pdo->prepare("SELECT mit.item_name, mi.item_price, mi.image_url FROM menu_item mi JOIN menu_item_translation mit ON mi.item_id = mit.item_id WHERE mi.item_id = ? AND mit.language_code = 'en'");
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
                echo "<p>Total Price : $totalPrice</p>";
                echo "<p>Discount applied</p>";
                $totalPrice -= $discount;
                echo "<p>New Total Price : $totalPrice</p>";
            } else if($coupon['type'] === 'free_item') {
                if($coupon['item_category'] === 'drink'){
                    $minDrinkPrice = null;
                    for ($i=0; $i < count($item_ids); $i++) {
                        $stmt = $pdo->prepare("SELECT category_id, item_price FROM menu_item WHERE item_id = ?");
                        $stmt->execute([$item_ids[$i]]);
                        $item = $stmt->fetch(PDO::FETCH_ASSOC);
                        if ($item && $item['category_id'] == 5) {
                            $qty = (int)$quantities[$i];
                            $price = (float)$item['item_price'];

                            if ($minDrinkPrice === null || $price < $minDrinkPrice) {
                                $minDrinkPrice = $price;
                            }
                        }
                    }
                }
                echo "<h3>Order Details:</h3>";
                echo "<ul>";
                for ($i = 0; $i < count($item_ids); $i++) {
                    $stmt = $pdo->prepare("SELECT mit.item_name, mi.item_price, mi.image_url FROM menu_item mi JOIN menu_item_translation mit ON mi.item_id = mit.item_id WHERE mi.item_id = ? AND mit.language_code = 'en'");
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
                echo "<p>Total Price : $totalPrice</p>";
                echo "<p>Discount applied</p>";
                if ($minDrinkPrice !== null) {
                    $totalPrice -= $minDrinkPrice;
                }
                echo "<p>New Total Price : $totalPrice</p>";
            }
        }
    }

} catch (PDOException $e) {
    die("Database error: " . $e->getMessage());
}
echo "<form method='post' action='insertOrder.php'>";
    echo "<div>
            <label><input type='radio' name='order_type' value='takeaway' required> Take away</label>
            <label><input type='radio' name='order_type' value='dine_in'> Dine in</label>
          </div>";
    echo "<button type='submit'>Pay by AlipayHK</button>";
    echo "</form>";
$_SESSION['total_price'] = $totalPrice;
$_SESSION['coupon'] = $couponId;
?>



</body>
</html>
