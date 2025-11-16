<?php
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit;
}

$cid = isset($_POST['cid']) ? intval($_POST['cid']) : 0;
$couponId = isset($_POST['coupon_id']) ? intval($_POST['coupon_id']) : 0;
// optional: pass current order total and order type from client
$orderTotal = isset($_POST['order_total']) ? floatval($_POST['order_total']) : 0.0;
$orderType  = isset($_POST['order_type']) ? $_POST['order_type'] : "dine_in"; // fallback

if ($cid <= 0 || $couponId <= 0) {
    echo json_encode(["success" => false, "error" => "Invalid parameters"]);
    exit;
}

// --- Load coupon rules ---
$ruleStmt = $conn->prepare("
    SELECT applies_to, discount_type, discount_value, min_spend, max_discount,
           per_customer_per_day, valid_dine_in, valid_takeaway, valid_delivery,
           combine_with_other_discounts, birthday_only
    FROM coupon_rules
    WHERE coupon_id = ?
    LIMIT 1
");
$ruleStmt->bind_param("i", $couponId);
$ruleStmt->execute();
$rule = $ruleStmt->get_result()->fetch_assoc();
$ruleStmt->close();

if (!$rule) {
    echo json_encode(["success" => false, "message" => "Coupon rules not found"]);
    exit;
}

// --- Validate min spend ---
if ($rule['min_spend'] !== null && $orderTotal < floatval($rule['min_spend'])) {
    echo json_encode(["success" => false, "message" => "Minimum spend HK$".$rule['min_spend']." required"]);
    exit;
}

// --- Validate order type ---
$validChannel = false;
if ($orderType === "dine_in"   && intval($rule['valid_dine_in'])   === 1) $validChannel = true;
if ($orderType === "takeaway"  && intval($rule['valid_takeaway'])  === 1) $validChannel = true;
if ($orderType === "delivery"  && intval($rule['valid_delivery'])  === 1) $validChannel = true;

if (!$validChannel) {
    echo json_encode(["success" => false, "message" => "Coupon not valid for ".$orderType]);
    exit;
}

// --- Validate birthday-only ---
if (intval($rule['birthday_only']) === 1) {
    // load customer birthday
    $custStmt = $conn->prepare("SELECT cbirthday FROM customer WHERE cid=?");
    $custStmt->bind_param("i", $cid);
    $custStmt->execute();
    $cust = $custStmt->get_result()->fetch_assoc();
    $custStmt->close();

    if (!$cust || empty($cust['cbirthday'])) {
        echo json_encode(["success" => false, "message" => "Birthday not set for customer"]);
        exit;
    }
    $today = date("m-d");
    if ($cust['cbirthday'] !== $today) {
        echo json_encode(["success" => false, "message" => "Coupon valid only on birthday"]);
        exit;
    }
}

// --- Validate applicable items/categories ---
$eligible = true;
$resItems = $conn->query("SELECT item_id FROM coupon_applicable_items WHERE coupon_id=".$couponId);
$resCats  = $conn->query("SELECT category_id FROM coupon_applicable_categories WHERE coupon_id=".$couponId);

if ($resItems->num_rows > 0 || $resCats->num_rows > 0) {
    $eligible = false;
    // TODO: check current cart items against these IDs/categories
    // For now, assume client passes eligible_item_ids[]
    if (isset($_POST['eligible_item_ids'])) {
        $cartItems = array_map('intval', $_POST['eligible_item_ids']);
        while ($row = $resItems->fetch_assoc()) {
            if (in_array(intval($row['item_id']), $cartItems)) $eligible = true;
        }
        while ($row = $resCats->fetch_assoc()) {
            // you’d need to map cart item category_ids here
        }
    }
}
if (!$eligible) {
    echo json_encode(["success" => false, "message" => "No eligible items in cart for this coupon"]);
    exit;
}

// --- Finally mark redemption as used ---
$stmt = $conn->prepare("
    UPDATE coupon_redemptions
    SET is_used = 1, used_at = NOW()
    WHERE cid = ? AND coupon_id = ? AND is_used = 0
    LIMIT 1
");
$stmt->bind_param("ii", $cid, $couponId);
$stmt->execute();

if ($stmt->affected_rows > 0) {
    echo json_encode([
        "success" => true,
        "message" => "Coupon applied successfully"
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode([
        "success" => false,
        "message" => "Coupon not available or already used"
    ], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>
