<?php
session_start();

// 清除 cookie
if (isset($_COOKIE['user'])) {
    setcookie('user', '', time() - 3600, '/');
    setcookie('cid', '', time() + 3600, '/');
    setcookie('username', '', time() + 3600, '/');
}


session_unset();
session_destroy();

header("Location: ../index/home.php");
exit();
?>

