<?php
$stmt = $pdo->prepare("UPDATE coupon_redemptions SET is_used = 1 WHERE cid = ? AND coupon_id = ?");
$stmt->execute([$cid, $couponId]);

$odate = date('Y-m-d H:i:s');
$orderRef = uniqid("ORD");
$stmt = $pdo->prepare("INSERT INTO orders (odate, cid, ostatus, orderRef, total_price) VALUES (?, ?, ?, ?, ?)");
$stmt->execute([$odate, $cid, 1, $orderRef, $totalPrice]);
$orderId = $pdo->lastInsertId();

for ($i = 0; $i < count($item_ids); $i++) {
    $stmt = $pdo->prepare("INSERT INTO order_items (oid, item_id, qty) VALUES (?, ?, ?)");
    $stmt->execute([$orderId, $item_ids[$i], $quantities[$i]]);
}

// 更新會員積分
if ($cid > 0) {
    $stmt = $pdo->prepare("UPDATE coupon_point SET points = points + ? WHERE cid = ?");
    $stmt->execute([$totalPrice, $cid]);
}


// …前面程式碼保持不變…

// 套用優惠券折扣（如果有選擇）
$discountedItem = null; // 用來記錄被折扣的商品名稱
if ($cid > 0 && $couponId) {
    $stmt = $pdo->prepare("SELECT discount_type, discount_value, item_category FROM coupons WHERE coupon_id = ?");
    $stmt->execute([$couponId]);
    $coupon = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($coupon) {
        if ($coupon['discount_type'] === 'percent') {
            $discount = $coupon['discount_value'] / 100;
            $totalPrice -= $totalPrice * $discount;
        } else if ($coupon['discount_type'] === 'cash') {
            $discount = $coupon['discount_value'];
            $totalPrice -= $discount;
        } else if ($coupon['discount_type'] === 'free_item') {
            if ($coupon['item_category'] === 'drink') {
                $minDrinkPrice = null;
                $minDrinkName = null;
                for ($i = 0; $i < count($item_ids); $i++) {
                    $stmt = $pdo->prepare("SELECT category_id, item_name, item_price FROM menu_item mi 
                                           JOIN menu_item_translation mit ON mi.item_id = mit.item_id 
                                           WHERE mi.item_id = ? AND mit.language_code = 'en'");
                    $stmt->execute([$item_ids[$i]]);
                    $item = $stmt->fetch(PDO::FETCH_ASSOC);
                    if ($item && $item['category_id'] == 5) {
                        $price = (float)$item['item_price'];
                        if ($minDrinkPrice === null || $price < $minDrinkPrice) {
                            $minDrinkPrice = $price;
                            $minDrinkName = $item['item_name'];
                        }
                    }
                }
                if ($minDrinkPrice !== null) {
                    $totalPrice -= $minDrinkPrice;
                    $discountedItem = $minDrinkName;
                }
            }
        }

        // 標記優惠券已使用
        $stmt = $pdo->prepare("UPDATE coupon_redemptions SET is_used = 1 WHERE cid = ? AND coupon_id = ?");
        $stmt->execute([$cid, $couponId]);
    }
}

// 建立訂單 …保持不變…

// 顯示結果
echo "<h2>Thank you! Your order has been placed.</h2>";
echo "<p>Order ID: #{$orderId}</p>";
echo "<p>Total Quantity: {$totalQty}</p>";
echo "<p>Final Price: $" . number_format($totalPrice, 2) . "</p>";

echo "<h3>Order Details:</h3>";
echo "<ul>";
for ($i = 0; $i < count($item_ids); $i++) {
    $stmt = $pdo->prepare("SELECT item_name, item_price FROM menu_item mi 
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

if ($discountedItem) {
    echo "<p><strong>Discount applied on item:</strong> {$discountedItem}</p>";
}

if ($cid > 0) {
    echo "<p>You earned {$totalPrice} points as a member.</p>";
}
echo "<a href='../Index/menu.php'>Back to Menu</a>";
