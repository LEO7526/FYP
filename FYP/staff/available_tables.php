<?php
header('Content-Type: application/json');
$pdo = new PDO("mysql:host=localhost;dbname=projectdb;charset=utf8", "root", "");

$date = $_POST['date'] ?? '';
$time = $_POST['time'] ?? '';
$guests = is_numeric($_POST['guests'] ?? '') ? (int)$_POST['guests'] : 0;

if ($date && $time && $guests) {
    $stmt = $pdo->prepare("SELECT tid FROM booking WHERE bdate = ? AND btime BETWEEN DATE_SUB(?, INTERVAL 90 MINUTE) AND DATE_ADD(?, INTERVAL 90 MINUTE)");
    $stmt->execute([$date, $time, $time]);
    $booked = $stmt->fetchAll(PDO::FETCH_COLUMN);

    if (!empty($booked)) {
        $placeholders = implode(',', array_fill(0, count($booked), '?'));
        $sql = "SELECT tid FROM seatingchart WHERE capacity >= ? AND tid NOT IN ($placeholders)";
        $stmt = $pdo->prepare($sql);
        $stmt->execute(array_merge([$guests], $booked));
    } else {
        $stmt = $pdo->prepare("SELECT tid FROM seatingchart WHERE capacity >= ?");
        $stmt->execute([$guests]);
    }

    $available = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode($available);
} else {
    echo json_encode([]);
}
?>
