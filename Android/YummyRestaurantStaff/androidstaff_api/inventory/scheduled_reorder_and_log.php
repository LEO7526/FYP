<?php
date_default_timezone_set('Asia/Hong_Kong');
require_once 'db_config.php';

// 尋找低庫存物料
$sql_find_low_stock = "SELECT mid, mname, mqty, mreorderqty FROM material WHERE mqty <= mreorderqty";
$result = mysqli_query($conn, $sql_find_low_stock);

if (!$result || mysqli_num_rows($result) == 0) {
    exit("No items needed reordering.");
}

$reordered_items = [];
while ($row = mysqli_fetch_assoc($result)) {
    $materialId = $row['mid'];
    $materialName = $row['mname'];
    $quantity_to_add = $row['mreorderqty']; // 假設補貨量等於再訂購點

    // 1. 更新庫存
    $sql_update_stock = "UPDATE material SET mqty = mqty + ? WHERE mid = ?";
    if ($stmt_update = mysqli_prepare($conn, $sql_update_stock)) {
        mysqli_stmt_bind_param($stmt_update, "ii", $quantity_to_add, $materialId);
        
        if (mysqli_stmt_execute($stmt_update)) {
            // 如果更新成功，則記錄到 reorder_history
            $sql_insert_log = "INSERT INTO reorder_history (material_name, reordered_quantity) VALUES (?, ?)";
            if ($stmt_log = mysqli_prepare($conn, $sql_insert_log)) {
                mysqli_stmt_bind_param($stmt_log, "si", $materialName, $quantity_to_add);
                mysqli_stmt_execute($stmt_log);
                mysqli_stmt_close($stmt_log);
            }
            
            // 為通知做準備
            $reordered_items[] = ['name' => $materialName, 'reordered_amount' => $quantity_to_add];
        }
        mysqli_stmt_close($stmt_update);
    }
}

// 2. 建立通知
if (!empty($reordered_items)) {
    $notification_title = "Daily Automatic Reorder Report";
    $notification_body = "";
    foreach ($reordered_items as $item) {
        $notification_body .= "- " . $item['name'] . " reordered by " . $item['reordered_amount'] . " units.\n";
    }
    $notification_body = rtrim($notification_body, "\n");

    $sql_insert_notification = "INSERT INTO notifications (title, message) VALUES (?, ?)";
    if ($stmt_notify = mysqli_prepare($conn, $sql_insert_notification)) {
        mysqli_stmt_bind_param($stmt_notify, "ss", $notification_title, $notification_body);
        mysqli_stmt_execute($stmt_notify);
        mysqli_stmt_close($stmt_notify);
        echo "Reorder and notification process completed for " . count($reordered_items) . " items.\n";
    }
}

mysqli_close($conn);
?>