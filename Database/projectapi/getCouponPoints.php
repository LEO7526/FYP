<?php
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success"=>false,"error"=>"DB connection failed"]);
    exit;
}

$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
if ($cid <= 0) {
    echo json_encode(["success"=>false,"error"=>"Invalid customer id"]);
    exit;
}

$stmt = $conn->prepare("SELECT points FROM coupon_point WHERE cid = ?");
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    echo json_encode([
        "success" => true,
        "points" => (int)$row['points']
    ], JSON_UNESCAPED_UNICODE);
    $stmt->close();
} else {
    $stmt->close();
    // Customer not found in coupon_point table - initialize with 0 points
    // Using INSERT IGNORE to handle concurrent requests gracefully
    $insertStmt = $conn->prepare("INSERT IGNORE INTO coupon_point (cid, points) VALUES (?, 0)");
    $insertStmt->bind_param("i", $cid);
    $insertStmt->execute();
    $insertStmt->close();
    
    // Re-fetch to get actual points (in case INSERT IGNORE skipped due to race condition)
    $stmt = $conn->prepare("SELECT points FROM coupon_point WHERE cid = ?");
    $stmt->bind_param("i", $cid);
    $stmt->execute();
    $result = $stmt->get_result();
    $row = $result->fetch_assoc();
    $stmt->close();
    
    if ($row) {
        echo json_encode([
            "success" => true,
            "points" => (int)$row['points']
        ], JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode(["success"=>false,"error"=>"Failed to initialize customer points"]);
    }
}

$conn->close();
