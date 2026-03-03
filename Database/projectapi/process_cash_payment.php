<?php
// process_cash_payment.php - 處理現金支付確認
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "ProjectDB";

$input = json_decode(file_get_contents("php://input"), true);

try {
    $conn = new mysqli($servername, $username, $password, $dbname);

    if ($conn->connect_error) {
        throw new Exception("Database connection failed: " . $conn->connect_error);
    }

    $oid = intval($input['order_id'] ?? 0);
    $staff_id = intval($input['staff_id'] ?? 0);

    if ($oid <= 0) {
        throw new Exception("無效的訂單ID");
    }

    $conn->begin_transaction();

    $checkStmt = $conn->prepare("
        SELECT ostatus, payment_method, table_number, cid
        FROM orders
        WHERE oid = ?
    ");
    $checkStmt->bind_param("i", $oid);
    $checkStmt->execute();
    $result = $checkStmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("訂單不存在");
    }

    $order = $result->fetch_assoc();
    $checkStmt->close();

    if ($order['payment_method'] !== 'cash') {
        throw new Exception("此訂單不是現金支付");
    }

    if (intval($order['ostatus']) !== 0) {
        throw new Exception("訂單狀態不正確，無法確認");
    }

    $updateStmt = $conn->prepare("
        UPDATE orders
        SET ostatus = 1,
            note = CONCAT(
                IFNULL(note, ''),
                CASE WHEN note IS NOT NULL AND note != '' THEN '\n' ELSE '' END,
                'Cash payment confirmed - Staff ID: ', ?, ' Time: ', NOW()
            )
        WHERE oid = ?
    ");
    $updateStmt->bind_param("ii", $staff_id, $oid);

    if (!$updateStmt->execute()) {
        throw new Exception("更新訂單狀態失敗");
    }
    $updateStmt->close();

    $cid = intval($order['cid'] ?? 0);
    $couponPointsToAdd = 0;
    $newBalance = null;

    if ($cid > 0) {
        $amountStmt = $conn->prepare("
            SELECT COALESCE(SUM(mi.item_price * oi.qty), 0) AS total
            FROM order_items oi
            JOIN menu_item mi ON oi.item_id = mi.item_id
            WHERE oi.oid = ?
        ");
        if (!$amountStmt) {
            throw new Exception("計算訂單金額失敗: " . $conn->error);
        }

        $amountStmt->bind_param("i", $oid);
        $amountStmt->execute();
        $amountResult = $amountStmt->get_result();
        $amountRow = $amountResult->fetch_assoc();
        $totalAmount = intval($amountRow['total'] ?? 0);
        $amountStmt->close();

        $couponPointsToAdd = $totalAmount;

        if ($couponPointsToAdd > 0) {
            $idempotencyNote = "Cash payment confirmed for order #" . $oid;

            $checkHistoryStmt = $conn->prepare("SELECT 1 FROM coupon_point_history WHERE cid = ? AND action = 'earn' AND note = ? LIMIT 1");
            if (!$checkHistoryStmt) {
                throw new Exception("檢查積分歷史失敗: " . $conn->error);
            }
            $checkHistoryStmt->bind_param("is", $cid, $idempotencyNote);
            $checkHistoryStmt->execute();
            $alreadyAwarded = $checkHistoryStmt->get_result()->num_rows > 0;
            $checkHistoryStmt->close();

            if (!$alreadyAwarded) {
                $pointsStmt = $conn->prepare("UPDATE customer SET coupon_point = coupon_point + ? WHERE cid = ?");
                if (!$pointsStmt) {
                    throw new Exception("更新客戶積分失敗: " . $conn->error);
                }
                $pointsStmt->bind_param("ii", $couponPointsToAdd, $cid);
                if (!$pointsStmt->execute()) {
                    throw new Exception("寫入客戶積分失敗: " . $pointsStmt->error);
                }
                $pointsStmt->close();

                $balanceStmt = $conn->prepare("SELECT coupon_point FROM customer WHERE cid = ?");
                if (!$balanceStmt) {
                    throw new Exception("查詢積分餘額失敗: " . $conn->error);
                }
                $balanceStmt->bind_param("i", $cid);
                $balanceStmt->execute();
                $balanceResult = $balanceStmt->get_result();
                $balanceRow = $balanceResult->fetch_assoc();
                $newBalance = intval($balanceRow['coupon_point'] ?? 0);
                $balanceStmt->close();

                $historyNote = $idempotencyNote;
                if ($staff_id > 0) {
                    $historyNote .= " by staff #" . $staff_id;
                }

                $historyStmt = $conn->prepare("INSERT INTO coupon_point_history (cid, coupon_id, delta, resulting_points, action, note) VALUES (?, NULL, ?, ?, 'earn', ?)");
                if (!$historyStmt) {
                    throw new Exception("寫入積分歷史失敗: " . $conn->error);
                }
                $historyStmt->bind_param("iiis", $cid, $couponPointsToAdd, $newBalance, $historyNote);
                if (!$historyStmt->execute()) {
                    throw new Exception("寫入積分歷史失敗: " . $historyStmt->error);
                }
                $historyStmt->close();
            }
        }
    }

    $table_number = intval($order['table_number'] ?? 0);
    if ($table_number > 0) {
        $checkTableStmt = $conn->prepare("
            SELECT toid FROM table_orders
            WHERE table_number = ? AND oid = ?
        ");
        $checkTableStmt->bind_param("ii", $table_number, $oid);
        $checkTableStmt->execute();
        $tableResult = $checkTableStmt->get_result();
        $checkTableStmt->close();

        if ($tableResult->num_rows === 0) {
            $insertTableStmt = $conn->prepare("
                INSERT INTO table_orders (table_number, oid, staff_id, created_at)
                VALUES (?, ?, ?, NOW())
            ");
            $insertTableStmt->bind_param("iii", $table_number, $oid, $staff_id);
            $insertTableStmt->execute();
            $insertTableStmt->close();
        } else {
            $updateTableStmt = $conn->prepare("
                UPDATE table_orders
                SET staff_id = ?
                WHERE table_number = ? AND oid = ?
            ");
            $updateTableStmt->bind_param("iii", $staff_id, $table_number, $oid);
            $updateTableStmt->execute();
            $updateTableStmt->close();
        }
    }

    $conn->commit();

    echo json_encode([
        'success' => true,
        'message' => '現金支付確認成功',
        'oid' => $oid,
        'table_number' => $table_number,
        'couponPointsAdded' => $couponPointsToAdd,
        'newCouponBalance' => $newBalance
    ]);

} catch (Exception $e) {
    if (isset($conn)) {
        $conn->rollback();
    }

    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
}

if (isset($conn)) {
    $conn->close();
}
?>