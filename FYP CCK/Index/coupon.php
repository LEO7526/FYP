<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>XXX Restaurant--Coupon</title>
    <link rel="stylesheet" href="../Com.css">
    <link rel="stylesheet" href="../index/coupon.css">
    <script>
        function openLogin() {
            document.getElementById("loginPopup").style.display = "flex";
        }

        function closeLogin() {
            document.getElementById("loginPopup").style.display = "none";
        }
    </script>
    <script>
        function openRedeemPopup(couponId, title) {
            document.getElementById("popup-coupon").value = couponId;
            document.getElementById("redeemPopup").style.display = "flex";
        }

        function closeRedeemPopup() {
            document.getElementById("redeemPopup").style.display = "none";
        }
    </script>

</head>
<body>
<div class="header">
    <a href="../Index/home.php">
        Yummy Restaurant
    </a>
    <ul>
        <li><a href="../Index/menu.php">MENU</a></li>
        <li><a href="../Index/reservation.php">RESERVATION</a></li>
        <li><a href="../order/order.php">ORDER</a></li>
        <li>
            <a href="../Index/coupon.php">COUPON</a>
        </li>
    </ul>

    <?php
    if (isset($_COOKIE['user'])) {
        $username = ($_COOKIE['username']);
        echo '<div class="user-bar">
            <a class="username" href="profile.php">Hello,' . $username . '</a>
            <a href="../login/logout.php" class="logout-btn">LOGOUT</a>
          </div>';
    } else {
        echo '<a href="#" class="login-btn" onclick="openLogin()">LOGIN</a>';
    }
    ?>


    <div class="login-popup" id="loginPopup">
        <div class="login-box">
            <span class="close-btn" onclick="closeLogin()">&times;</span>
            <h2>Login</h2>
            <form method="post" action="../login/login.php">
                <label for="useremail">Email:</label>
                <input type="text" id="useremail" name="useremail" required>

                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required>

                <button type="submit">Login</button>
            </form>

            <!-- Sign Up button -->
            <div class="register-link">
                <p>Don't have an account? <a href="../register/register.php">Sign Up</a></p>
            </div>
        </div>
    </div>


</div>
<div>
    <?php
    if (!isset($_COOKIE['user'])) {
        echo '<h3 style="text-align: center">Please login to view your point<h3>';
    } else {
        $host = 'localhost';
        $dbname = 'projectdb';
        $user = 'root';
        $pass = '';
        $cid = $_COOKIE['cid'];
        try {
            $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

            // 先查會員積分
            $stmt = $pdo->prepare("SELECT coupon_point FROM customer WHERE cid = ?");
            $stmt->execute([$_COOKIE['cid']]);   // 這裡用 cookie 裡的 cid
            $memberPoints = $stmt->fetchColumn();

            $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

            // 顯示客人已擁有的 coupon
            $stmt = $pdo->prepare("SELECT cr.coupon_id, ct.title, ct.description 
                       FROM coupon_redemptions cr 
                       JOIN coupon_translation ct ON cr.coupon_id = ct.coupon_id 
                       WHERE cr.cid = ? AND ct.language_code = 'en'");
            $stmt->execute([$_COOKIE['cid']]);
            $ownedCoupons = $stmt->fetchAll(PDO::FETCH_ASSOC);

            if ($ownedCoupons) {
                // 用 coupon_id 分組並計算數量
                $couponCounts = [];
                foreach ($ownedCoupons as $oc) {
                    $id = $oc['coupon_id'];
                    if (!isset($couponCounts[$id])) {
                        $couponCounts[$id] = [
                                'title' => htmlspecialchars($oc['title']),
                                'desc'  => htmlspecialchars($oc['description']),
                                'count' => 1
                        ];
                    } else {
                        $couponCounts[$id]['count']++;
                    }
                }

                echo "<div style='margin:20px; font-size:18px; color:#2c3e50; text-align: center;'>
            <strong>Your Coupons:</strong>
          </div>";
                echo "<div class='owned-coupons'>";
                foreach ($couponCounts as $coupon) {
                    echo "<div class='coupon-owned'>
                {$coupon['title']} - {$coupon['desc']} × {$coupon['count']}
              </div>";
                }
                echo "</div>";
            } else {
                echo "<div style='text-align:center; color:#888;'>You don't have any coupons yet.</div>";
            }

            echo "<div style='margin:20px; font-size:18px; color:#2c3e50; text-align: center;'>
            <strong>Your point: {$memberPoints}</strong>
          </div>";


            $stmt = $pdo->prepare("SELECT c.coupon_id, ct.title, ct.description, c.points_required
                           FROM coupons c, coupon_translation ct
                           WHERE c.coupon_id = ct.coupon_id AND ct.language_code = 'en'");
            $stmt->execute();
            $coupons = $stmt->fetchAll(PDO::FETCH_ASSOC);
            if ($coupons){
                foreach ($coupons as $c){
                    $couponId = htmlspecialchars($c['coupon_id']);
                    $title = htmlspecialchars($c['title']);
                    $desc = htmlspecialchars($c['description']);
                    $points = htmlspecialchars($c['points_required']);

                    echo '<div class="coupon-container">';
                    echo '<button type="button" onclick="openRedeemPopup(\''.$couponId.'\', \''.$title.'\')" 
      class="coupon-btn">';
                    echo $title.' - '.$desc.' (Points Required: '.$points.')';
                    echo '</button>';
                    echo '</div>';


                }
            }

        } catch (PDOException $e) {
            die("Database error: " . $e->getMessage());
        }
    }
    ?>
</div>
<div id="redeemPopup" class="popup" style="display:none;">
    <div class="popup-box">
        <span class="close-btn" onclick="closeRedeemPopup()">&times;</span>
        <h2>Redeem Coupon</h2>
        <form method="post" action="redeem_coupon.php">
            <input type="hidden" id="popup-coupon" name="coupon">
            <label for="quantity">Quantity:</label>
            <input type="number" id="popup-quantity" name="quantity" value="1" min="1" required>
            <div class="popup-buttons">
                <button type="submit">Confirm Redeem</button>
                <button type="button" class="cancel-btn" onclick="closeRedeemPopup()">Close</button>
            </div>
        </form>
    </div>
</div>


</body>
</html>
