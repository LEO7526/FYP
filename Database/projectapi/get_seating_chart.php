<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=utf-8");

// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error]);
    exit;
}

// Query to get all tables with their status from seatingChart and table_orders
$sql = "
    SELECT 
        sc.tid AS table_id,
        sc.capacity,
        COALESCE(sc.status, 0) AS status,
        CASE 
            WHEN to.toid IS NOT NULL THEN 'occupied'
            WHEN sc.status = 1 THEN 'reserved'
            ELSE 'available'
        END AS current_status
    FROM seatingChart sc
    LEFT JOIN table_orders to ON sc.tid = to.table_number AND to.status NOT IN ('paid', 'cancelled')
    ORDER BY sc.tid ASC
";

$result = $conn->query($sql);

if ($result === false) {
    echo json_encode(["success" => false, "message" => $conn->error]);
    $conn->close();
    exit;
}

$tables = [];
while ($row = $result->fetch_assoc()) {
    $tables[] = [
        "id" => (int)$row["table_id"],
        "capacity" => (int)$row["capacity"],
        "status" => $row["current_status"],
        "reserved" => (int)$row["status"]
    ];
}

$conn->close();

echo json_encode([
    "success" => true,
    "data" => $tables,
    "total_tables" => count($tables)
]);
?>
