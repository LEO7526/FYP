<?php
/**
 * Generate QR Code data for a specific table
 * Returns JSON format suitable for encoding into QR code
 * Usage: GET /generate_qr_data.php?table_id=5
 */

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=utf-8");

// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error]);
    exit;
}

// Get table_id from query parameter
$table_id = $_GET['table_id'] ?? null;

if (!$table_id || !is_numeric($table_id)) {
    echo json_encode([
        "success" => false, 
        "message" => "table_id parameter is required and must be numeric"
    ]);
    $conn->close();
    exit;
}

$table_id = (int)$table_id;

// Verify table exists
$sql = "SELECT tid, capacity FROM seatingChart WHERE tid = ?";
$stmt = $conn->prepare($sql);
if ($stmt === false) {
    echo json_encode(["success" => false, "message" => $conn->error]);
    $conn->close();
    exit;
}

$stmt->bind_param("i", $table_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "success" => false,
        "message" => "Table not found"
    ]);
    $stmt->close();
    $conn->close();
    exit;
}

$row = $result->fetch_assoc();

// Generate QR data in JSON format
$qr_data = [
    "table_id" => (int)$row["tid"],
    "restaurant_id" => 1,
    "restaurant_name" => "Yummy Restaurant",
    "timestamp" => date("Y-m-d H:i:s")
];

echo json_encode([
    "success" => true,
    "qr_content" => json_encode($qr_data),
    "table_id" => (int)$row["tid"],
    "capacity" => (int)$row["capacity"],
    "message" => "QR code data generated successfully"
]);

$stmt->close();
$conn->close();
?>
