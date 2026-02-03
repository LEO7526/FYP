<?php
// 引用連線檔
require_once 'db_connect.php';

// 告訴瀏覽器或 App，回傳的是 JSON 格式
header('Content-Type: application/json');

// 檢查是否有收到 POST 請求
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    
    // 獲取 Android 傳來的參數
    $email = isset($_POST['email']) ? $_POST['email'] : '';
    $password = isset($_POST['password']) ? $_POST['password'] : '';

    if (empty($email) || empty($password)) {
        echo json_encode(array("status" => "error", "message" => "Missing email or password"));
        exit();
    }

    // 準備 SQL 語法 (防止 SQL Injection)
    // 你的資料庫欄位是 semail 和 spassword
    $stmt = $conn->prepare("SELECT sid, sname, srole, semail FROM staff WHERE semail = ? AND spassword = ?");
    $stmt->bind_param("ss", $email, $password);
    
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        // 帳密正確，取出資料
        $row = $result->fetch_assoc();
        
        echo json_encode(array(
            "status" => "success",
            "message" => "Login successful",
            "sid" => $row['sid'],
            "name" => $row['sname'],
            "role" => $row['srole']
        ));
    } else {
        // 帳密錯誤
        echo json_encode(array("status" => "error", "message" => "Invalid email or password"));
    }
    
    $stmt->close();
} else {
    echo json_encode(array("status" => "error", "message" => "Invalid Request Method"));
}

$conn->close();
?>