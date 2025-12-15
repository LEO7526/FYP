<?php
header('Content-Type: application/json');

$conn = new mysqli('localhost', 'root', '', 'ProjectDB');
if ($conn->connect_error) {
    echo json_encode(['error' => $conn->connect_error]);
    exit;
}

// Get menu items and their customization options
$sql = "SELECT 
            mi.item_id, 
            mi.category_id,
            ico.option_id,
            ico.option_name,
            ico.max_selections
        FROM menu_item mi
        LEFT JOIN item_customization_options ico ON mi.item_id = ico.item_id
        ORDER BY mi.item_id, ico.option_id";

$result = $conn->query($sql);
if (!$result) {
    echo json_encode(['error' => $conn->error]);
    exit;
}

$data = [];

while ($row = $result->fetch_assoc()) {
    $item_id = $row['item_id'];
    if (!isset($data[$item_id])) {
        $data[$item_id] = [
            'item_id' => $item_id,
            'category_id' => $row['category_id'],
            'options' => []
        ];
    }
    
    if ($row['option_id']) {
        $data[$item_id]['options'][] = [
            'option_id' => $row['option_id'],
            'option_name' => $row['option_name'],
            'max_selections' => $row['max_selections']
        ];
    }
}

echo json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);

$conn->close();
?>
