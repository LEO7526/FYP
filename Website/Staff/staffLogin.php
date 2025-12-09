<?php
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    session_start();
    include 'conn.php';

    extract($_POST);

    $staffLoginSql = "SELECT * FROM staff WHERE sid = '$uid' AND spassword = '$password'";
    $rs = mysqli_query($conn, $staffLoginSql);

    if (mysqli_num_rows($rs) === 1) {
        $user = mysqli_fetch_assoc($rs);
        $_SESSION['staff'] = $user;
        header('Location: staff/staffIndex.php');
        exit();
    } else {
        echo '<script>alert("Invalid Staff ID or Password.");
        window.location.href = "staffLogin.html";</script>';
    }
}
?>