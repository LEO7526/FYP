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

// 從 session 取出訂單資料
$item_ids = $_SESSION['order']['item_ids'] ?? [];
$quantities = $_SESSION['order']['quantities'] ?? [];

if (empty($item_ids) || empty($quantities)) {
    die("No order data found. Please resubmit your order.");
}

$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

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
    echo "<p>Total Price : $totalPrice</p>";

    $_SESSION['total_price'] = $totalPrice;
    // 用 form 包起來
    echo "<form method='post' action='payment_choose.php'>";
    echo "<div>
            <label><input type='radio' name='order_type' value='takeaway' id='order_type' required> Take away</label>
            <label><input type='radio' name='order_type' value='dine_in' id='order_type' required> Dine in</label>
          </div>";
    echo "<button type='submit' name='payment' value='1'>Pay by credit card</button>";
    echo "<button type='submit' name='payment' value='0'>Pay to the font disk</button>";
    echo "</form>";


} catch (PDOException $e) {
    die("Database error: " . $e->getMessage());
}
?>


</body>
</html>
