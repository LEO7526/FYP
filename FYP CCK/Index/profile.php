<?php
session_start();

$pdo = new PDO("mysql:host=localhost;dbname=projectdb;charset=utf8", "root", "");

// get user account
$username = $_COOKIE['user'] ?? '';
if (!$username) {
    header("Location: home.php");
    exit;
}

// get user profile
$stmt = $pdo->prepare("SELECT * FROM customer WHERE cname = ?");
$stmt->execute([$username]);
$user = $stmt->fetch();
?>

<!DOCTYPE html>
<html lang="zh-Hant">
<head>
    <meta charset="UTF-8">
    <title>Change Profile</title>
    <link rel="stylesheet" href="../Com.css">
    <link rel="stylesheet" href="profile.css">
</head>
<body>
<div class="profile-box">
    <h2>Profile</h2>
    <form method="post" action="update_profile.php">
        <label>User Name：</label>
        <input type="text" name="username" value="<?= htmlspecialchars($user['cname']) ?>" required>

        <label>Old Password：</label>
        <input type="password" name="old_password">

        <label>New Password：</label>
        <input type="password" name="new_password">

        <label>Confirm New Password：</label>
        <input type="password" name="confirm_password">

        <label>Tel：</label>
        <input type="text" name="phone" value="<?= htmlspecialchars($user['stel'] ?? '') ?>">

        <button type="submit">Save</button>
    </form>

</div>
</body>
</html>

