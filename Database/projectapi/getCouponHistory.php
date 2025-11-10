<?php
header('Content-Type: application/json; charset=utf-8');

// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "DB connection failed"]);
    exit;
}

// Validate input
$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
if ($cid <= 0) {
    echo json_encode(["success" => false, "error" => "Missing or invalid cid"]);
    exit;
}

// Build query: join history with coupon_translation for readable title
$sql = "SELECT h.delta,
               h.resulting_points,
               h.action,
               h.note,
               h.created_at,
               ct.title AS coupon_title
        FROM coupon_point_history h
        LEFT JOIN coupon_translation ct 
               ON h.coupon_id = ct.coupon_id AND ct.language_code = 'en'
        WHERE h.cid = ?
        ORDER BY h.created_at DESC";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result();

$history = [];
while ($row = $result->fetch_assoc()) {
    $history[] = [
        "delta"            => intval($row['delta']),
        "resulting_points" => intval($row['resulting_points']),
        "action"           => $row['action'],
        "note"             => $row['note'],
        "created_at"       => $row['created_at'],
        "coupon_title"     => $row['coupon_title']
    ];
}

echo json_encode([
    "success" => true,
    "history" => $history
], JSON_UNESCAPED_UNICODE);

$stmt->close();
$conn->close();
?>
