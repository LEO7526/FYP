<?php
require_once '../auth_check.php';
check_customer_auth();

include '../conn.php';

// Status mapping
$statusMap = [
    0 => 'Rejected',
    1 => 'Pending',
    2 => 'Processing',
    3 => 'Completed'
];
?>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>My Orders</title>
        <link rel="stylesheet" href="../CSS/header.css">
        <link rel="stylesheet" href="../CSS/common.css">
        <link rel="stylesheet" href="../CSS/order.css">
    </head>
    <body>
    <header>
        <div class="logo">
            <a href="Index.php">Smile &amp; Sunshine Toy</a>
        </div>
        <div class="user-actions">
            <a href="Index.php">Home</a>
            <a href="MyOrder.php">My Orders</a>
            <a href="Profile.php">My Profile</a>
            <a href="../logout.php" class="logout-btn">Log out</a>
        </div>
    </header>

    <div class="container">
        <h1>My Orders</h1>
        <table>
            <thead>
            <tr>
                <th>Order ID</th>
                <th>Date</th>
                <th>Total</th>
                <th>Status</th>
                <th>Details</th>
            </tr>
            </thead>
            <tbody>
            <?php
            $cid = $_SESSION['customer']['cid'];
            $sql = "SELECT o.oid, o.odate, o.ocost, o.ostatus, p.pname 
                    FROM orders o
                    JOIN product p ON o.pid = p.pid
                    WHERE o.cid = $cid
                    ORDER BY o.odate DESC";
            $result = mysqli_query($conn, $sql);

            while ($row = mysqli_fetch_assoc($result)) {
                $status = isset($statusMap[$row['ostatus']]) ? $statusMap[$row['ostatus']] : 'Unknown';
                echo "<tr>
                        <td>{$row['oid']}</td>
                        <td>" . date('Y-m-d', strtotime($row['odate'])) . "</td>
                        <td>$" . number_format($row['ocost'], 2) . "</td>
                        <td class=\"status\">$status</td>
                        <td>
                            <a href=\"OrderDetails_cus.php?oid={$row['oid']}\" class=\"btn-view\">View</a>
                        </td>
                    </tr>";
            }
            ?>
            </tbody>
        </table>
    </div>
    </body>
    </html>
<?php mysqli_close($conn); ?>