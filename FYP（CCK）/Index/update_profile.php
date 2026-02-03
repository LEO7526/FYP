<?php
session_start();
$pdo = new PDO("mysql:host=localhost;dbname=projectdb;charset=utf8", "root", "");

$currentUser = $_COOKIE['user'] ?? '';
$newUsername = $_POST['username'];
$oldPassword = $_POST['old_password'];
$newPassword = $_POST['new_password'];
$confirmPassword = $_POST['confirm_password'];
$phone = $_POST['phone'];

// 取得原始使用者資料
$stmt = $pdo->prepare("SELECT * FROM staff WHERE sname = ?");
$stmt->execute([$currentUser]);
$user = $stmt->fetch();

// 驗證舊密碼
if ($oldPassword && $oldPassword !== $user['spassword'])
 {
    echo "<script>alert('舊密碼錯誤'); window.history.back();</script>";
    exit;
}

// 驗證新密碼一致性
if ($newPassword && $newPassword !== $confirmPassword) {
    echo "<script>alert('新密碼不一致'); window.history.back();</script>";
    exit;
}

// 更新資料
if ($newPassword) {
    $stmt = $pdo->prepare("UPDATE staff SET sname = ?, spassword = ?, stel = ? WHERE sname = ?");
    $stmt->execute([$newUsername, $newPassword, $phone, $currentUser]);

} else {
    $stmt = $pdo->prepare("UPDATE staff SET sname = ?, phone = ? WHERE sname = ?");
    $stmt->execute([$newUsername, $phone, $currentUser]);
}

// 更新 cookie
setcookie('user', $newUsername, time() + 3600, '/');
header("Location: profile.php");
exit;
?>
