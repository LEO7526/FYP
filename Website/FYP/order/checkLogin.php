<?php
session_start();

$item_ids = $_POST['item_ids'] ?? [];
$quantities = $_POST['quantities'] ?? [];
$spice_levels = $_POST['spice_levels'] ?? [];
$remarks = $_POST['remarks'] ?? [];

$_SESSION['order'] = [
    'item_ids' => $item_ids,
    'quantities' => $quantities,
    'spice_levels' => $spice_levels,
    'remarks' => $remarks
];

if (!isset($_COOKIE['cid'])) {
    ?>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Order - XXX Restaurant</title>
        <!-- 引入共用 CSS -->
        <link rel="stylesheet" href="../Com.css">
        <!-- 內嵌一些簡單樣式 -->
        <style>
            body {
                font-family: Arial, sans-serif;
                background-color: #f9f9f9;
                text-align: center;
                padding: 50px;
            }
            h2 {
                color: #e74c3c;
            }
            p {
                margin: 10px 0;
            }
            a {
                display: inline-block;
                margin-top: 10px;
                padding: 8px 16px;
                background: #3498db;
                color: #fff;
                text-decoration: none;
                border-radius: 6px;
            }
            a:hover {
                background: #2980b9;
            }
        </style>
    </head>
    <body>
    <h2>You are not logged in</h2>
    <p>Please <a href="../login/login2.php">login</a> to use coupons and earn points.</p>
    <p><a href="../order/finalize_order.php">Order without account</a></p>
    </body>
    </html>
    <?php
    exit;
} else {
    header("Location: ../order/select_coupon.php");
}
?>
