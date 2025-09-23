<?php

session_start();
$pdo = new PDO("mysql:host=localhost;dbname=projectdb;charset=utf8", "root", "");
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'fetch_tables') {
    $bdate = $_POST['bdate'] ?? '';
    $btime = $_POST['btime'] ?? '';
    $pnum = is_numeric($_POST['pnum'] ?? '') ? (int)$_POST['pnum'] : 0;

    if ($bdate && $btime && $pnum) {
        $stmt = $pdo->prepare("SELECT tid FROM booking WHERE bdate = ? AND btime BETWEEN DATE_SUB(?, INTERVAL 90 MINUTE) AND DATE_ADD(?, INTERVAL 90 MINUTE) AND status != 2");
        $stmt->execute([$bdate, $btime, $btime]);
        $booked = $stmt->fetchAll(PDO::FETCH_COLUMN);

        if (!empty($booked)) {
            $placeholders = implode(',', array_fill(0, count($booked), '?'));
            $sql = "SELECT tid FROM seatingchart WHERE capacity >= ? AND tid NOT IN ($placeholders)";
            $stmt = $pdo->prepare($sql);
            $stmt->execute(array_merge([$pnum], $booked));
        } else {
            $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity >= ?");
            $stmt->execute([$pnum]);
        }

        $available = $stmt->fetchAll(PDO::FETCH_ASSOC);
        header('Content-Type: application/json');
        echo json_encode($available);
        exit;
    }

    echo json_encode([]);
    exit;
}


$cid = $_COOKIE['cid'] ?? '';
$action = $_POST['action'] ?? '';

if (!$cid) {
    echo "<p>請先登入以查詢訂枱。</p>";
    exit;
}

// 顯示訂枱記錄
function showBookings($pdo, $cid) {
    $stmt = $pdo->prepare("SELECT * FROM booking WHERE cid = ? AND status != 2 ORDER BY bdate ASC");
    $stmt->execute([$cid]);
    $bookings = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if ($bookings) {
        echo "<h4>您的訂枱記錄：</h4><ul>";
        foreach ($bookings as $b) {
            echo "<li>
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
        echo "<p>目前沒有訂枱記錄。</p>";
    }
}

// 處理取消
if ($action === 'cancel') {
    $bid = $_POST['bid'] ?? '';
    if ($bid) {
        $stmt = $pdo->prepare("UPDATE booking SET status = 2 WHERE bid = ? AND cid = ?");
        $stmt->execute([$bid, $cid]);
        echo "<p>訂位已取消。</p>";
        showBookings($pdo, $cid);
        exit;
    }
}

// 處理更新
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
        echo "<p>訂位已更新。</p>";
        showBookings($pdo, $cid);
        exit;
    }
}

// 處理修改表單顯示
if ($action === 'edit') {
    $bid = $_POST['bid'] ?? '';
    if ($bid) {
        $stmt = $pdo->prepare("SELECT * FROM booking WHERE bid = ? AND cid = ?");
        $stmt->execute([$bid, $cid]);
        $b = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($b) {
            // 查詢可預約座位
            $date = $b['bdate'];
            $time = $b['btime'];
            $guests = $b['pnum'];

            // 找出已被預約的座位
            $stmt = $pdo->prepare("SELECT tid FROM booking WHERE bdate = ? AND btime BETWEEN DATE_SUB(?, INTERVAL 90 MINUTE) AND DATE_ADD(?, INTERVAL 90 MINUTE) AND status != 2");
            $stmt->execute([$date, $time, $time]);
            $booked = $stmt->fetchAll(PDO::FETCH_COLUMN);

            // 查詢可用座位
            if (!empty($booked)) {
                $placeholders = implode(',', array_fill(0, count($booked), '?'));
                $sql = "SELECT tid FROM seatingchart WHERE capacity >= ? AND tid NOT IN ($placeholders)";
                $stmt = $pdo->prepare($sql);
                $stmt->execute(array_merge([$guests], $booked));
            } else {
                $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity >= ?");
                $stmt->execute([$guests]);
            }

            $availableTables = $stmt->fetchAll(PDO::FETCH_COLUMN);

            echo "<h4>修改訂位</h4>
            <form method='post' class='update-form'>
                <input type='hidden' name='action' value='update'>
                <input type='hidden' name='bid' value='{$b['bid']}'>
                <label>日期：</label><input type='date' name='bdate' value='{$b['bdate']}' required><br>
                <label>時間：</label><input type='time' name='btime' value='{$b['btime']}' required><br>
                <label>人數：</label><input type='number' name='pnum' value='{$b['pnum']}' min='1' max='8' required><br>
                <label>座位：</label>
                <select name='tid' required>
                    <option value=''>請選擇座位</option>";

            foreach ($availableTables as $tid) {
                $selected = ($tid == $b['tid']) ? 'selected' : '';
                echo "<option value='$tid' $selected>Table $tid</option>";
            }

            echo "</select><br>
                <label>用途：</label><input type='text' name='purpose' value='{$b['purpose']}'><br>
                <label>備註：</label><input type='text' name='remark' value='{$b['remark']}'><br>
                <button type='submit'>確認修改</button>
            </form>";
            echo <<<JS
<script>
document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('.update-form');
    if (!form) return;

    const dateInput = form.querySelector('input[name="bdate"]');
    const timeInput = form.querySelector('input[name="btime"]');
    const guestsInput = form.querySelector('input[name="pnum"]');
    const tableSelect = form.querySelector('select[name="tid"]');

    function fetchTables() {
        const bdate = dateInput.value;
        const btime = timeInput.value;
        const pnum = guestsInput.value;
        const currentTid = tableSelect.value;

        if (bdate && btime && pnum) {
            fetch('myBooking.php', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `action=fetch_tables&bdate=\${bdate}&btime=\${btime}&pnum=\${pnum}`
            })
            .then(response => response.json())
            .then(data => {
                tableSelect.innerHTML = '<option value="">請選擇座位</option>';
                if (data.length === 0) {
                    tableSelect.innerHTML += '<option value="">目前無可用座位</option>';
                } else {
                    data.forEach(table => {
                        const option = document.createElement('option');
                        option.value = table.tid;
                        option.textContent = `Table \${table.tid}`;
                        if (table.tid == currentTid) {
                            option.selected = true;
                        }
                        tableSelect.appendChild(option);
                    });
                }
            })
            .catch(error => {
                console.error('座位載入失敗:', error);
                tableSelect.innerHTML = '<option value="">載入失敗</option>';
            });
        }
    }

    dateInput.addEventListener('change', fetchTables);
    timeInput.addEventListener('change', fetchTables);
    guestsInput.addEventListener('change', fetchTables);
});
</script>
JS;

            exit;

        }
    }
}



// 預設顯示訂枱記錄
showBookings($pdo, $cid);
?>
