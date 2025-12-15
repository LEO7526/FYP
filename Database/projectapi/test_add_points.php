<?php
header('Content-Type: application/json');

$conn = new mysqli('localhost', 'root', '', 'ProjectDB');
if ($conn->connect_error) {
    echo json_encode(['error' => $conn->connect_error]);
    exit;
}

$cid = 1;
$pointsToAdd = 100;

// Update points
$stmt = $conn->prepare("UPDATE customer SET coupon_point = coupon_point + ? WHERE cid = ?");
$stmt->bind_param("ii", $pointsToAdd, $cid);
$stmt->execute();

// Get new balance
$balanceStmt = $conn->prepare("SELECT coupon_point FROM customer WHERE cid = ?");
$balanceStmt->bind_param("i", $cid);
$balanceStmt->execute();
$balanceRow = $balanceStmt->get_result()->fetch_assoc();
$newBalance = $balanceRow['coupon_point'];

// Insert history
$historyStmt = $conn->prepare("INSERT INTO coupon_point_history (cid, coupon_id, delta, resulting_points, action, note) VALUES (?, NULL, ?, ?, 'earn', ?)");
$note = "Test: Add 100 points";
$historyStmt->bind_param("iiss", $cid, $pointsToAdd, $newBalance, $note);
$historyStmt->execute();

echo json_encode([
    'success' => true,
    'points_added' => $pointsToAdd,
    'new_balance' => $newBalance
]);

$conn->close();
?>
