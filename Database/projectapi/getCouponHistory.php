<?php
header('Content-Type: application/json');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "DB connection failed"]);
    exit;
}

$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
if ($cid <= 0) {
    echo json_encode(["success" => false, "error" => "Missing or invalid cid"]);
    exit;
}

$sql = "SELECT delta, resulting_points, action, note, created_at
        FROM coupon_point_history
        WHERE cid = ?
        ORDER BY created_at DESC";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result();

$history = [];
while ($row = $result->fetch_assoc()) {
    $history[] = $row;
}

echo json_encode([
    "success" => true,
    "history" => $history
]);

$stmt->close();
$conn->close();