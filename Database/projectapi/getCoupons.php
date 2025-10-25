<?php
header('Content-Type: application/json');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success"=>false,"error"=>"DB connection failed"]);
    exit;
}

$sql = "SELECT coupon_id, title, description, points_required,
               DATE_FORMAT(expiry_date, '%Y-%m-%d') AS expiry_date,
               is_active
        FROM coupons
        WHERE is_active = 1
          AND (expiry_date IS NULL OR expiry_date >= CURDATE())";

$result = $conn->query($sql);

$coupons = [];
while ($row = $result->fetch_assoc()) {
    $coupons[] = [
        "coupon_id"       => (int)$row['coupon_id'],
        "title"           => $row['title'],
        "description"     => $row['description'],
        "points_required" => (int)$row['points_required'],
        "expiry_date"     => $row['expiry_date']
    ];
}

echo json_encode(["success"=>true,"coupons"=>$coupons], JSON_UNESCAPED_UNICODE);
$conn->close();