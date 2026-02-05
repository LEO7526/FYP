<?php
session_start();

$orderType = $_POST["order_type"];
var_dump($orderType);
$_SESSION['order_type'] = $orderType;
if (isset($_POST['payment'])) {
    if ($_POST['payment'] == 0) {
        // 付款成功 → 轉跳到 order_paid.php
        header("Location: order_paid.php");
        exit;
    } else {
        // 選擇其他付款方式 → 轉跳到 insertOrder.php
        header("Location: insertOrder.php");
        exit;
    }
} else {
    // 如果沒有收到 payment 參數，回到首頁或錯誤提示
    header("Location: index.php");
    exit;
}
