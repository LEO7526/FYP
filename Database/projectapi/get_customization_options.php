<?php
/**
 * 取得菜品的自訂選項（v5.9版本 - 支援多語言翻譯）
 *
 * 請求：GET /get_customization_options.php?item_id=6&lang=zh-TW
 * 回應：
 * {
 *   "success": true,
 *   "options": [
 *     {
 *       "option_id": 1,
 *       "item_id": 6,
 *       "group_id": 1,
 *       "group_name": "辣度",
 *       "group_type": "spice",
 *       "is_required": 1,
 *       "max_selections": 1,
 *       "values": [
 *         { "value_id": 1, "value_name": "微辣", "display_order": 1 },
 *         { "value_id": 2, "value_name": "中辣", "display_order": 2 }
 *       ]
 *     }
 *   ]
 * }
 */
header('Content-Type: application/json; charset=utf-8');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    error_log("DB Connection failed: " . $conn->connect_error);
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Connection failed"]);
    exit;
}

$item_id = isset($_GET['item_id']) ? intval($_GET['item_id']) : null;

if ($item_id === null) {
    http_response_code(400);
    echo json_encode(["success" => false, "error" => "Missing item_id parameter"]);
    $conn->close();
    exit;
}

// Get language from query parameter; fall back to English
$lang = $_GET['lang'] ?? 'en';
$validLangs = ['en', 'zh-CN', 'zh-TW'];
if (!in_array($lang, $validLangs)) {
    $lang = 'en';
}

// v5.9: Use COALESCE with translation table so non-English requests get
// translated group names when available, falling back to the English default.
$stmt = $conn->prepare("
    SELECT
        ico.option_id,
        ico.item_id,
        ico.group_id,
        COALESCE(cogt.group_name, cog.group_name) AS group_name,
        cog.group_type,
        ico.max_selections,
        ico.is_required
    FROM item_customization_options ico
    JOIN customization_option_group cog ON ico.group_id = cog.group_id
    LEFT JOIN customization_option_group_translation cogt
           ON cogt.group_id = cog.group_id AND cogt.language_code = ?
    WHERE ico.item_id = ?
    ORDER BY ico.option_id
");

if (!$stmt) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Failed to prepare query"]);
    $conn->close();
    exit;
}

$stmt->bind_param("si", $lang, $item_id);
$stmt->execute();
$result = $stmt->get_result();

$options = [];

while ($row = $result->fetch_assoc()) {
    $group_id = $row['group_id'];

    // v5.9: Use COALESCE with translation table for value names.
    $valueStmt = $conn->prepare("
        SELECT
            cov.value_id,
            COALESCE(covt.value_name, cov.value_name) AS value_name,
            cov.display_order
        FROM customization_option_value cov
        LEFT JOIN customization_option_value_translation covt
               ON covt.value_id = cov.value_id AND covt.language_code = ?
        WHERE cov.group_id = ?
        ORDER BY cov.display_order ASC, cov.value_id ASC
    ");
    $valueStmt->bind_param("si", $lang, $group_id);
    $valueStmt->execute();
    $valueResult = $valueStmt->get_result();

    $values = [];
    while ($valueRow = $valueResult->fetch_assoc()) {
        $values[] = [
            "value_id" => intval($valueRow['value_id']),
            "value_name" => $valueRow['value_name'],
            "display_order" => intval($valueRow['display_order'])
        ];
    }
    $valueStmt->close();

    $options[] = [
        "option_id" => intval($row['option_id']),
        "item_id" => intval($row['item_id']),
        "group_id" => intval($row['group_id']),
        "group_name" => $row['group_name'],
        "group_type" => $row['group_type'],
        "max_selections" => intval($row['max_selections']),
        "is_required" => intval($row['is_required']),
        "values" => $values
    ];
}

$stmt->close();
$conn->close();

echo json_encode(["success" => true, "language" => $lang, "options" => $options], JSON_UNESCAPED_UNICODE);
?>
