<?php
// C:\xampp\htdocs\androidstaff_api\inventory\db_connect.php

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "projectdb"; // 確認你的資料庫名稱

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Database connection failed: " . $conn->connect_error]));
}

$conn->set_charset("utf8mb4");
?>