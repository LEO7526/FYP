<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>XXX Restaurant--Coupon</title>
    <link rel="stylesheet" href="../Com.css">
    <link rel="stylesheet" href="coupon.css">
    <script>
        function openLogin() {
            document.getElementById("loginPopup").style.display = "flex";
        }

        function closeLogin() {
            document.getElementById("loginPopup").style.display = "none";
        }
    </script>
</head>
<body>
<div class="header">
    <a href="home.php">
        <img src="../Image/logo.png" alt="Logo">
    </a>
    <ul>
        <li><a href="menu.php">MENU</a></li>
        <li><a href="reservation.php">RESERVATION</a></li>
        <li><a href="../order/order.php">ORDER</a></li>
        <li>
            <a href="coupon.php">COUPON</a>
        </li>
    </ul>

    <?php
    if (isset($_COOKIE['user'])) {
        echo '<a href="profile.php" class="avatar-link">
            <img src="../Image/head.jpg" alt="Member Avatar" class="avatar">
          </a>';
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
        echo 'Please login to view your point';
    } else {
        $host = 'localhost';
        $dbname = 'projectdb';
        $user = 'root';
        $pass = '';

        try {
            $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

            // 先查會員積分
            $stmt = $pdo->prepare("SELECT coupon_point FROM customer WHERE cid = ?");
            $stmt->execute([$_COOKIE['cid']]);   // 這裡用 cookie 裡的 cid
            $memberPoints = $stmt->fetchColumn();

            echo "<div style='margin:20px; font-size:18px; color:#2c3e50;'>
                <strong>Your Points:</strong> {$memberPoints}
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

                    echo '<form method="post" action="redeem_coupon.php" style="margin-bottom:10px;" onsubmit="return confirm(\'Do you want to redeem this coupon: '.$title.'?\')">';
                    echo '<input type="hidden" name="coupon" value="'.$couponId.'">';
                    echo '<button type="submit" style="padding:10px 20px; background:#f39c12; color:#fff; border:none; border-radius:6px;">';
                    echo $title.' - '.$desc.' (Points Required: '.$points.')';
                    echo '</button>';
                    echo '</form>';
                }
            }

        } catch (PDOException $e) {
            die("Database error: " . $e->getMessage());
        }
    }
    ?>
</div>
</body>
</html>
