<?php
header('Content-Type: application/json');

// Connect to MySQL
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    error_log("DB Connection failed: " . $conn->connect_error);
    echo json_encode(["error" => "Connection failed"]);
    exit;
}

// Query all table orders
$sql = "SELECT toid, table_number, oid, staff_id, status FROM table_orders ORDER BY table_number ASC";
$result = $conn->query($sql);

$tableOrders = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $tableOrders[] = [
            "toid" => (int)$row["toid"],
            "tableNumber" => (int)$row["table_number"],
            "oid" => isset($row["oid"]) ? (int)$row["oid"] : null,
            "staffId" => isset($row["staff_id"]) ? (int)$row["staff_id"] : null,
            "status" => $row["status"]
        ];
    }
}

$conn->close();
echo json_encode($tableOrders);