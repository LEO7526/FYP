<?php
/**
 * 取得菜品的自訂選項
 * 
 * 請求：GET /get_customization_options.php?item_id=6
 * 回應：
 * {
 *   "success": true,
 *   "options": [
 *     {
 *       "option_id": 1,
 *       "item_id": 6,
 *       "option_name": "Spice Level",
 *       "option_type": "single_choice",
 *       "is_required": 1,
 *       "max_selections": 1,
 *       "choices": [
 *         { "choice_id": 1, "choice_name": "Mild", "additional_cost": 0 },
 *         { "choice_id": 2, "choice_name": "Hot", "additional_cost": 0 }
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

// Get customization options for this item
$stmt = $conn->prepare("
    SELECT option_id, item_id, option_name, max_selections, is_required
    FROM item_customization_options
    WHERE item_id = ?
    ORDER BY option_id
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
    $option_id = $row['option_id'];
    
    // Get choices for this option
    $choiceStmt = $conn->prepare("
        SELECT choice_id, choice_name, additional_cost
        FROM customization_option_choices
        WHERE option_id = ?
        ORDER BY display_order, choice_id
    ");
    $choiceStmt->bind_param("i", $option_id);
    $choiceStmt->execute();
    $choiceResult = $choiceStmt->get_result();
    
    $choices = [];
    while ($choiceRow = $choiceResult->fetch_assoc()) {
        $choices[] = [
            "choice_id" => intval($choiceRow['choice_id']),
            "choice_name" => $choiceRow['choice_name'],
            "additional_cost" => floatval($choiceRow['additional_cost'])
        ];
    }
    $choiceStmt->close();
    
    $options[] = [
        "option_id" => intval($row['option_id']),
        "item_id" => intval($row['item_id']),
        "option_name" => $row['option_name'],
        "max_selections" => intval($row['max_selections']),
        "is_required" => intval($row['is_required']),  // ✅ 新增
        "choices" => $choices
    ];
}

$stmt->close();
$conn->close();

echo json_encode([
    "success" => true,
    "options" => $options
]);
?>
