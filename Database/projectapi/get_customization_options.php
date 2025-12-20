<?php
/**
 * 取得菜品的自訂選項（v4.5版本 - 使用群組-值的階層結構）
 * 
 * 請求：GET /get_customization_options.php?item_id=6
 * 回應：
 * {
 *   "success": true,
 *   "options": [
 *     {
 *       "option_id": 1,
 *       "item_id": 6,
 *       "group_id": 1,
 *       "group_name": "Spice Level",
 *       "group_type": "spice",
 *       "is_required": 1,
 *       "max_selections": 1,
 *       "values": [
 *         { "value_id": 1, "value_name": "Mild", "display_order": 1 },
 *         { "value_id": 2, "value_name": "Hot", "display_order": 2 }
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

// ✅ v4.5: 從item_customization_options和customization_option_group獲取選項
// JOIN以獲取group_id、group_name、group_type
$stmt = $conn->prepare("
    SELECT 
        ico.option_id,
        ico.item_id,
        ico.group_id,
        cog.group_name,
        cog.group_type,
        ico.max_selections,
        ico.is_required
    FROM item_customization_options ico
    JOIN customization_option_group cog ON ico.group_id = cog.group_id
    WHERE ico.item_id = ?
    ORDER BY ico.option_id
");

if (!$stmt) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Failed to prepare query"]);
    $conn->close();
    exit;
}

$stmt->bind_param("i", $item_id);
$stmt->execute();
$result = $stmt->get_result();

$options = [];

while ($row = $result->fetch_assoc()) {
    $group_id = $row['group_id'];
    
    // ✅ 從customization_option_value獲取此group的所有值
    $valueStmt = $conn->prepare("
        SELECT value_id, value_name, display_order
        FROM customization_option_value
        WHERE group_id = ?
        ORDER BY display_order ASC, value_id ASC
    ");
    $valueStmt->bind_param("i", $group_id);
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

echo json_encode(["success" => true, "options" => $options]);
?>
