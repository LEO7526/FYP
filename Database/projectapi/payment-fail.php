<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: text/plain");

// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error]);
    exit;
}

$orderRef = $_GET['orderRef'] ?? '';

if (!$orderRef) {
    echo "Missing orderRef.";
    exit;
}

// Update order status to '2' (failed)
$stmt = $conn->prepare("UPDATE orders SET ostatus = 2 WHERE orderRef = ?");
$stmt->bind_param("s", $orderRef);

if ($stmt->execute()) {
    echo "❌ Payment failed. Order marked as failed.";
} else {
    echo "⚠️ Failed to update order status.";
}