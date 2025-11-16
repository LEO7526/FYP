<?php
header('Content-Type: application/json; charset=utf-8');

// Only log errors, not notices/warnings (prevents malformed JSON output)
error_reporting(E_ERROR | E_PARSE);

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit;
}

$cid = isset($_POST['cid']) ? intval($_POST['cid']) : 0;
$orderTotal = isset($_POST['order_total']) ? floatval($_POST['order_total'])/100.0 : 0.0; // ✅ convert cents to HKD
$orderType  = isset($_POST['order_type']) ? $_POST['order_type'] : "dine_in";

if ($cid <= 0) {
    echo json_encode(["success" => false, "error" => "Invalid customer ID"]);
    exit;
}

// --- Debug incoming payload ---
error_log("=== Incoming coupon usage request ===");
error_log("cid=" . $cid);
error_log("order_total(HKD)=" . $orderTotal);
error_log("coupon_quantities=" . (isset($_POST['coupon_quantities']) ? print_r($_POST['coupon_quantities'], true) : 'none'));
error_log("eligible_item_ids=" . (isset($_POST['eligible_item_ids']) ? print_r($_POST['eligible_item_ids'], true) : 'none'));

// --- Validate coupon_quantities array ---
if (!isset($_POST['coupon_quantities']) || !is_array($_POST['coupon_quantities'])) {
    echo json_encode(["success" => false, "error" => "No coupons provided"]);
    exit;
}

$couponQuantities = $_POST['coupon_quantities'];
$results = [];

foreach ($couponQuantities as $couponId => $qty) {
    $couponId = intval($couponId);
    $qty = intval($qty);

    error_log("Processing couponId=$couponId qty=$qty");

    if ($couponId <= 0 || $qty <= 0) {
        $results[$couponId] = "Invalid coupon/quantity";
        continue;
    }

    // --- Load coupon rules ---
    $ruleStmt = $conn->prepare("
        SELECT applies_to, discount_type, discount_value, min_spend,
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
        error_log("❌ Rules not found for couponId=$couponId");
        $results[$couponId] = "Rules not found";
        continue;
    }
    error_log("Rule loaded: " . json_encode($rule));

    // --- Validate min spend ---
    if ($rule['min_spend'] !== null && $orderTotal < floatval($rule['min_spend'])) {
        error_log("❌ Min spend failed: required=" . $rule['min_spend'] . " actual=" . $orderTotal);
        $results[$couponId] = "Minimum spend HK$".$rule['min_spend']." required";
        continue;
    }

    // --- Validate order type ---
    $validChannel = false;
    if ($orderType === "dine_in"   && intval($rule['valid_dine_in'])   === 1) $validChannel = true;
    if ($orderType === "takeaway"  && intval($rule['valid_takeaway'])  === 1) $validChannel = true;
    if ($orderType === "delivery"  && intval($rule['valid_delivery'])  === 1) $validChannel = true;

    error_log("OrderType=" . $orderType . " validChannel=" . ($validChannel ? "yes" : "no"));

    if (!$validChannel) {
        $results[$couponId] = "Coupon not valid for ".$orderType;
        continue;
    }

    // --- Validate birthday-only ---
    if (intval($rule['birthday_only']) === 1) {
        $custStmt = $conn->prepare("SELECT cbirthday FROM customer WHERE cid=?");
        $custStmt->bind_param("i", $cid);
        $custStmt->execute();
        $cust = $custStmt->get_result()->fetch_assoc();
        $custStmt->close();

        $today = date("m-d");
        error_log("Birthday-only check: customerBirthday=" . ($cust['cbirthday'] ?? 'null') . " today=" . $today);

        if (!$cust || empty($cust['cbirthday'])) {
            $results[$couponId] = "Birthday not set for customer";
            continue;
        }
        if ($cust['cbirthday'] !== $today) {
            $results[$couponId] = "Coupon valid only on birthday";
            continue;
        }
    }

    // --- Mark coupon redemptions as used (loop qty times) ---
    $appliedCount = 0;
    for ($i = 0; $i < $qty; $i++) {
        error_log("Attempting redemption update for couponId=$couponId cid=$cid (attempt " . ($i+1) . ")");
        $stmt = $conn->prepare("
            UPDATE coupon_redemptions
            SET is_used = 1, used_at = NOW()
            WHERE cid = ? AND coupon_id = ? AND is_used = 0
            LIMIT 1
        ");
        $stmt->bind_param("ii", $cid, $couponId);
        $stmt->execute();
        if ($stmt->affected_rows > 0) {
            $appliedCount++;
            error_log("✅ Redemption applied for couponId=$couponId (count=$appliedCount)");
        } else {
            error_log("❌ No redemption row found for couponId=$couponId");
        }
        $stmt->close();
    }

    if ($appliedCount > 0) {
        $results[$couponId] = "Applied successfully (qty=$appliedCount)";
    } else {
        $results[$couponId] = "Not available or already used";
    }
}

error_log("=== Final results: " . json_encode($results) . " ===");

// Always return clean JSON
echo json_encode([
    "success" => true,
    "results" => $results
], JSON_UNESCAPED_UNICODE);

$conn->close();
?>
