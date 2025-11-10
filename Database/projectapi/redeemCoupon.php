<?php
header('Content-Type: application/json; charset=utf-8');
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success"=>false,"error"=>"DB connection failed"]);
    exit;
}

$cid       = isset($_POST['cid']) ? intval($_POST['cid']) : 0;
$coupon_id = isset($_POST['coupon_id']) ? intval($_POST['coupon_id']) : 0;
$quantity  = isset($_POST['quantity']) ? intval($_POST['quantity']) : 1;

if ($cid <= 0 || $coupon_id <= 0) {
    echo json_encode(["success"=>false,"error"=>"Missing or invalid cid/coupon_id"]);
    exit;
}

$conn->begin_transaction();

try {
    // Get coupon details
    $stmt = $conn->prepare("SELECT points_required FROM coupons WHERE coupon_id=? AND is_active=1");
    $stmt->bind_param("i", $coupon_id);
    $stmt->execute();
    $coupon = $stmt->get_result()->fetch_assoc();
    $stmt->close();
    if (!$coupon) throw new Exception("Coupon not found or inactive");

    $points_required = intval($coupon['points_required']);
    $total_cost = $points_required * $quantity;

    // Get customer points
    $stmt = $conn->prepare("SELECT cp_id, points FROM coupon_point WHERE cid=?");
    $stmt->bind_param("i", $cid);
    $stmt->execute();
    $row = $stmt->get_result()->fetch_assoc();
    $stmt->close();
    if (!$row) throw new Exception("Customer not found");

    $cp_id = $row['cp_id'];
    $current_points = intval($row['points']);
    if ($total_cost > 0 && $current_points < $total_cost) {
        throw new Exception("Not enough points");
    }
    $new_points = $current_points - $total_cost;

    // Update balance
    if ($total_cost > 0) {
        $stmt = $conn->prepare("UPDATE coupon_point SET points=? WHERE cp_id=?");
        $stmt->bind_param("ii", $new_points, $cp_id);
        $stmt->execute();
        $stmt->close();
    }

    // Insert redemption(s)
    $stmt = $conn->prepare("INSERT INTO coupon_redemptions (coupon_id, cid, is_used, used_at) VALUES (?, ?, 0, NULL)");
    for ($i = 0; $i < $quantity; $i++) {
        $stmt->bind_param("ii", $coupon_id, $cid);
        $stmt->execute();
    }
    $stmt->close();

    // Insert history
    $delta = -$total_cost;
    $action = "redeem";
    $note = "Coupon ID $coupon_id x$quantity";
    $stmt = $conn->prepare("INSERT INTO coupon_point_history (cp_id, cid, coupon_id, delta, resulting_points, action, note) VALUES (?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("iiiisss", $cp_id, $cid, $coupon_id, $delta, $new_points, $action, $note);
    $stmt->execute();
    $stmt->close();

    $conn->commit();
    echo json_encode([
        "success"       => true,
        "message"       => "Redeemed $quantity coupon(s) successfully",
        "points_before" => $current_points,
        "points_after"  => $new_points
    ]);
} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["success"=>false,"error"=>$e->getMessage()]);
}
$conn->close();
?>
