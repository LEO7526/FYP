<?php
require_once 'db_config.php';

// 忽略用戶中斷，讓腳本持續運行
ignore_user_abort(true);
// 永不超時
set_time_limit(0);

echo "Real-time inventory processor started at " . date('Y-m-d H:i:s') . "\n";
echo "---------------------------------------------------\n";

while (true) {
    // 使用新的資料庫連線，避免長時間運行的連線超時問題
    $conn = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_NAME);
    if ($conn === false) {
        echo "Database connection failed. Retrying in 10 seconds...\n";
        sleep(10);
        continue;
    }
    mysqli_set_charset($conn, "utf8mb4");

    $sql_find_new = "SELECT oid FROM orders WHERE inventory_processed = FALSE";
    $result_new_orders = mysqli_query($conn, $sql_find_new);

    if ($result_new_orders && mysqli_num_rows($result_new_orders) > 0) {
        while ($order = mysqli_fetch_assoc($result_new_orders)) {
            $orderId = $order['oid'];
            echo "[" . date('H:i:s') . "] Processing new order ID: {$orderId}...\n";
            
            mysqli_begin_transaction($conn);
            try {
                $sql_items = "SELECT item_id, qty FROM order_items WHERE oid = ?";
                $stmt_items = mysqli_prepare($conn, $sql_items);
                mysqli_stmt_bind_param($stmt_items, "i", $orderId);
                mysqli_stmt_execute($stmt_items);
                $result_items = mysqli_stmt_get_result($stmt_items);

                if (mysqli_num_rows($result_items) == 0) {
                    throw new Exception("Order has no items.");
                }

                while ($item = mysqli_fetch_assoc($result_items)) {
                    $itemId = $item['item_id'];
                    $quantitySold = $item['qty'];

                    // *** 這裡是填上的核心扣減邏輯 ***
                    $sql_recipe = "SELECT material_id, quantity_used FROM recipes WHERE item_id = ?";
                    $stmt_recipe = mysqli_prepare($conn, $sql_recipe);
                    mysqli_stmt_bind_param($stmt_recipe, "i", $itemId);
                    mysqli_stmt_execute($stmt_recipe);
                    $result_recipe = mysqli_stmt_get_result($stmt_recipe);

                    if (mysqli_num_rows($result_recipe) == 0) {
                         // 如果這道菜沒有食譜，就跳過，不影響整個訂單
                        echo "  - Warning: No recipe found for item ID {$itemId}. Skipping deduction.\n";
                        continue;
                    }

                    while ($ingredient = mysqli_fetch_assoc($result_recipe)) {
                        $materialId = $ingredient['material_id'];
                        $totalConsumed = $quantitySold * $ingredient['quantity_used'];
                        
                        $sql_update = "UPDATE materials SET mqty = mqty - ? WHERE mid = ?";
                        $stmt_update = mysqli_prepare($conn, $sql_update);
                        mysqli_stmt_bind_param($stmt_update, "di", $totalConsumed, $materialId);
                        if (!mysqli_stmt_execute($stmt_update)) {
                            throw new Exception("Failed to deduct stock for material ID: {$materialId}");
                        }
                        echo "  - Deducted {$totalConsumed} from material ID {$materialId}.\n";
                    }
                    mysqli_stmt_close($stmt_recipe);
                }
                mysqli_stmt_close($stmt_items);

                $sql_mark_done = "UPDATE orders SET inventory_processed = TRUE WHERE oid = ?";
                $stmt_mark_done = mysqli_prepare($conn, $sql_mark_done);
                mysqli_stmt_bind_param($stmt_mark_done, "i", $orderId);
                if (!mysqli_stmt_execute($stmt_mark_done)) {
                    throw new Exception("Failed to mark order as processed.");
                }
                mysqli_stmt_close($stmt_mark_done);

                mysqli_commit($conn);
                echo "[" . date('H:i:s') . "] Order ID: {$orderId} processed successfully.\n";
                echo "---------------------------------------------------\n";

            } catch (Exception $e) {
                mysqli_rollback($conn);
                echo "[" . date('H:i:s') . "] Error processing order ID {$orderId}: " . $e->getMessage() . "\n";
                // 發生錯誤時，可以考慮將訂單標記為錯誤狀態，避免無限重試
                mysqli_query($conn, "UPDATE orders SET inventory_processed = TRUE WHERE oid = {$orderId}");
                echo "---------------------------------------------------\n";
            }
        }
    } else {
        // 為了讓您看到它在工作，我們讓它每5秒印出一個點
        echo ".";
        sleep(5);
    }
    
    // 關閉這次迴圈的連線
    mysqli_close($conn);
}
?>