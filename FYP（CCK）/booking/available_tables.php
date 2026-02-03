<?php
header('Content-Type: application/json');
$pdo = new PDO("mysql:host=localhost;dbname=projectdb;charset=utf8", "root", "");
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

$date = $_POST['date'] ?? '';
$time = $_POST['time'] ?? '';
$guests = is_numeric($_POST['guests'] ?? '') ? (int)$_POST['guests'] : 0;

if ($date && $time && $guests) {
    $selectedStart = new DateTime("$date $time");
    $selectedEnd = clone $selectedStart;
    $selectedEnd->modify('+90 minutes');

    $startStr = $selectedStart->format('Y-m-d H:i:s');
    $endStr = $selectedEnd->format('Y-m-d H:i:s');

    // 找出已被訂位的桌子
    $stmt = $pdo->prepare("
        SELECT tid FROM booking 
        WHERE CONCAT(bdate, ' ', btime) < ? 
        AND ADDTIME(CONCAT(bdate, ' ', btime), '01:30:00') > ?
        AND status != 0
    ");
    $stmt->execute([$endStr, $startStr]);
    $bookedTables = $stmt->fetchAll(PDO::FETCH_COLUMN);

    // 找出符合人數的桌子
    if ($guests > 0 && $guests <= 2) {
        $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity = 2");
    } elseif ($guests >= 3 && $guests <= 4) {
        $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity = 4");
    } elseif ($guests >= 5) {
        $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity = 8");
    } else {
        echo json_encode([]);
        exit;
    }
    $stmt->execute();
    $validTables = $stmt->fetchAll(PDO::FETCH_COLUMN);

    // 組合結果：每張桌子都標示 available 狀態
    $result = [];
    foreach ($validTables as $tid) {
        $result[] = [
            'tid' => $tid,
            'available' => !in_array($tid, $bookedTables) // true = 可選, false = 已訂
        ];
    }

    echo json_encode($result);

} else {
    echo json_encode([]);
}
?>
