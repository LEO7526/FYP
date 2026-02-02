<?php
session_start();
$pdo = new PDO("mysql:host=localhost;dbname=projectdb;charset=utf8", "root", "");
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

if (!isset($_COOKIE['cid']) || empty($_COOKIE['cid'])) {
    echo "<script>alert('請先登入才能進行訂位。'); window.location.href='../index/reservation.php';</script>";
    exit;
}

$date = $_POST['date'] ?? '';
$time = $_POST['time'] ?? '';
$guests = $_POST['guests'] ?? '';
$tid = $_POST['tid'] ?? '';
$cid = $_COOKIE['cid'] ?? '';
$purpose = $_POST['purpose'] ?? '';
$remark = $_POST['remark'] ?? '';

if ($date && $time && $guests && $tid && $cid) {
    try {
        $stmt = $pdo->prepare("SELECT cname, ctel FROM customer WHERE cid = ?");
        $stmt->execute([$cid]);
        $customer = $stmt->fetch(PDO::FETCH_ASSOC);

        $bkcname = $customer['cname'];
        $bktel = $customer['ctel'];

        $stmt = $pdo->prepare("INSERT INTO booking (cid, bkcname, bktel, tid, bdate, btime, pnum, purpose, remark, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        $stmt->execute([$cid, $bkcname, $bktel, $tid, $date, $time, $guests, $purpose, $remark, 2]);
        $bid = $pdo->lastInsertId();

        echo "<script>alert('訂位成功！您的訂位編號為：$bid'); window.location.href='home.php';</script>";
    } catch (Exception $e) {
        echo "<script>alert('訂位失敗，請稍後再試。'); window.history.back();</script>";
    }
} else {
    echo "<script>alert('請填寫完整訂位資訊。'); window.history.back();</script>";
}
?>
