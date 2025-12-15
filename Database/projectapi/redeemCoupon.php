<?php
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode([
        "success"    => false,
        "error"      => "DB connection failed",
        "error_code" => "DB_CONNECTION_FAILED"
    ]);
    exit;
}

$cid       = isset($_POST['cid']) ? intval($_POST['cid']) : 0;
$coupon_id = isset($_POST['coupon_id']) ? intval($_POST['coupon_id']) : 0;
$quantity  = isset($_POST['quantity']) ? intval($_POST['quantity']) : 1;

if ($cid <= 0 || $coupon_id <= 0) {
    echo json_encode([
        "success"    => false,
        "error"      => "Missing or invalid cid/coupon_id",
        "error_code" => "INVALID_INPUT"
    ]);
    exit;
}

$conn->begin_transaction();

try {
    // ✅ Birthday coupon check (coupon_id = 4)
    if ($coupon_id == 4) {
        // Get customer's birthday (MM-DD)
        $stmt = $conn->prepare("SELECT cbirthday FROM customer WHERE cid=?");
        $stmt->bind_param("i", $cid);
        $stmt->execute();
        $brow = $stmt->get_result()->fetch_assoc();
        $stmt->close();

        if (!$brow || empty($brow['cbirthday'])) {
            echo json_encode([
                "success"    => false,
                "error"      => "Birthday not set",
                "error_code" => "NO_BIRTHDAY_SET"
            ]);
            $conn->rollback();
            $conn->close();
            exit;
        }

        // Parse MM-DD
        list($month, $day) = explode("-", $brow['cbirthday']);
        $currentMonth = date("m");

        if ($month != $currentMonth) {
            echo json_encode([
                "success"    => false,
                "error"      => "Birthday coupon can only be redeemed during your birthday month",
                "error_code" => "NOT_BIRTHDAY_MONTH"
            ]);
            $conn->rollback();
            $conn->close();
            exit;
        }

        // Check if already redeemed this year
        $year = date("Y");
        $stmt = $conn->prepare("SELECT COUNT(*) AS cnt 
                                FROM coupon_redemptions 
                                WHERE cid=? AND coupon_id=? AND YEAR(redeemed_at)=?");
        $stmt->bind_param("iii", $cid, $coupon_id, $year);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();

        if ($result['cnt'] > 0) {
            echo json_encode([
                "success"    => false,
                "error"      => "Birthday coupon already redeemed this year",
                "error_code" => "BIRTHDAY_ALREADY_REDEEMED"
            ]);
            $conn->rollback();
            $conn->close();
            exit;
        }
    }

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
    
    // Initialize customer with 0 points if not found
    if (!$row) {
        $stmt = $conn->prepare("INSERT INTO coupon_point (cid, points) VALUES (?, 0)");
        $stmt->bind_param("i", $cid);
        $stmt->execute();
        $cp_id = $stmt->insert_id;
        $stmt->close();
        $current_points = 0;
    } else {
        $cp_id = $row['cp_id'];
        $current_points = intval($row['points']);
    }
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
    $stmt = $conn->prepare("INSERT INTO coupon_redemptions (coupon_id, cid, redeemed_at, is_used, used_at) 
                            VALUES (?, ?, NOW(), 0, NULL)");
    for ($i = 0; $i < $quantity; $i++) {
        $stmt->bind_param("ii", $coupon_id, $cid);
        $stmt->execute();
    }
    $stmt->close();

    // Insert history
    $delta = -$total_cost;
    $action = "redeem";
    $note = "Coupon ID $coupon_id x$quantity";
    $stmt = $conn->prepare("INSERT INTO coupon_point_history 
        (cp_id, cid, coupon_id, delta, resulting_points, action, note) 
        VALUES (?, ?, ?, ?, ?, ?, ?)");
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
    echo json_encode([
        "success"    => false,
        "error"      => $e->getMessage(),
        "error_code" => "GENERIC_ERROR"
    ]);
}

$conn->close();
?>