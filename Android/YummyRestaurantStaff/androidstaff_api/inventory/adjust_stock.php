<?php
// C:\xampp\htdocs\androidstaff_api\inventory\adjust_stock.php
header('Content-Type: application/json');
require_once 'db_connect.php';

$response = ['success' => false, 'message' => 'Invalid request.'];
$data = json_decode(file_get_contents('php://input'));

if (isset($data->material_id) && isset($data->quantity) && isset($data->action)) {
    $materialId = (int)$data->material_id;
    $quantity = (double)$data->quantity;
    $action = $data->action; // 'in' or 'out'

    if ($quantity <= 0) {
        echo json_encode(['success' => false, 'message' => 'Quantity must be positive.']);
        exit();
    }

    $operator = ($action === 'in') ? '+' : '-';
    
    // 檢查出庫時庫存是否足夠
    if ($action === 'out') {
        $check_sql = "SELECT mqty FROM materials WHERE mid = ?";
        $check_stmt = $conn->prepare($check_sql);
        $check_stmt->bind_param("i", $materialId);
        $check_stmt->execute();
        $res = $check_stmt->get_result();
        $row = $res->fetch_assoc();
        
        if (!$row || $row['mqty'] < $quantity) {
            echo json_encode(['success' => false, 'message' => 'Insufficient stock.']);
            exit();
        }
        $check_stmt->close();
    }

    // 更新庫存
    $sql = "UPDATE materials SET mqty = mqty $operator ? WHERE mid = ?";
    if ($stmt = $conn->prepare($sql)) {
        $stmt->bind_param("di", $quantity, $materialId);
        if ($stmt->execute()) {
            $response['success'] = true;
            $response['message'] = "Stock adjusted successfully.";
            
            // (選做) 寫入 consumption_history
            $logType = ($action === 'in') ? 'Restock' : 'Manual Deduction';
            $details = "Manual adjustment: {$operator}{$quantity}";
            $date = date('Y-m-d');
            $conn->query("INSERT INTO consumption_history (log_date, log_type, details) VALUES ('$date', '$logType', '$details')");
            
        } else {
            $response['message'] = "Update failed: " . $stmt->error;
        }
        $stmt->close();
    }
}

$conn->close();
echo json_encode($response);
?>