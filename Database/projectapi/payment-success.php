<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database connection failed: " . $conn->connect_error]);
    exit;
}

// Get the orderRef from the redirect URL
$orderRef = $_GET['orderRef'] ?? '';

if (!$orderRef) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Missing orderRef parameter"]);
    exit;
}

// Get order details (oid, cid)
$orderStmt = $conn->prepare("SELECT oid, cid FROM orders WHERE orderRef = ?");
if (!$orderStmt) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Failed to prepare order query: " . $conn->error]);
    exit;
}

$orderStmt->bind_param("s", $orderRef);
$orderStmt->execute();
$orderResult = $orderStmt->get_result();

if ($orderResult->num_rows === 0) {
    http_response_code(404);
    echo json_encode(["success" => false, "message" => "Order not found"]);
    exit;
}

$orderRow = $orderResult->fetch_assoc();
$oid = $orderRow['oid'];
$cid = $orderRow['cid'];

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
$totalAmount = (int)$amountRow['total'];

// Calculate coupon points (1 point per dollar, or customize the ratio as needed)
$couponPointsToAdd = $totalAmount; // Can change to $totalAmount / 10 for different ratio

// Start transaction
$conn->begin_transaction();

try {
    // Update order status to '1' (completed)
    $updateStmt = $conn->prepare("UPDATE orders SET ostatus = 1 WHERE orderRef = ?");
    if (!$updateStmt) {
        throw new Exception("Failed to prepare update statement: " . $conn->error);
    }
    
    $updateStmt->bind_param("s", $orderRef);
    if (!$updateStmt->execute()) {
        throw new Exception("Failed to update order status: " . $updateStmt->error);
    }
    
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
    
    // Insert into coupon_point_history
    $historyStmt = $conn->prepare("INSERT INTO coupon_point_history (cid, coupon_id, delta, resulting_points, action, note) VALUES (?, NULL, ?, ?, 'earn', ?)");
    if (!$historyStmt) {
        throw new Exception("Failed to prepare history insert: " . $conn->error);
    }
    $note = "Payment for order $oid";
    $newBalance = intval($couponPointsToAdd) + intval($totalAmount);
    $historyStmt->bind_param("iiss", $cid, $couponPointsToAdd, $newBalance, $note);
    if (!$historyStmt->execute()) {
        throw new Exception("Failed to insert history: " . $historyStmt->error);
    }
    $historyStmt->close();
    
    // Commit transaction
    $conn->commit();
    
    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "Payment successful. Order status updated and coupon points added.",
        "oid" => $oid,
        "cid" => $cid,
        "totalAmount" => $totalAmount,
        "couponPointsAdded" => $couponPointsToAdd
    ]);
    
} catch (Exception $e) {
    // Rollback on error
    $conn->rollback();
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Payment processing failed: " . $e->getMessage()]);
}

$amountStmt->close();
$orderStmt->close();
$conn->close();
?>