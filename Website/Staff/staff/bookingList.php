<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Order List</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/order.css">
</head>
<body>
<header>
    <div class="logo">
        <a href="staffIndex.php">Smile & Sunshine Toy</a>
    </div>

    <nav class="main-nav">
        <a href="InsertItems.php" class="nav-button insert-items">Insert Items</a>
        <a href="InsertMaterials.php" class="nav-button insert-materials">Insert Materials</a>
        <a href="bookingList.php" class="nav-button order-list">Order List</a>
        <a href="Report.php" class="nav-button report">Report</a>
        <a href="Delete.php" class="nav-button delete">Delete</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php">Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>
<div class="back-link">
    <a href="staffIndex.php" class="back-link-text"> &#60; Back</a>
</div>
<div class="container">
    <h1>Booking List</h1>

    <table id="ordersTable">
        <thead>
        <tr>
            <th>Booking ID</th>
            <th>Number of people</th>
            <th>Contact Name</th>
            <th>Booking Date</th>
            <th>Booking Status</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <?php
        $sql = "SELECT o.oid, o.pid, c.cname, o.odeliverdate, o.ostatus 
                        FROM orders o
                        JOIN customer c ON o.cid = c.cid";
        $result = mysqli_query($conn, $sql);

        $statusMap = [
            0 => 'Rejected',
            1 => 'To be viewed',
            2 => 'To be accepted',
            3 => 'Approved'
        ];

        while ($row = mysqli_fetch_assoc($result)) {
            $status = isset($statusMap[$row['ostatus']]) ?
                $statusMap[$row['ostatus']] :
                'Unknown';

            $deliveryDate = $row['odeliverdate'] ?
                date('Y-m-d', strtotime($row['odeliverdate'])) :
                'N/A';

            echo "<tr>
                            <td>{$row['oid']}</td>
                            <td>{$row['pid']}</td>
                            <td>{$row['cname']}</td>
                            <td>{$deliveryDate}</td>
                            <td>{$status}</td>
                            <td>
                                <a href=\"StaffOrderDetails.php?oid={$row['oid']}\" class=\"btn-view\">View Details</a>
                            </td>
                        </tr>";
        }
        ?>
        </tbody>
    </table>
</div>
</body>
</html>