<?php
header('Content-Type: application/json');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit;
}

$cid = isset($_POST['cid']) ? intval($_POST['cid']) : 0;
$couponId = isset($_POST['coupon_id']) ? intval($_POST['coupon_id']) : 0;

if ($cid === 0 || $couponId === 0) {
    echo json_encode(["success" => false, "error" => "Invalid parameters"]);
    exit;
}

$stmt = $conn->prepare("
    UPDATE coupon_redemptions
    SET is_used = 1, used_at = NOW()
    WHERE cid = ? AND coupon_id = ? AND is_used = 0
");
$stmt->bind_param("ii", $cid, $couponId);
$stmt->execute();

if ($stmt->affected_rows > 0) {
    echo json_encode(["success" => true, "message" => "Coupon applied successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Coupon not available or already used"]);
}

$stmt->close();
$conn->close();