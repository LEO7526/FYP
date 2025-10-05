<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: text/plain");

// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error]);
    exit;
}

// Get the orderRef from the redirect URL
$orderRef = $_GET['orderRef'] ?? '';

if (!$orderRef) {
    echo "Missing orderRef.";
    exit;
}

// Update order status to '1' (completed)
$stmt = $conn->prepare("UPDATE orders SET ostatus = 1 WHERE orderRef = ?");
$stmt->bind_param("s", $orderRef);

if ($stmt->execute()) {
    echo "✅ Payment successful. Order status updated.";
} else {
    echo "❌ Failed to update order status.";
}