<?php
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    session_start();
    include 'conn.php';

    extract($_POST);

    $customerLoginSql = "SELECT * FROM customer WHERE cid = '$uid' AND cpassword = '$password'";
    $rs = mysqli_query($conn, $customerLoginSql);

    if (mysqli_num_rows($rs) === 1) {
        $user = mysqli_fetch_assoc($rs);
        $_SESSION['customer'] = $user;
        header('Location: customer/Index.php');
        exit();
    } else {
        echo '<script>alert("Invalid customer ID or Password.");
        window.location.href = "customerLogin.html";</script>';
    }
}
?>