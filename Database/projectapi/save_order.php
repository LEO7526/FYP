<?php
header('Content-Type: application/json');

// Connect to MySQL
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["error" => "Connection failed"]);
    exit;
}

// Read JSON input
$input = json_decode(file_get_contents("php://input"), true);

// Extract and validate fields
$pid = $input['pid'] ?? null;
$oqty = $input['oqty'] ?? null;
$ocost = $input['ocost'] ?? null;
$cid = $input['cid'] ?? null;
$odeliverdate = $input['odeliverdate'] ?? null;
$ostatus = $input['ostatus'] ?? 0;
$odate = date('Y-m-d H:i:s'); // Current timestamp

if (!$pid || !$oqty || !$ocost || !$cid) {
    echo json_encode(["error" => "Missing required fields"]);
    exit;
}

// Prepare and execute insert
$stmt = $conn->prepare("
    INSERT INTO orders (odate, pid, oqty, ocost, cid, odeliverdate, ostatus)
    VALUES (?, ?, ?, ?, ?, ?, ?)
");

$stmt->bind_param("siidisi", $odate, $pid, $oqty, $ocost, $cid, $odeliverdate, $ostatus);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "order_id" => $stmt->insert_id]);
} else {
    echo json_encode(["error" => "Failed to save order"]);
}

$stmt->close();
$conn->close();