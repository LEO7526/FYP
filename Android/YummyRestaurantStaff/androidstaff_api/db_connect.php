<?php
$servername = "localhost";
$username = "root";      // XAMPP 預設帳號
$password = "";          // XAMPP 預設密碼是空字串
$dbname = "projectdb";   // 你的資料庫名稱

// 建立連線
$conn = new mysqli($servername, $username, $password, $dbname);

// 檢查連線
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// 設定編碼為 UTF-8，避免中文亂碼
$conn->set_charset("utf8mb4");
?>