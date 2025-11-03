<?php
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "DB connection failed"]);
    exit;
}

$couponId = isset($_GET['coupon_id']) ? intval($_GET['coupon_id']) : 0;
$lang     = isset($_GET['lang']) ? $conn->real_escape_string($_GET['lang']) : 'en';

if ($couponId <= 0) {
    echo json_encode(["success" => false, "error" => "Invalid coupon id"]);
    exit;
}

$sql = "SELECT c.coupon_id,
               c.points_required,
               c.type,
               c.discount_amount,
               c.item_category,
               DATE_FORMAT(c.expiry_date, '%Y-%m-%d') AS expiry_date,
               t.title,
               t.description
        FROM coupons c
        JOIN coupon_translation t ON c.coupon_id = t.coupon_id
        WHERE c.coupon_id = ? AND t.language_code = ?";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(["success" => false, "error" => "SQL prepare failed"]);
    exit;
}
$stmt->bind_param("is", $couponId, $lang);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    // Fetch terms
    $terms = [];
    $termSql = "SELECT term_text FROM coupon_terms WHERE coupon_id = ? AND language_code = ?";
    $termStmt = $conn->prepare($termSql);
    if ($termStmt) {
        $termStmt->bind_param("is", $couponId, $lang);
        $termStmt->execute();
        $termResult = $termStmt->get_result();
        while ($t = $termResult->fetch_assoc()) {
            $terms[] = $t['term_text'];
        }
        $termStmt->close();
    }

    echo json_encode([
        "success" => true,
        "coupon"  => [
            "coupon_id"      => (int)$row['coupon_id'],
            "title"          => $row['title'],
            "description"    => $row['description'],
            "requiredPoints" => (int)$row['points_required'],
            "type"           => $row['type'],
            "discountAmount" => (int)$row['discount_amount'],
            "itemCategory"   => $row['item_category'],
            "expiry_date"    => $row['expiry_date'],
            "terms"          => $terms
        ]
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "error" => "Coupon not found"]);
}

$stmt->close();
$conn->close();
