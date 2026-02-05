<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>XXX Restaurant</title>
    <link rel="stylesheet" href="../Com.css">
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
        Yummy Restaurant
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
<div></div>
</body>
</html>
