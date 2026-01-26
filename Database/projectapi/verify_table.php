<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=utf-8");

// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error, "valid" => false]);
    exit;
}

// Get table_id from POST or GET
$table_id = $_POST['table_id'] ?? $_GET['table_id'] ?? null;

if (!$table_id) {
    echo json_encode(["success" => false, "message" => "table_id parameter is required", "valid" => false]);
    $conn->close();
    exit;
}

// Validate that table_id is numeric
if (!is_numeric($table_id)) {
    echo json_encode(["success" => false, "message" => "Invalid table_id format", "valid" => false]);
    $conn->close();
    exit;
}

$table_id = (int)$table_id;

// Query to verify table exists and check its status
$sql = "
    SELECT 
        sc.tid,
        sc.capacity,
        sc.status,
        CASE 
            WHEN to.toid IS NOT NULL THEN 'occupied'
            WHEN sc.status = 1 THEN 'reserved'
            ELSE 'available'
        END AS current_status,
        COALESCE(to.oid, 0) AS active_order
    FROM seatingChart sc
    LEFT JOIN table_orders to ON sc.tid = to.table_number AND to.status NOT IN ('paid', 'cancelled')
    WHERE sc.tid = ?
    LIMIT 1
";

$stmt = $conn->prepare($sql);
if ($stmt === false) {
    echo json_encode(["success" => false, "message" => $conn->error, "valid" => false]);
    $conn->close();
    exit;
}

$stmt->bind_param("i", $table_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "success" => true,
        "valid" => false,
        "message" => "Table not found",
        "table_id" => $table_id
    ]);
    $stmt->close();
    $conn->close();
    exit;
}

$row = $result->fetch_assoc();

// Check if table is available for ordering
$is_available = ($row["current_status"] === "available");

echo json_encode([
    "success" => true,
    "valid" => true,
    "table_id" => (int)$row["tid"],
    "capacity" => (int)$row["capacity"],
    "status" => $row["current_status"],
    "available" => $is_available,
    "message" => $is_available ? "Table is available" : "Table is not available (" . $row["current_status"] . ")"
]);

$stmt->close();
$conn->close();
?>
