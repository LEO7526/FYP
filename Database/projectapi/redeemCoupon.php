<?php
header('Content-Type: application/json');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success"=>false,"error"=>"DB connection failed"]);
    exit;
}

$inputRaw = file_get_contents("php://input");
$input = json_decode($inputRaw, true);

$cid = isset($input['cid']) ? intval($input['cid']) : 0;
$coupon_id = isset($input['coupon_id']) ? intval($input['coupon_id']) : 0;

if ($cid <= 0 || $coupon_id <= 0) {
    http_response_code(400);
    echo json_encode(["success"=>false,"error"=>"Missing cid or coupon_id"]);
    exit;
}

$conn->begin_transaction();

try {
    // Get coupon info
    $stmt = $conn->prepare("SELECT points_required, expiry_date, is_active FROM coupons WHERE coupon_id=? LIMIT 1");
    $stmt->bind_param("i", $coupon_id);
    $stmt->execute();
    $stmt->bind_result($points_required, $expiry_date, $is_active);
    if (!$stmt->fetch()) {
        throw new Exception("Coupon not found");
    }
    $stmt->close();

    if ($is_active != 1) {
        throw new Exception("Coupon inactive");
    }
    if ($expiry_date && strtotime($expiry_date) < time()) {
        throw new Exception("Coupon expired");
    }

    // Get customer points
    $stmt = $conn->prepare("SELECT cp_id, points FROM coupon_point WHERE cid=? LIMIT 1");
    $stmt->bind_param("i", $cid);
    $stmt->execute();
    $stmt->bind_result($cp_id, $current_points);
    if (!$stmt->fetch()) {
        throw new Exception("No coupon_point record for this customer");
    }
    $stmt->close();

    if ($current_points < $points_required) {
        throw new Exception("Not enough points");
    }

    // Deduct points
    $newPoints = $current_points - $points_required;
    $stmt = $conn->prepare("UPDATE coupon_point SET points=?, updated_at=CURRENT_TIMESTAMP WHERE cp_id=?");
    $stmt->bind_param("ii", $newPoints, $cp_id);
    if (!$stmt->execute()) {
        throw new Exception("Failed to update points: ".$stmt->error);
    }
    $stmt->close();

    // Record redemption
    $stmt = $conn->prepare("INSERT INTO coupon_redemptions (coupon_id, cid) VALUES (?, ?)");
    $stmt->bind_param("ii", $coupon_id, $cid);
    if (!$stmt->execute()) {
        throw new Exception("Failed to insert redemption: ".$stmt->error);
    }
    $stmt->close();

    $conn->commit();
    echo json_encode(["success"=>true,"message"=>"Coupon redeemed","remaining_points"=>$newPoints]);

} catch (Exception $e) {
    $conn->rollback();
    http_response_code(400);
    echo json_encode(["success"=>false,"error"=>$e->getMessage()]);
}

$conn->close();