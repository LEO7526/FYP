<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login2</title>
    <link rel="stylesheet" href="login2.css">
</head>
<body>
<div class="login-popup" id="loginPopup">
    <div class="login-box">
        <span class="close-btn" onclick="closeLogin()">&times;</span>
        <h2>Login</h2>
        <form method="post" action="login2.php">
            <label for="useremail">Email:</label>
            <input type="text" id="useremail" name="useremail" required>

            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>

            <button type="submit">Submit</button>
        </form>
    </div>
</div>
</body>
</html>

<?php

$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("資料庫連線失敗：" . $e->getMessage());
}

session_start();
$item_ids = $_SESSION['order']['item_ids'] ?? [];
$quantities = $_SESSION['order']['quantities'] ?? [];
$spice_levels = $_SESSION['order']['spice_levels'] ?? [];
$remarks = $_SESSION['order']['remarks'] ?? [];
$useremail = $_POST['useremail'] ?? '';
$password = $_POST['password'] ?? '';
if ($useremail && $password) {
    $stmt = $pdo->prepare("SELECT cid, cemail, cpassword, cname FROM customer WHERE cemail = :useremail");
    $stmt->execute([':useremail' => $useremail]);
    $user = $stmt->fetch();

    if ($user && $password === $user['cpassword']) {
        $_SESSION['cid'] = $user['cid'];
        setcookie('user', $useremail, time() + 3600, '/');
        setcookie('cid', $user['cid'], time() + 3600, '/');
        setcookie('username', $user['cname'], time() + 3600, '/');
            header("Location: ../order/select_coupon.php");
        exit;
    } else {
        echo "<script>alert('Wrong email or password'); window.history.back();</script>";
    }
}

?>
