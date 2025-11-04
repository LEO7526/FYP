<?php
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success"=>false,"error"=>"DB connection failed"]);
    exit;
}

$lang = isset($_GET['lang']) ? $conn->real_escape_string($_GET['lang']) : 'en';

$sql = "SELECT c.coupon_id,
               c.points_required,
               c.type,
               c.discount_amount,
               c.item_category,
               DATE_FORMAT(c.expiry_date, '%Y-%m-%d') AS expiry_date,
               t.title,
               t.description,
               r.applies_to,
               r.discount_type,
               r.discount_value,
               r.min_spend,
               r.valid_dine_in,
               r.valid_takeaway,
               r.valid_delivery,
               r.combine_with_other_discounts,
               r.birthday_only
        FROM coupons c
        JOIN coupon_translation t ON c.coupon_id = t.coupon_id
        LEFT JOIN coupon_rules r ON c.coupon_id = r.coupon_id
        WHERE c.is_active = 1
          AND (c.expiry_date IS NULL OR c.expiry_date >= CURDATE())
          AND t.language_code = '$lang'";

$result = $conn->query($sql);

$coupons = [];
if ($result) {
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
            "applies_to"      => $row['applies_to'],
            "discount_type"   => $row['discount_type'],
            "discount_value"  => $row['discount_value'],
            "min_spend"       => $row['min_spend'],
            "valid_dine_in"   => (bool)$row['valid_dine_in'],
            "valid_takeaway"  => (bool)$row['valid_takeaway'],
            "valid_delivery"  => (bool)$row['valid_delivery'],
            "combine_with_other_discounts" => (bool)$row['combine_with_other_discounts'],
            "birthday_only"   => (bool)$row['birthday_only']
        ];
    }
}

echo json_encode(["success"=>true,"coupons"=>$coupons], JSON_UNESCAPED_UNICODE);
$conn->close();