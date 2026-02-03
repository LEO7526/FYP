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

// Update order status to '3' (Cancelled)
$stmt = $conn->prepare("UPDATE orders SET ostatus = 3 WHERE orderRef = ?");
$stmt->bind_param("s", $orderRef);

if ($stmt->execute()) {
    echo "🚫 Payment canceled. Order marked as canceled.";
} else {
    echo "⚠️ Failed to update order status.";
}