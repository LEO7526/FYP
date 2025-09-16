<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error]);
    exit;
}

// Get language from query parameter
$lang = $_GET['lang'] ?? 'en';
$validLangs = ['en', 'zh-CN', 'zh-TW'];
if (!in_array($lang, $validLangs)) {
    echo json_encode(["success" => false, "message" => "Invalid language code"]);
    exit;
}

// Prepare SQL query
$sql = "
    SELECT 
        mi.item_id AS id,
        mit.item_name AS name,
        mit.item_description AS description,
        mi.item_price AS price,
        mi.image_url,
        mi.spice_level,
        mi.tags,
        mc.category_name AS category
    FROM menu_item mi
    JOIN menu_item_translation mit ON mi.item_id = mit.item_id
    JOIN menu_category mc ON mi.category_id = mc.category_id
    WHERE mi.is_available = 1 AND mit.language_code = ?
    ORDER BY mc.category_name, mit.item_name
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $lang);
$stmt->execute();
$result = $stmt->get_result();

$items = [];
while ($row = $result->fetch_assoc()) {
    $items[] = $row;
}

echo json_encode([
    "success" => true,
    "language" => $lang,
    "data" => $items
]);

$stmt->close();
$conn->close();
?>