<?php
session_start();

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

$useremail = $_POST['useremail'] ?? '';
$password = $_POST['password'] ?? '';

if ($useremail && $password) {

    $stmt = $pdo->prepare("SELECT cid, cemail, cpassword, cname FROM customer WHERE cemail = :useremail");
    $stmt->execute([':useremail' => $useremail]);
    $user = $stmt->fetch();


    if ($user && $password === $user['cpassword']) {
        setcookie('user', $useremail, time() + 3600, '/');
        setcookie('cid', $user['cid'], time() + 3600, '/');
        setcookie('username', $user['cname'], time() + 3600, '/');
        header("Location: ../index/home.php");
        exit;
    } else {
        echo "<script>alert('Wrong email or password'); window.history.back();</script>";
    }
}
?>
