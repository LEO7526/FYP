<?php
// auth_check.php - Login status check utility
session_start();

// Check staff authentication
function check_staff_auth() {
    if (!isset($_SESSION['staff'])) {
        // 顯示彈出通知並重定向
        echo '<script>
            alert("Please log in first");
            window.location.href = "../staffLogin.html";
        </script>';
        exit();
    }
}
?>