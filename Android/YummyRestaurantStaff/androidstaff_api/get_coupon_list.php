<?php
// get_coupon_list.php
require_once 'db_connect.php';
header('Content-Type: application/json');

$coupons = [];

// 抓取優惠券 (包含翻譯的英文標題)
$sql = "SELECT c.coupon_id, c.points_required, c.type, c.discount_amount, 
               t.title, c.expiry_date
        FROM coupons c
        LEFT JOIN coupon_translation t ON c.coupon_id = t.coupon_id AND t.language_code = 'en'
        WHERE c.is_active = 1
        ORDER BY c.coupon_id DESC";

$result = $conn->query($sql);

while ($row = $result->fetch_assoc()) {
    $title = $row['title'] ?? "No English Title";
    $discount = "";
    
    if ($row['type'] == 'percent') {
        $discount = $row['discount_amount'] . "% OFF";
    } elseif ($row['type'] == 'cash') {
        $discount = "$" . $row['discount_amount'] . " OFF";
    } else {
        $discount = "Free Item";
    }

    $coupons[] = [
        "id" => $row['coupon_id'],
        "title" => $title,
        "points" => $row['points_required'],
        "discount" => $discount,
        "expiry" => $row['expiry_date']
    ];
}

echo json_encode(["status" => "success", "data" => $coupons]);
$conn->close();
?>