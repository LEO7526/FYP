<?php
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success"=>false,"error"=>"DB connection failed"]);
    exit;
}

$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
$lang = isset($_GET['lang']) ? $conn->real_escape_string($_GET['lang']) : 'en';

if ($cid <= 0) {
    echo json_encode(["success"=>false,"error"=>"Invalid customer id"]);
    exit;
}

$sql = "
    SELECT
        c.coupon_id,
        c.points_required,
        c.type,
        c.discount_amount,
        c.item_category,
        c.expiry_date,
        c.is_active,

        r.applies_to,
        r.discount_type,
        r.discount_value,
        r.min_spend,
        r.max_discount,
        r.per_customer_per_day,
        r.valid_dine_in,
        r.valid_takeaway,
        r.valid_delivery,
        r.combine_with_other_discounts,
        r.birthday_only,

        t.title,
        t.description,

        ai.applicable_items,
        ac.applicable_categories,
        tm.terms,

        rd.redemption_count,
        rd.first_redeemed_at
    FROM coupons c
    -- rules
    LEFT JOIN coupon_rules r
        ON c.coupon_id = r.coupon_id
    -- localized translation
    LEFT JOIN coupon_translation t
        ON c.coupon_id = t.coupon_id AND t.language_code = ?
    -- aggregated applicable items
    LEFT JOIN (
        SELECT coupon_id, GROUP_CONCAT(DISTINCT item_id ORDER BY item_id SEPARATOR '||') AS applicable_items
        FROM coupon_applicable_items
        GROUP BY coupon_id
    ) ai ON ai.coupon_id = c.coupon_id
    -- aggregated applicable categories
    LEFT JOIN (
        SELECT coupon_id, GROUP_CONCAT(DISTINCT category_id ORDER BY category_id SEPARATOR '||') AS applicable_categories
        FROM coupon_applicable_categories
        GROUP BY coupon_id
    ) ac ON ac.coupon_id = c.coupon_id
    -- aggregated localized terms
    LEFT JOIN (
        SELECT coupon_id, GROUP_CONCAT(DISTINCT term_text ORDER BY term_id SEPARATOR '||') AS terms
        FROM coupon_terms
        WHERE language_code = ?
        GROUP BY coupon_id
    ) tm ON tm.coupon_id = c.coupon_id
    -- aggregated redemptions for this customer (unused only)
    INNER JOIN (
        SELECT coupon_id,
               COUNT(DISTINCT redemption_id) AS redemption_count,
               MIN(redeemed_at) AS first_redeemed_at
        FROM coupon_redemptions
        WHERE cid = ? AND is_used = 0
        GROUP BY coupon_id
    ) rd ON rd.coupon_id = c.coupon_id
    WHERE c.is_active = 1
    ORDER BY rd.first_redeemed_at DESC, c.coupon_id ASC
";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(["success"=>false,"error"=>"Prepare failed: ".$conn->error]);
    exit;
}

// bind: translation lang, terms lang, cid for redemptions subquery
$stmt->bind_param("ssi", $lang, $lang, $cid);
$stmt->execute();
$result = $stmt->get_result();

$coupons = [];
while ($row = $result->fetch_assoc()) {
    // explode safe separator '||' and filter empty strings
    $items = isset($row['applicable_items']) && $row['applicable_items'] !== null && $row['applicable_items'] !== ''
        ? array_filter(explode('||', $row['applicable_items']), function($v){ return $v !== ''; })
        : [];
    $cats = isset($row['applicable_categories']) && $row['applicable_categories'] !== null && $row['applicable_categories'] !== ''
        ? array_filter(explode('||', $row['applicable_categories']), function($v){ return $v !== ''; })
        : [];
    $terms = isset($row['terms']) && $row['terms'] !== null && $row['terms'] !== ''
        ? array_filter(explode('||', $row['terms']), function($v){ return $v !== ''; })
        : [];

    // normalize types to int where appropriate
    $coupons[] = [
        "coupon_id"            => (int)$row['coupon_id'],
        "title"                => $row['title'],
        "description"          => $row['description'],
        "points_required"      => (int)$row['points_required'],
        "type"                 => $row['type'],
        "discount_amount"      => (int)$row['discount_amount'],  // cents or percent int
        "item_category"        => $row['item_category'],
        "expiry_date"          => $row['expiry_date'],

        "applies_to"           => $row['applies_to'],
        "discount_type"        => $row['discount_type'],
        "discount_value"       => $row['discount_value'],        // DECIMAL, keep as string or cast if needed
        "min_spend"            => $row['min_spend'],
        "max_discount"         => $row['max_discount'],
        "per_customer_per_day" => $row['per_customer_per_day'],
        "valid_dine_in"        => isset($row['valid_dine_in']) ? (int)$row['valid_dine_in'] : 0,
        "valid_takeaway"       => isset($row['valid_takeaway']) ? (int)$row['valid_takeaway'] : 0,
        "valid_delivery"       => isset($row['valid_delivery']) ? (int)$row['valid_delivery'] : 0,
        "combine_with_other_discounts" => isset($row['combine_with_other_discounts']) ? (int)$row['combine_with_other_discounts'] : 0,
        "birthday_only"        => isset($row['birthday_only']) ? (int)$row['birthday_only'] : 0,

        "applicable_items"     => $items,
        "applicable_categories"=> $cats,
        "terms"                => $terms,

        "redemption_count"     => (int)$row['redemption_count'],
        "first_redeemed_at"    => $row['first_redeemed_at'],
    ];
}

echo json_encode(["success"=>true,"coupons"=>$coupons], JSON_UNESCAPED_UNICODE);

$stmt->close();
$conn->close();
?>
