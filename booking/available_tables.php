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

    $stmt = $pdo->prepare("
        SELECT tid FROM booking 
        WHERE CONCAT(bdate, ' ', btime) < ? 
        AND ADDTIME(CONCAT(bdate, ' ', btime), '01:30:00') > ?
        AND status != 0
    ");
    $stmt->execute([$endStr, $startStr]);
    $bookedTables = $stmt->fetchAll(PDO::FETCH_COLUMN);

    // 根據人數選擇合適的座位容量
    if ($guests > 0 && $guests <= 2) {
        $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity = 2");
        $stmt->execute();
        $validTables = $stmt->fetchAll(PDO::FETCH_COLUMN);
    } elseif ($guests >= 3 && $guests <= 4) {
        $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity = 4");
        $stmt->execute();
        $validTables = $stmt->fetchAll(PDO::FETCH_COLUMN);
    } elseif ($guests >= 5) {
        $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity = 8");
        $stmt->execute();
        $validTables = $stmt->fetchAll(PDO::FETCH_COLUMN);
    } else {
        $validTables = [];
    }


    $availableTables = array_diff($validTables, $bookedTables);

    $result = array_map(fn($tid) => ['tid' => $tid], $availableTables);
    echo json_encode(array_values($result));
} else {
    echo json_encode([]);
}
?>
