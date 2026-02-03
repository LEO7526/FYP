<?php
// get_table_detail.php (5.7 版：不依賴 table_orders.status)
require_once 'db_connect.php';

// 關閉錯誤顯示，確保只輸出 JSON
ini_set('display_errors', 0);
header('Content-Type: application/json');

$tid = $_GET['tid'] ?? 0;
if ($tid == 0) {
    echo json_encode(["status" => "error", "message" => "Invalid Table ID"]);
    exit();
}

try {
    // 1. 第一步：檢查現場訂單 (透過 table_orders 找到 orders 表)
    // 邏輯：找這桌最新的一筆，且訂單狀態是 1 (New) 或 2 (Cooking)
    $sqlLive = "SELECT 
                    o.oid, 
                    o.ostatus, 
                    c.cname,
                    o.order_type,
                    TIMESTAMPDIFF(MINUTE, o.odate, NOW()) as duration_min
                FROM table_orders t
                JOIN orders o ON t.oid = o.oid
                LEFT JOIN customer c ON o.cid = c.cid
                WHERE t.table_number = ? AND o.ostatus IN (1, 2)
                ORDER BY t.toid DESC LIMIT 1";

    $stmt = $conn->prepare($sqlLive);
    $stmt->bind_param("i", $tid);
    $stmt->execute();
    $resLive = $stmt->get_result();

    if ($rowLive = $resLive->fetch_assoc()) {
        // 有現場客人，開始抓菜名
        $items = [];
        $oid = $rowLive['oid'];
        
        $sqlItems = "SELECT mit.item_name, oi.qty 
                     FROM order_items oi
                     JOIN menu_item_translation mit ON oi.item_id = mit.item_id
                     WHERE oi.oid = ? AND mit.language_code = 'en'";
        
        $stmtItem = $conn->prepare($sqlItems);
        $stmtItem->bind_param("i", $oid);
        $stmtItem->execute();
        $resItems = $stmtItem->get_result();
        
        while ($item = $resItems->fetch_assoc()) {
            $items[] = $item['qty'] . "x " . $item['item_name'];
        }

        // 決定顯示狀態文字
        $displayStatus = ($rowLive['ostatus'] == 1) ? "New Order" : "Cooking";

        echo json_encode([
            "status" => "success",
            "data" => [
                "type" => "live",
                "table_status" => $displayStatus,
                "customer_name" => $rowLive['cname'] ?? "Walk-in Guest",
                "duration" => (int)$rowLive['duration_min'],
                "items" => $items
            ]
        ]);
        exit();
    }

    // 2. 第二步：如果沒人坐，檢查是否有「預約」(Booking)
    $currentDate = date('Y-m-d');
    $currentTime = date('H:i:s');
    
    $sqlBooking = "SELECT bkcname, pnum, remark 
                   FROM booking 
                   WHERE tid = ? AND bdate = ? AND status IN (1, 2)
                   AND btime BETWEEN SUBTIME(?, '00:30:00') AND ADDTIME(?, '00:30:00')
                   LIMIT 1";

    $stmt2 = $conn->prepare($sqlBooking);
    $stmt2->bind_param("isss", $tid, $currentDate, $currentTime, $currentTime);
    $stmt2->execute();
    $resBooking = $stmt2->get_result();

    if ($rowBooking = $resBooking->fetch_assoc()) {
        echo json_encode([
            "status" => "success",
            "data" => [
                "type" => "booking",
                "table_status" => "Reserved",
                "customer_name" => $rowBooking['bkcname'],
                "pnum" => (int)$rowBooking['pnum'],
                "duration" => 0,
                "remark" => $rowBooking['remark'] ?? "No remark",
                "items" => []
            ]
        ]);
        exit();
    }

    // 3. 第三步：什麼都沒有，就是空桌
    echo json_encode([
        "status" => "success",
        "data" => [
            "type" => "empty",
            "table_status" => "Available",
            "customer_name" => "None",
            "duration" => 0,
            "items" => []
        ]
    ]);

} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => "Server Error: " . $e->getMessage()]);
}

$conn->close();
?>