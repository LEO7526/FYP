<?php
// create_coupon_api.php
require_once 'db_connect.php';
header('Content-Type: application/json');

// 接收 JSON 輸入
$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (!$data) {
    echo json_encode(["status" => "error", "message" => "Invalid JSON"]);
    exit();
}

$conn->begin_transaction();

try {
    // 1. 插入 coupons 表
    $points = $data['points_required'];
    $type = $data['discount_type'];
    $amount = $data['discount_value'];
    $expiry = $data['expiry_date'];
    $active = 1;

    $stmt = $conn->prepare("INSERT INTO coupons (points_required, type, discount_amount, expiry_date, is_active) VALUES (?, ?, ?, ?, ?)");
    $stmt->bind_param("isssi", $points, $type, $amount, $expiry, $active);
    $stmt->execute();
    $coupon_id = $conn->insert_id;
    $stmt->close();

    // 2. 插入 coupon_rules 表
    $applies = $data['applies_to'];
    // 處理可選數值 (如果 App 傳 -1 或 null，資料庫存 NULL)
    $min_spend = (!empty($data['min_spend'])) ? $data['min_spend'] : null;
    $max_dist = (!empty($data['max_discount'])) ? $data['max_discount'] : null;
    $limit = (!empty($data['per_customer_limit'])) ? $data['per_customer_limit'] : null;
    
    $dine = $data['valid_dine_in'] ? 1 : 0;
    $take = $data['valid_takeaway'] ? 1 : 0;
    $deliv = $data['valid_delivery'] ? 1 : 0;
    $combo = $data['combine_discount'] ? 1 : 0;
    $bday = $data['birthday_only'] ? 1 : 0;

    $stmt = $conn->prepare("INSERT INTO coupon_rules (coupon_id, applies_to, discount_type, discount_value, min_spend, max_discount, per_customer_per_day, valid_dine_in, valid_takeaway, valid_delivery, combine_with_other_discounts, birthday_only) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("issddidiiiii", $coupon_id, $applies, $type, $amount, $min_spend, $max_dist, $limit, $dine, $take, $deliv, $combo, $bday);
    $stmt->execute();
    $stmt->close();

    // 3. 插入翻譯與條款 (Translations & Terms)
    $languages = ['en', 'zh-CN', 'zh-TW'];
    
    foreach ($languages as $lang) {
        if (isset($data['translations'][$lang])) {
            $trans = $data['translations'][$lang];
            $title = $trans['title'];
            $desc = $trans['description'];
            
            // 插入翻譯
            $stmt = $conn->prepare("INSERT INTO coupon_translation (coupon_id, language_code, title, description) VALUES (?, ?, ?, ?)");
            $stmt->bind_param("isss", $coupon_id, $lang, $title, $desc);
            $stmt->execute();
            $stmt->close();

            // 插入條款
            if (isset($trans['terms']) && is_array($trans['terms'])) {
                $stmtTerm = $conn->prepare("INSERT INTO coupon_terms (coupon_id, language_code, term_text) VALUES (?, ?, ?)");
                foreach ($trans['terms'] as $term) {
                    if (!empty(trim($term))) {
                        $stmtTerm->bind_param("iss", $coupon_id, $lang, $term);
                        $stmtTerm->execute();
                    }
                }
                $stmtTerm->close();
            }
        }
    }

    // 4. 插入適用範圍 (Scope)
    if ($applies == 'category' && !empty($data['selected_ids'])) {
        $stmt = $conn->prepare("INSERT INTO coupon_applicable_categories (coupon_id, category_id) VALUES (?, ?)");
        foreach ($data['selected_ids'] as $id) {
            $stmt->bind_param("ii", $coupon_id, $id);
            $stmt->execute();
        }
        $stmt->close();
    } elseif ($applies == 'item' && !empty($data['selected_ids'])) {
        $stmt = $conn->prepare("INSERT INTO coupon_applicable_items (coupon_id, item_id) VALUES (?, ?)");
        foreach ($data['selected_ids'] as $id) {
            $stmt->bind_param("ii", $coupon_id, $id);
            $stmt->execute();
        }
        $stmt->close();
    } elseif ($applies == 'package' && !empty($data['selected_ids'])) {
        $stmt = $conn->prepare("INSERT INTO coupon_applicable_package (coupon_id, package_id) VALUES (?, ?)");
        foreach ($data['selected_ids'] as $id) {
            $stmt->bind_param("ii", $coupon_id, $id);
            $stmt->execute();
        }
        $stmt->close();
    }

    $conn->commit();
    echo json_encode(["status" => "success", "message" => "Coupon Created!"]);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}

$conn->close();
?>