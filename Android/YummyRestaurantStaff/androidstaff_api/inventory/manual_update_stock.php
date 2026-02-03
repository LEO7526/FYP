<?php
header('Content-Type: application/json');
require_once 'db_config.php';
$response = ['success' => false, 'message' => 'Invalid request.'];
$data = json_decode(file_get_contents('php://input'));
if (isset($data->material_id) && isset($data->quantity) && isset($data->action)) {
    $materialId = (int)$data->material_id;
    $quantity = (int)$data->quantity;
    $action = $data->action; // 'in' or 'out'
    $operator = ($action === 'in') ? '+' : '-';
    $whereClause = ($action === 'in') ? "" : " AND mqty >= ?";
    $sql = "UPDATE material SET mqty = mqty $operator ? WHERE mid = ? $whereClause";
    if ($stmt = mysqli_prepare($conn, $sql)) {
        if ($action === 'in') { mysqli_stmt_bind_param($stmt, "ii", $quantity, $materialId); } 
        else { mysqli_stmt_bind_param($stmt, "iii", $quantity, $materialId, $quantity); }
        if (mysqli_stmt_execute($stmt)) {
            if (mysqli_stmt_affected_rows($stmt) > 0) {
                $response['success'] = true;
                $response['message'] = "Stock updated successfully.";
            } else { $response['message'] = "Update failed. Insufficient stock or material not found."; }
        } else { $response['message'] = 'Error executing update.'; }
        mysqli_stmt_close($stmt);
    } else { $response['message'] = 'Error preparing statement.'; }
}
mysqli_close($conn);
echo json_encode($response);
?>