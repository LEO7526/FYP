<?php
header('Content-Type: application/json');

$conn = new mysqli('localhost', 'root', '', 'ProjectDB');
if ($conn->connect_error) {
    echo json_encode(['error' => $conn->connect_error]);
    exit;
}

// Before balance
$result = $conn->query('SELECT coupon_point FROM customer WHERE cid = 1');
$rowBefore = $result->fetch_assoc();
$balanceBefore = $rowBefore['coupon_point'];

// Simulate an order (adding 200 points)
$pointsToAdd = 200;
$stmt = $conn->prepare("UPDATE customer SET coupon_point = coupon_point + ? WHERE cid = ?");
$stmt->bind_param("ii", $pointsToAdd, 1);
$stmt->execute();

// Get new balance
$result = $conn->query('SELECT coupon_point FROM customer WHERE cid = 1');
$rowAfter = $result->fetch_assoc();
$balanceAfter = $rowAfter['coupon_point'];

// Insert history
$note = "Order completed - Test order #123";
$historyStmt = $conn->prepare("INSERT INTO coupon_point_history (cid, coupon_id, delta, resulting_points, action, note) VALUES (?, NULL, ?, ?, 'earn', ?)");
$cid = 1;
$action = 'earn';
$historyStmt->bind_param("iisss", $cid, $pointsToAdd, $balanceAfter, $action, $note);
$historyStmt->execute();

echo json_encode([
    'success' => true,
    'before' => $balanceBefore,
    'added' => $pointsToAdd,
    'after' => $balanceAfter
]);

$conn->close();
?>
