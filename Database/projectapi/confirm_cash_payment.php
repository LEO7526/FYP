<?php
header('Content-Type: application/json');

// Connect to MySQL
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database connection failed: " . $conn->connect_error]);
    exit;
}

// Read JSON input
$input = json_decode(file_get_contents("php://input"), true);

$oid = intval($input['oid'] ?? 0);
$sid = intval($input['sid'] ?? 0); // Staff ID confirming the payment

if ($oid <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Missing or invalid order ID"]);
    exit;
}

// Get order details
$orderStmt = $conn->prepare("SELECT cid, ostatus FROM orders WHERE oid = ?");
if (!$orderStmt) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Failed to prepare order query: " . $conn->error]);
    exit;
}

$orderStmt->bind_param("i", $oid);
$orderStmt->execute();
$orderResult = $orderStmt->get_result();

if ($orderResult->num_rows === 0) {
    http_response_code(404);
    echo json_encode(["success" => false, "message" => "Order not found"]);
    exit;
}

$orderRow = $orderResult->fetch_assoc();
$cid = intval($orderRow['cid']);
$ostatus = intval($orderRow['ostatus']);
$orderStmt->close();

// Only allow confirmation for unpaid cash orders (ostatus = 2)
if ($ostatus !== 2) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "This order cannot be confirmed. Current status: $ostatus (only status 2 'unpaid cash' can be confirmed)"
    ]);
    exit;
}

// Calculate total order amount
$amountStmt = $conn->prepare("
    SELECT COALESCE(SUM(mi.item_price * oi.qty), 0) as total
    FROM order_items oi
    JOIN menu_item mi ON oi.item_id = mi.item_id
    WHERE oi.oid = ?
");

if (!$amountStmt) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Failed to prepare amount query: " . $conn->error]);
    exit;
}

$amountStmt->bind_param("i", $oid);
$amountStmt->execute();
$amountResult = $amountStmt->get_result();
$amountRow = $amountResult->fetch_assoc();
$totalAmount = intval($amountRow['total']);
$amountStmt->close();

// Calculate coupon points: HK$1 = 1 point
$couponPointsToAdd = $totalAmount;

// Start transaction
$conn->begin_transaction();

try {
    // Update order status to '1' (completed) or to a 'paid cash' status
    $updateStmt = $conn->prepare("UPDATE orders SET ostatus = 1 WHERE oid = ?");
    if (!$updateStmt) {
        throw new Exception("Failed to prepare update statement: " . $conn->error);
    }
    
    $updateStmt->bind_param("i", $oid);
    if (!$updateStmt->execute()) {
        throw new Exception("Failed to update order status: " . $updateStmt->error);
    }
    $updateStmt->close();
    
    // Add coupon points to customer
    $pointsStmt = $conn->prepare("UPDATE customer SET coupon_point = coupon_point + ? WHERE cid = ?");
    if (!$pointsStmt) {
        throw new Exception("Failed to prepare points update: " . $conn->error);
    }
    
    $pointsStmt->bind_param("ii", $couponPointsToAdd, $cid);
    if (!$pointsStmt->execute()) {
        throw new Exception("Failed to add coupon points: " . $pointsStmt->error);
    }
    $pointsStmt->close();
    
    // Get the new coupon point balance
    $balanceStmt = $conn->prepare("SELECT coupon_point FROM customer WHERE cid = ?");
    if (!$balanceStmt) {
        throw new Exception("Failed to prepare balance query: " . $conn->error);
    }
    
    $balanceStmt->bind_param("i", $cid);
    $balanceStmt->execute();
    $balanceResult = $balanceStmt->get_result();
    $balanceRow = $balanceResult->fetch_assoc();
    $newBalance = $balanceRow['coupon_point'];
    $balanceStmt->close();
    
    // Insert into coupon_point_history for tracking
    $note = "Cash payment confirmed for order #" . $oid . " (Amount: HK$" . $totalAmount . ")";
    if ($sid > 0) {
        $note .= " by staff #" . $sid;
    }
    
    $historyStmt = $conn->prepare("INSERT INTO coupon_point_history (cid, coupon_id, delta, resulting_points, action, note) VALUES (?, NULL, ?, ?, 'earn', ?)");
    if (!$historyStmt) {
        throw new Exception("Failed to prepare history insert: " . $conn->error);
    }
    
    $historyStmt->bind_param("iiis", $cid, $couponPointsToAdd, $newBalance, $note);
    if (!$historyStmt->execute()) {
        throw new Exception("Failed to insert history: " . $historyStmt->error);
    }
    $historyStmt->close();
    
    // Commit transaction
    $conn->commit();
    
    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "Cash payment confirmed. Coupon points added.",
        "oid" => $oid,
        "cid" => $cid,
        "totalAmount" => $totalAmount,
        "couponPointsAdded" => $couponPointsToAdd,
        "newBalance" => $newBalance
    ]);
    error_log("✅ Cash payment confirmed for order $oid: cid=$cid, points=$couponPointsToAdd, new_balance=$newBalance");
    
} catch (Exception $e) {
    $conn->rollback();
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Failed to confirm payment: " . $e->getMessage()]);
    error_log("❌ Error confirming cash payment for order $oid: " . $e->getMessage());
}

$conn->close();
?>
