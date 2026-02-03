<?php
session_start();
$pdo = new PDO("mysql:host=localhost;dbname=projectdb;charset=utf8", "root", "");
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

// 取得使用者身份
$cid = $_COOKIE['cid'] ?? '';
if (!$cid) {
    echo "<p>請先登入以查詢訂單。</p>";
    exit;
}

$action = $_POST['action'] ?? '';

// ✅ 取消訂單
if ($action === 'cancel') {
    $bid = $_POST['bid'] ?? '';
    if ($bid) {
        $stmt = $pdo->prepare("UPDATE booking SET status = 0 WHERE bid = ? AND cid = ?");
        $stmt->execute([$bid, $cid]);
        echo "<p>訂單已取消。</p>";
    }
}

// ✅ 更新訂單
if ($action === 'update') {
    $bid = $_POST['bid'] ?? '';
    $bdate = $_POST['bdate'] ?? '';
    $btime = $_POST['btime'] ?? '';
    $pnum = $_POST['pnum'] ?? '';
    $tid = $_POST['tid'] ?? '';
    $purpose = $_POST['purpose'] ?? '';
    $remark = $_POST['remark'] ?? '';

    if ($bid && $bdate && $btime && $pnum && $tid) {
        $stmt = $pdo->prepare("UPDATE booking SET bdate = ?, btime = ?, pnum = ?, tid = ?, purpose = ?, remark = ? WHERE bid = ? AND cid = ?");
        $stmt->execute([$bdate, $btime, $pnum, $tid, $purpose, $remark, $bid, $cid]);
        echo "<p>訂單已更新。</p>";
    }
}

// ✅ 顯示修改表單
if ($action === 'edit') {
    $bid = $_POST['bid'] ?? '';
    if ($bid) {
        $stmt = $pdo->prepare("SELECT * FROM booking WHERE bid = ? AND cid = ?");
        $stmt->execute([$bid, $cid]);
        $b = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($b) {
            // 查詢可用座位（排除重疊時段）
            $stmt = $pdo->prepare("SELECT tid FROM booking WHERE bdate = ? AND btime BETWEEN DATE_SUB(?, INTERVAL 90 MINUTE) AND DATE_ADD(?, INTERVAL 90 MINUTE) AND status != 0 AND bid != ?");
            $stmt->execute([$b['bdate'], $b['btime'], $b['btime'], $bid]);
            $booked = $stmt->fetchAll(PDO::FETCH_COLUMN);

            if (!empty($booked)) {
                $placeholders = implode(',', array_fill(0, count($booked), '?'));
                $sql = "SELECT tid FROM seatingchart WHERE capacity >= ? AND tid NOT IN ($placeholders)";
                $stmt = $pdo->prepare($sql);
                $stmt->execute(array_merge([$b['pnum']], $booked));
            } else {
                $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity >= ?");
                $stmt->execute([$b['pnum']]);
            }

            $availableTables = $stmt->fetchAll(PDO::FETCH_COLUMN);

            echo "<div id='editBookingForm'>
    <h3>Modify Reservation</h3>
    <form method='post' class='reservation-form update-form'>
        <input type='hidden' name='action' value='update'>
        <input type='hidden' name='bid' value='{$b['bid']}'>

        <label for='date'>Dining Date:</label>
        <input type='date' id='edit-date' name='bdate' value='{$b['bdate']}' required>

        <label for='time'>Dining Time:</label>
        <select id='edit-time' name='btime' required>
            <option value=''>Select Time</option>";
            $startHour = 11;
            $endHour = 22;
            $stepMinutes = 30;
            $durationMinutes = 90;
            $finalEndTime = 23;

            $current = new DateTime();
            $current->setTime($startHour, 0);
            $endLimit = new DateTime();
            $endLimit->setTime($finalEndTime, 0);

            while (true) {
                $end = clone $current;
                $end->modify("+{$durationMinutes} minutes");
                if ($end > $endLimit) break;

                $startStr = $current->format('H:i');
                $endStr = $end->format('H:i');
                $label = "$startStr - $endStr";
                $value = $startStr . ":00";
                $selected = ($value === $b['btime']) ? 'selected' : '';
                echo "<option value='$value' $selected>$label</option>";

                $current->modify("+{$stepMinutes} minutes");
            }
            echo "</select>

        <label for='guests'>Number of Guests:</label>
        <select id='edit-guests' name='pnum' required>
            <option value=''>Select</option>";
            for ($i = 1; $i <= 8; $i++) {
                $selected = ($i == $b['pnum']) ? 'selected' : '';
                echo "<option value='$i' $selected>$i</option>";
            }
            echo "</select>

        <label for='purpose'>Purpose of booking</label>
        <select id='purpose' name='purpose'>
            <option value='Date Night' " . ($b['purpose'] === 'Date Night' ? 'selected' : '') . ">Date Night</option>
            <option value='Family Dinner' " . ($b['purpose'] === 'Family Dinner' ? 'selected' : '') . ">Family Dinner</option>
            <option value='Business Meeting' " . ($b['purpose'] === 'Business Meeting' ? 'selected' : '') . ">Business Meeting</option>
            <option value='Lunch Meeting' " . ($b['purpose'] === 'Lunch Meeting' ? 'selected' : '') . ">Lunch Meeting</option>
            <option value='Birthday Celebration' " . ($b['purpose'] === 'Birthday Celebration' ? 'selected' : '') . ">Birthday Celebration</option>
        </select>

        <label for='remark'>Remark:</label>
        <input id='remark' name='remark' type='text' value='{$b['remark']}'>

        <button type='button' id='edit-loadTablesBtn'>Load Available Tables</button>
<div id='edit-table-map' class='table-map'></div>
<input type='hidden' id='edit-tid' name='tid' required>



        <button type='submit'>Confirm Modification</button>
    </form>
</div>";
            exit;
        }
    }
}


$stmt = $pdo->prepare("SELECT * FROM booking WHERE cid = ? AND status != 0 ORDER BY bdate ASC");
$stmt->execute([$cid]);
$bookings = $stmt->fetchAll(PDO::FETCH_ASSOC);

if ($bookings) {
    echo "<h4>您的訂單記錄：</h4><ul>";
    foreach ($bookings as $b) {
        echo "<li>
            <strong>訂單編號：</strong> {$b['bid']}<br>
            日期：{$b['bdate']}，時間：{$b['btime']}，人數：{$b['pnum']}，座位：{$b['tid']}<br>
            <form method='post' class='edit-form' style='display:inline;'>
                <input type='hidden' name='action' value='edit'>
                <input type='hidden' name='bid' value='{$b['bid']}'>
                <button type='submit'>修改</button>
            </form>
            <form method='post' class='cancel-form' style='display:inline;' onsubmit='return confirm(\"確定取消這筆訂位嗎？\");'>
                <input type='hidden' name='action' value='cancel'>
                <input type='hidden' name='bid' value='{$b['bid']}'>
                <button type='submit'>取消</button>
            </form>
        </li>";
    }
    echo "</ul>";
} else {
    echo "<p>目前沒有有效訂單。</p>";
}
?>
