<?php
header("Content-Type: application/json; charset=UTF-8");

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error]);
    exit;
}

// Fetch all packages
$sql = "SELECT package_id, package_name, num_of_type, package_image_url, amounts FROM menu_package";
$result = $conn->query($sql);

$packages = [];
if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $packages[] = [
            "id" => (int)$row['package_id'],
            "name" => $row['package_name'],
            "num_of_type" => (int)$row['num_of_type'],
            "image_url" => $row['package_image_url'],
            "price" => (float)$row['amounts']
        ];
    }
}

$conn->close();

// Return consistent JSON structure
echo json_encode([
    "success" => true,
    "data" => $packages
], JSON_UNESCAPED_UNICODE);