<?php
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "DB connection failed"]);
    exit;
}

$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
if ($cid === 0) {
    echo json_encode(["success" => true, "coupons" => []]);
    exit;
}

$sql = "
    SELECT c.coupon_id,
           c.title,
           c.description,
           c.points_required,
           c.type,
           c.discount_amount,
           c.item_category,
           DATE_FORMAT(c.expiry_date, '%Y-%m-%d') AS expiry_date,
           COUNT(r.redemption_id) AS quantity,
           MIN(r.redeemed_at) AS first_redeemed_at
    FROM coupon_redemptions r
    INNER JOIN coupons c ON r.coupon_id = c.coupon_id
    WHERE r.cid = ?
      AND r.is_used = 0
      AND c.is_active = 1
      AND (c.expiry_date IS NULL OR c.expiry_date >= CURDATE())
    GROUP BY c.coupon_id, c.title, c.description, c.points_required,
             c.type, c.discount_amount, c.item_category, c.expiry_date
    ORDER BY first_redeemed_at DESC
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result();

$coupons = [];
while ($row = $result->fetch_assoc()) {
    $coupons[] = [
        "coupon_id"       => (int)$row['coupon_id'],
        "title"           => $row['title'],
        "description"     => $row['description'],
        "points_required" => (int)$row['points_required'],
        "type"            => $row['type'],
        "discount_amount" => (int)$row['discount_amount'],
        "item_category"   => $row['item_category'],
        "expiry_date"     => $row['expiry_date'],
        "quantity"        => (int)$row['quantity']
    ];
}

echo json_encode(["success" => true, "coupons" => $coupons], JSON_UNESCAPED_UNICODE);

$stmt->close();
$conn->close();