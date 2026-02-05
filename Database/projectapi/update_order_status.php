<?php
// update_order_status.php - For updating order status in kitchen
require_once '../../conn.php';
header('Content-Type: application/json');

$oid = $_POST['oid'] ?? '';
$newStatus = $_POST['status'] ?? '';

if (empty($oid) || empty($newStatus)) {
    echo json_encode(["status" => "error", "message" => "Missing data"]);
    exit();
}

// Update orders table
$sql = "UPDATE orders SET ostatus = ? WHERE oid = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $newStatus, $oid);

if ($stmt->execute()) {
    
    // ✅ NEW: Deduct materials when order status changes from 1 to 2 (New/Making -> Delivered)
    if ($newStatus == 2) {
        deductMaterialsForOrder($conn, $oid);
    }
    
    // Sync table_orders status if needed
    // If order becomes Cooking (2) -> table status 'ordering'
    // If order becomes Delivered (3) -> table status 'available'
    if ($newStatus == 2) {
        $updateTableSql = "UPDATE table_orders SET status = 'ordering' WHERE oid = ?";
    } else if ($newStatus == 3) {
        $updateTableSql = "UPDATE table_orders SET status = 'available' WHERE oid = ?";
    }
    
    if (isset($updateTableSql)) {
        $stmtTable = $conn->prepare($updateTableSql);
        if ($stmtTable) {
            $stmtTable->bind_param("i", $oid);
            $stmtTable->execute();
            $stmtTable->close();
        } else {
            error_log("Failed to prepare table_orders update query: " . $conn->error);
        }
    }
    
    echo json_encode(["status" => "success", "message" => "Order updated successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to update order"]);
}

$conn->close();

/**
 * Deduct materials from inventory when an order is completed (ostatus 1 -> 2)
 */
function deductMaterialsForOrder($conn, $oid) {
    error_log("deductMaterialsForOrder: Starting material deduction for order $oid");
    
    // Get all items in this order
    $orderItemsQuery = "SELECT item_id, qty FROM order_items WHERE oid = ?";
    $stmt = $conn->prepare($orderItemsQuery);
    $stmt->bind_param("i", $oid);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $totalDeductions = [];
    
    // For each item in the order, calculate total material requirements
    while ($row = $result->fetch_assoc()) {
        $item_id = $row['item_id'];
        $order_qty = $row['qty'];
        
        error_log("Processing item_id=$item_id with order quantity=$order_qty");
        
        // Get recipe materials for this menu item
        $recipeQuery = "SELECT mid, quantity FROM recipe_materials WHERE item_id = ?";
        $recipeStmt = $conn->prepare($recipeQuery);
        $recipeStmt->bind_param("i", $item_id);
        $recipeStmt->execute();
        $recipeResult = $recipeStmt->get_result();
        
        while ($recipeRow = $recipeResult->fetch_assoc()) {
            $material_id = $recipeRow['mid'];
            $recipe_qty = $recipeRow['quantity'];
            $total_needed = $recipe_qty * $order_qty;
            
            // Accumulate total deductions for each material
            if (!isset($totalDeductions[$material_id])) {
                $totalDeductions[$material_id] = 0;
            }
            $totalDeductions[$material_id] += $total_needed;
            
            error_log("Material $material_id: recipe_qty=$recipe_qty * order_qty=$order_qty = total_needed=$total_needed");
        }
        $recipeStmt->close();
    }
    $stmt->close();
    
    // Now deduct the total calculated amounts from materials inventory
    foreach ($totalDeductions as $material_id => $total_deduction) {
        // Check current stock
        $checkStockQuery = "SELECT mname, mqty, unit FROM materials WHERE mid = ?";
        $checkStmt = $conn->prepare($checkStockQuery);
        $checkStmt->bind_param("i", $material_id);
        $checkStmt->execute();
        $stockResult = $checkStmt->get_result();
        
        if ($stockRow = $stockResult->fetch_assoc()) {
            $current_stock = $stockRow['mqty'];
            $material_name = $stockRow['mname'];
            $unit = $stockRow['unit'];
            $new_stock = $current_stock - $total_deduction;
            
            // Deduct the material (allow negative stock for now)
            $updateQuery = "UPDATE materials SET mqty = ? WHERE mid = ?";
            $updateStmt = $conn->prepare($updateQuery);
            $updateStmt->bind_param("di", $new_stock, $material_id);
            
            if ($updateStmt->execute()) {
                error_log("✅ Deducted material '$material_name' (ID: $material_id): $current_stock $unit - $total_deduction $unit = $new_stock $unit");
                
                // Log warning if stock is low or negative
                if ($new_stock <= 0) {
                    error_log("⚠️  WARNING: Material '$material_name' is out of stock! Current: $new_stock $unit");
                }
            } else {
                error_log("❌ Failed to deduct material '$material_name' (ID: $material_id): " . $updateStmt->error);
            }
            $updateStmt->close();
        } else {
            error_log("❌ Material with ID $material_id not found in inventory");
        }
        $checkStmt->close();
    }
    
    error_log("deductMaterialsForOrder: Completed material deduction for order $oid");
}
?>