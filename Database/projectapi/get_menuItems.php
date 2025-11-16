<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=utf-8");

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
// - Join menu_item_translation for requested language
// - LEFT JOIN menu_tag and tag to aggregate tags
// - GROUP_CONCAT to collect tags as comma separated string
$sql = "
    SELECT
        mi.item_id AS id,
        mit.item_name AS name,
        mit.item_description AS description,
        mi.item_price AS price,
        mi.image_url,
        mi.spice_level,
        mi.category_id AS category_id, 
        mc.category_name AS category,
        GROUP_CONCAT(t.tag_name SEPARATOR ',') AS tags_concat
    FROM menu_item mi
    JOIN menu_item_translation mit ON mi.item_id = mit.item_id
    JOIN menu_category mc ON mi.category_id = mc.category_id
    LEFT JOIN menu_tag mt ON mi.item_id = mt.item_id
    LEFT JOIN tag t ON mt.tag_id = t.tag_id
    WHERE mi.is_available = 1 AND mit.language_code = ?
    GROUP BY mi.item_id, mit.item_name, mit.item_description, mi.item_price, mi.image_url, mi.spice_level, mc.category_name
    ORDER BY mc.category_name, mit.item_name
";

$stmt = $conn->prepare($sql);
if ($stmt === false) {
    echo json_encode(["success" => false, "message" => $conn->error]);
    $conn->close();
    exit;
}

$stmt->bind_param("s", $lang);
$stmt->execute();
$result = $stmt->get_result();

$items = [];
while ($row = $result->fetch_assoc()) {
    $tags = [];
    if (!empty($row['tags_concat'])) {
        // split and trim tags into array
        $raw = explode(',', $row['tags_concat']);
        foreach ($raw as $r) {
            $r = trim($r);
            if ($r !== '') $tags[] = $r;
        }
    }

    $items[] = [
    "id" => (int)$row['id'],
    "category_id" => (int)$row['category_id'],      // <-- add this line
    "name" => $row['name'],
    "description" => $row['description'],
    "price" => (float)$row['price'],
    "image_url" => $row['image_url'],
    "spice_level" => (int)$row['spice_level'],
    "category" => $row['category'],
    "tags" => $tags
    ];

}

echo json_encode([
    "success" => true,
    "language" => $lang,
    "data" => $items
], JSON_UNESCAPED_UNICODE);

$stmt->close();
$conn->close();
?>
