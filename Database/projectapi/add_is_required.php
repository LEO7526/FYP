<?php
/**
 * 添加is_required欄位到item_customization_options表
 */
header('Content-Type: application/json');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["error" => "Connection failed: " . $conn->connect_error]);
    exit;
}

// 檢查欄位是否已存在
$checkResult = $conn->query("SHOW COLUMNS FROM item_customization_options LIKE 'is_required'");

if ($checkResult && $checkResult->num_rows > 0) {
    echo json_encode(["success" => true, "message" => "is_required column already exists"]);
} else {
    // 添加欄位
    $alterQuery = "ALTER TABLE item_customization_options ADD COLUMN is_required TINYINT(1) DEFAULT 0";
    
    if ($conn->query($alterQuery)) {
        echo json_encode(["success" => true, "message" => "is_required column added successfully"]);
    } else {
        echo json_encode(["error" => "Failed to add column: " . $conn->error]);
    }
}

$conn->close();
?>
