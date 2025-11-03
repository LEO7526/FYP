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

$sql = "SELECT points FROM coupon_point WHERE cid = $cid";
$result = $conn->query($sql);

if ($row = $result->fetch_assoc()) {
    echo json_encode([
        "success" => true,
        "points" => (int)$row['points']
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success"=>false,"error"=>"Customer not found"]);
}

$conn->close();
