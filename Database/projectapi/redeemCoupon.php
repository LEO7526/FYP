<?php
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode([
        "success"    => false,
        "error"      => "Database connection failed",
        "error_code" => "DB_ERROR"
    ]);
    exit;
}

$cid       = isset($_POST['cid']) ? intval($_POST['cid']) : 0;
$coupon_id = isset($_POST['coupon_id']) ? intval($_POST['coupon_id']) : 0;
$quantity  = isset($_POST['quantity']) ? intval($_POST['quantity']) : 1;

if ($cid <= 0 || $coupon_id <= 0) {
    http_response_code(400);
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
        if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
        $stmt->bind_param("i", $cid);
        $stmt->execute();
        $brow = $stmt->get_result()->fetch_assoc();
        $stmt->close();

        if (!$brow || empty($brow['cbirthday'])) {
            $conn->rollback();
            http_response_code(400);
            echo json_encode([
                "success"    => false,
                "error"      => "Birthday not set",
                "error_code" => "NO_BIRTHDAY_SET"
            ]);
            exit;
        }

        // Parse MM-DD
        list($month, $day) = explode("-", $brow['cbirthday']);
        $currentMonth = date("m");

        if ($month != $currentMonth) {
            $conn->rollback();
            http_response_code(400);
            echo json_encode([
                "success"    => false,
                "error"      => "Birthday coupon can only be redeemed during your birthday month",
                "error_code" => "NOT_BIRTHDAY_MONTH"
            ]);
            exit;
        }

        // Check if already redeemed this year
        $year = date("Y");
        $stmt = $conn->prepare("SELECT COUNT(*) AS cnt 
                                FROM coupon_redemptions 
                                WHERE cid=? AND coupon_id=? AND YEAR(redeemed_at)=?");
        if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
        $stmt->bind_param("iii", $cid, $coupon_id, $year);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();

        if ($result['cnt'] > 0) {
            $conn->rollback();
            http_response_code(400);
            echo json_encode([
                "success"    => false,
                "error"      => "Birthday coupon already redeemed this year",
                "error_code" => "BIRTHDAY_ALREADY_REDEEMED"
            ]);
            exit;
        }
    }

    // Get coupon details
    $stmt = $conn->prepare("SELECT points_required FROM coupons WHERE coupon_id=? AND is_active=1");
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("i", $coupon_id);
    $stmt->execute();
    $coupon = $stmt->get_result()->fetch_assoc();
    $stmt->close();
    
    if (!$coupon) {
        throw new Exception("Coupon not found or inactive");
    }

    $points_required = intval($coupon['points_required']);
    $total_cost = $points_required * $quantity;

    // Get customer's current coupon points from customer table
    $stmt = $conn->prepare("SELECT coupon_point FROM customer WHERE cid=?");
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("i", $cid);
    $stmt->execute();
    $custRow = $stmt->get_result()->fetch_assoc();
    $stmt->close();
    
    if (!$custRow) {
        throw new Exception("Customer not found");
    }

    $current_points = intval($custRow['coupon_point']);
    
    // Check if enough points
    if ($total_cost > 0 && $current_points < $total_cost) {
        $conn->rollback();
        http_response_code(400);
        echo json_encode([
            "success"    => false,
            "error"      => "Not enough points",
            "error_code" => "INSUFFICIENT_POINTS",
            "points_before" => $current_points,
            "points_required" => $total_cost
        ]);
        exit;
    }

    $new_points = $current_points - $total_cost;

    // Update customer points
    $stmt = $conn->prepare("UPDATE customer SET coupon_point=? WHERE cid=?");
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("ii", $new_points, $cid);
    if (!$stmt->execute()) throw new Exception("Execute failed: " . $stmt->error);
    $stmt->close();

    // Insert into coupon_point_history
    $historyStmt = $conn->prepare("INSERT INTO coupon_point_history (cid, coupon_id, delta, resulting_points, action, note) VALUES (?, ?, ?, ?, 'redeem', ?)");
    if (!$historyStmt) throw new Exception("Prepare failed: " . $conn->error);
    $note = "Redeemed $quantity coupon(s)";
    $negativeDelta = -$total_cost;
    $historyStmt->bind_param("iiiss", $cid, $coupon_id, $negativeDelta, $new_points, $note);
    if (!$historyStmt->execute()) throw new Exception("Execute failed: " . $historyStmt->error);
    $historyStmt->close();

    // Insert redemption(s)
    $stmt = $conn->prepare("INSERT INTO coupon_redemptions (coupon_id, cid, redeemed_at, is_used, used_at) 
                            VALUES (?, ?, NOW(), 0, NULL)");
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    
    for ($i = 0; $i < $quantity; $i++) {
        $stmt->bind_param("ii", $coupon_id, $cid);
        if (!$stmt->execute()) throw new Exception("Execute failed: " . $stmt->error);
    }
    $stmt->close();

    // Commit transaction
    $conn->commit();
    
    http_response_code(200);
    echo json_encode([
        "success"       => true,
        "message"       => "Redeemed $quantity coupon(s) successfully",
        "points_before" => $current_points,
        "points_after"  => $new_points
    ]);
    
} catch (Exception $e) {
    $conn->rollback();
    http_response_code(500);
    echo json_encode([
        "success"    => false,
        "error"      => $e->getMessage(),
        "error_code" => "GENERIC_ERROR"
    ]);
}

$conn->close();
?>
    $stmt->execute();
    $row = $stmt->get_result()->fetch_assoc();
    $stmt->close();
    
    // Initialize customer with 0 points if not found
    // Using INSERT IGNORE to handle concurrent requests gracefully
    if (!$row) {
        $stmt = $conn->prepare("INSERT IGNORE INTO coupon_point (cid, points) VALUES (?, 0)");
        $stmt->bind_param("i", $cid);
        $stmt->execute();
        $stmt->close();
        
        // Re-fetch to get cp_id (in case INSERT IGNORE skipped due to race condition)
        $stmt = $conn->prepare("SELECT cp_id, points FROM coupon_point WHERE cid=?");
        $stmt->bind_param("i", $cid);
        $stmt->execute();
        $row = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        
        if (!$row) {
            throw new Exception("Failed to initialize customer points");
        }
        
        $cp_id = $row['cp_id'];
        $current_points = intval($row['points']);
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