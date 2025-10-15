<?php
// Always start with no whitespace before <?php
header('Content-Type: application/json; charset=utf-8');

// Database connection
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "DB connection failed"]);
    exit;
}

// Validate inputs
$cid = isset($_POST['cid']) ? intval($_POST['cid']) : 0;
$coupon_id = isset($_POST['coupon_id']) ? intval($_POST['coupon_id']) : 0;

if ($cid <= 0 || $coupon_id <= 0) {
    echo json_encode(["success" => false, "error" => "Missing or invalid cid/coupon_id"]);
    exit;
}

$conn->begin_transaction();

try {
    // 1. Get coupon details
    $stmt = $conn->prepare("SELECT points_required, title FROM coupons WHERE coupon_id=? AND is_active=1");
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("i", $coupon_id);
    $stmt->execute();
    $coupon = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$coupon) throw new Exception("Coupon not found or inactive");

    $points_required = intval($coupon['points_required']);
    $coupon_title = $coupon['title'];

    // 2. Get customer points
    $stmt = $conn->prepare("SELECT cp_id, points FROM coupon_point WHERE cid=?");
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("i", $cid);
    $stmt->execute();
    $row = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$row) throw new Exception("Customer not found");

    $cp_id = $row['cp_id'];
    $current_points = intval($row['points']);

    if ($points_required > 0 && $current_points < $points_required) {
        throw new Exception("Not enough points");
    }

    $new_points = $current_points - $points_required;

    // 3. Update balance only if points_required > 0
    if ($points_required > 0) {
        $stmt = $conn->prepare("UPDATE coupon_point SET points=? WHERE cp_id=?");
        if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
        $stmt->bind_param("ii", $new_points, $cp_id);
        if (!$stmt->execute()) throw new Exception("Failed to update points: " . $stmt->error);
        $stmt->close();
    }

    // 4. Insert redemption
    $stmt = $conn->prepare("INSERT INTO coupon_redemptions (coupon_id, cid) VALUES (?, ?)");
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("ii", $coupon_id, $cid);
    if (!$stmt->execute()) throw new Exception("Failed to insert redemption: " . $stmt->error);
    $stmt->close();

    // 5. Insert history
    $delta = -$points_required; // will be 0 for free coupons
    $action = "Redeemed";
    $note = "Coupon ID " . $coupon_id;

    $stmt = $conn->prepare("INSERT INTO coupon_point_history 
        (cp_id, cid, coupon_id, delta, resulting_points, action, note) 
        VALUES (?, ?, ?, ?, ?, ?, ?)");
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("iiiisss", $cp_id, $cid, $coupon_id, $delta, $new_points, $action, $note);
    if (!$stmt->execute()) throw new Exception("Failed to insert history: " . $stmt->error);
    $stmt->close();

    // Commit transaction
    $conn->commit();

    echo json_encode([
        "success" => true,
        "message" => "Coupon redeemed successfully",
        "coupon_title" => $coupon_title,
        "points_before" => $current_points,
        "points_after" => $new_points
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["success" => false, "error" => $e->getMessage()]);
}

$conn->close();