<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sign Up - XXX Restaurant</title>
    <link rel="stylesheet" href="../Com.css">
</head>
<body>
<div class="register-container">
    <h2>Create Your Account</h2>
    <form method="post" action="register.php">
        <label for="email">Email Address:</label>
        <input type="email" id="email" name="email" required pattern="[^@ \t\r\n]+@[^@ \t\r\n]+\.[^@ \t\r\n]+" title="Please enter a valid email address">

        <label for="fullname">Full Name:</label>
        <input type="text" id="fullname" name="fullname" required>

        <label for="password">Password:</label>
        <input type="password" id="password" name="password" required>

        <label for="confirm_password">Confirm Password:</label>
        <input type="password" id="confirm_password" name="confirm_password" required>

        <label for="phone">Phone Number:</label>
        <input type="tel" id="phone" name="phone" required pattern="\d{8}" title="Phone number must be exactly 8 digits">

        <button type="submit">Sign Up</button>
    </form>
</div>
</body>
</html>

<?php
session_start();
$pdo = new PDO("mysql:host=localhost;dbname=projectdb", "root", "");
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'];
    $fullname = $_POST['fullname'];
    $password = $_POST['password'];
    $confirm_password = $_POST['confirm_password'];
    $phone = $_POST['phone'];

    if ($email && $fullname && $password && $confirm_password && $phone) {
        if ($password == $confirm_password) {
            $stmt = $pdo->prepare("SELECT * FROM customer WHERE cemail = :email");
            $stmt->execute([':email' => $email]);
            $registered_user = $stmt->fetch();
            if ($registered_user) {
                echo "<script>alert('Registered email'); window.location.href='home.php';</script>";
            } else {
                $stmt = $pdo->prepare("insert into customer (cname, cpassword, cemail, crole) values (?, ?, ?, ?)");
                $stmt->execute([$fullname, $password, $email, "customer"]);
                echo "<script>alert('registration successful'); window.location.href='home.php';</script>";
            }
        } else {
            echo "<script>alert('Passwords do not match'); window.location.href='register.php';</script>";
        }
    }
}
?>

