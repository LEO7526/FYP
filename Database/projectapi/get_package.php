<?php
header("Content-Type: application/json; charset=UTF-8");

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error]);
    exit;
}

$packageId = isset($_GET['id']) ? intval($_GET['id']) : 0;
$lang = isset($_GET['lang']) ? $_GET['lang'] : 'en';

if ($packageId <= 0) {
    echo json_encode(["success" => false, "message" => "Invalid package id"]);
    exit;
}

// Fetch package info
$sql = "SELECT package_id, package_name, num_of_type, amounts, package_image_url 
        FROM menu_package WHERE package_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $packageId);
$stmt->execute();
$packageResult = $stmt->get_result();

if ($packageResult->num_rows === 0) {
    echo json_encode(["success" => false, "message" => "Package not found"]);
    exit;
}

$package = $packageResult->fetch_assoc();
$response = [
    "id" => (int)$package['package_id'],
    "name" => $package['package_name'],
    "num_of_type" => (int)$package['num_of_type'],
    "price" => (float)$package['amounts'],
    "image_url" => $package['package_image_url'],
    "types" => []
];

// Fetch package types with translations
$sql = "SELECT pt.type_id, pt.optional_quantity, ptt.type_name
        FROM package_type pt
        JOIN package_type_translation ptt ON pt.type_id = ptt.type_id
        WHERE pt.package_id = ? AND ptt.type_language_code = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("is", $packageId, $lang);
$stmt->execute();
$typeResult = $stmt->get_result();

while ($type = $typeResult->fetch_assoc()) {
    $typeId = (int)$type['type_id'];

    // Fetch allowed dishes for this type
    $dishSql = "SELECT mi.item_id AS id, mit.item_name AS name, mit.item_description AS description,
                       mi.item_price AS price, mi.image_url
                FROM package_dish pd
                JOIN menu_item mi ON pd.item_id = mi.item_id
                JOIN menu_item_translation mit ON mi.item_id = mit.item_id
                WHERE pd.type_id = ? AND mit.language_code = ?";
    $dishStmt = $conn->prepare($dishSql);
    $dishStmt->bind_param("is", $typeId, $lang);
    $dishStmt->execute();
    $dishResult = $dishStmt->get_result();

    $dishes = [];
    while ($dish = $dishResult->fetch_assoc()) {
        $dishes[] = [
            "id" => (int)$dish['id'],
            "name" => $dish['name'],
            "description" => $dish['description'],
            "price" => (float)$dish['price'],
            "image_url" => $dish['image_url']
        ];
    }
    $dishStmt->close();

    $response['types'][] = [
        "id" => $typeId,
        "name" => $type['type_name'],
        "optional_quantity" => (int)$type['optional_quantity'],
        "items" => $dishes
    ];
}

$stmt->close();
$conn->close();

echo json_encode(["success" => true, "data" => $response], JSON_UNESCAPED_UNICODE);