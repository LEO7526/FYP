<?php
/**
 * 設置哪些選項為必填
 * 邏輯：大多數選項都應該是必填的（has choices = required）
 */
header('Content-Type: application/json');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["error" => "Connection failed"]);
    exit;
}

// 設置所有有choices的選項為必填（因為它們在UI中會被顯示）
$updateQuery = "
    UPDATE item_customization_options ico
    SET is_required = 1
    WHERE is_required = 0
    AND option_id IN (
        SELECT DISTINCT option_id FROM customization_option_choices
    )
";

if ($conn->query($updateQuery)) {
    $affectedRows = $conn->affected_rows;
    echo json_encode([
        "success" => true, 
        "message" => "Updated $affectedRows rows to set as required",
        "details" => "All options with choices are now marked as required"
    ]);
} else {
    echo json_encode(["error" => "Update failed: " . $conn->error]);
}

$conn->close();
?>
