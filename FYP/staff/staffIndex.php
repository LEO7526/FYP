
<?php
require_once '../auth_check.php';
check_staff_auth(); // Check staff authentication status
include '../conn.php'; //
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yummy Restaurant - Staff Dashboard</title>
    <link rel="stylesheet" href="../CSS/staff-index-fix.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/dashboard.css">
    <link rel="stylesheet" href="../CSS/order.css">
    <link rel="stylesheet" href="../CSS/form.css"><link rel="stylesheet" href="../CSS/staff-index-fix.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script> <!-- 添加jQuery -->
</head>

<body>
<header>
    <div class="hamburger-menu" id="hamburgerMenu">
        <span></span>
        <span></span>
        <span></span>
    </div>
    <div class="logo">
        <a href="staffIndex.php">Yummy Restaurant</a>
    </div>
    <nav class="main-nav">
        <a href="MenuManagement.php" class="nav-button insert-items">Menu Management</a>
        <a href="newInventory.php" class="nav-button insert-materials">Inventory</a>
        <a href="bookingList.php" class="nav-button order-list">Reservations</a>
        <a href="salesReport.php" class="nav-button report">Sales Reports</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php" class="profile-btn">Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>

<div class="container">
    <div class="order-section">
        <h2>Pending Reservations</h2>
        <div class="table-container">
            <table id="pendingBookingTable">
                <thead>
                <tr>
                    <th class="booking-id-col">Booking ID</th>
                    <th class="customer-col">Customer</th>
                    <th class="table-col">Table</th>
                    <th class="time-col">Booking Time</th>
                    <th class="guests-col">Guests</th>
                    <th class="purpose-col">Purpose</th>
                    <th class="status-col">Status</th>
                    <th class="actions-col">Actions</th>
                </tr>
                </thead>
                <tbody>
                <?php
                $sql = "SELECT b.bid, b.bkcname, b.bktel, b.tid, b.bdate, b.btime, b.pnum, b.purpose, b.status, b.remark, 
                               b.cid, c.cname as member_name
                        FROM booking b
                        LEFT JOIN customer c ON b.cid = c.cid
                        WHERE b.status = 1
                        ORDER BY b.bdate ASC, b.btime ASC
                        LIMIT 3";
                $result = mysqli_query($conn, $sql);

                if (mysqli_num_rows($result) > 0) {
                    while ($row = mysqli_fetch_assoc($result)) {
                        $hasRemark = !empty($row['remark']);
                        $isMember = !is_null($row['cid']);
                        $customerName = $isMember ? $row['member_name'] : $row['bkcname'];

                        echo "<tr data-bid='{$row['bid']}' data-remark='" . ($hasRemark ? htmlspecialchars($row['remark']) : "") . "' data-status='{$row['status']}'>";

                        // Booking ID cell with member badge
                        echo "<td class='booking-id-cell'>";
                        echo "<div class='booking-id'>{$row['bid']}</div>";
                        if ($isMember) {
                            echo "<div class='member-badge'>Member</div>";
                        }
                        echo "</td>";

                        // Customer information cell
                        echo "<td class='customer-info'>";
                        echo "<div><strong>Name:</strong> {$customerName}</div>";
                        echo "<div><strong>Tel:</strong> {$row['bktel']}</div>";
                        echo "</td>";

                        echo "<td>{$row['tid']}</td>";

                        // Booking time cell with no-wrap date
                        echo "<td class='time-info'>";
                        echo "<div class='date-line'><strong>Date:</strong> {$row['bdate']}</div>";
                        echo "<div><strong>Time:</strong> " . date('H:i', strtotime($row['btime'])) . "</div>";
                        echo "</td>";

                        echo "<td>{$row['pnum']}</td>";
                        echo "<td>{$row['purpose']}</td>";
                        echo "<td class='status-cell' data-status='{$row['status']}'>";
                        echo "<span class='status pending'>Pending</span>";
                        echo "</td>";
                        echo "<td class='actions-cell'>";
                        echo "<div class='action-buttons-vertical'>";
                        echo "<button class='btn-action btn-accept' data-bid='{$row['bid']}' data-action='accept'>Accept</button>";
                        echo "<button class='btn-action btn-reject' data-bid='{$row['bid']}' data-action='reject'>Reject</button>";
                        echo "</div>";
                        echo "</td>";
                        echo "</tr>";

                        // Add remark row if exists
                        if ($hasRemark) {
                            echo "<tr class='remark-row'>";
                            echo "<td colspan='8'>";
                            echo "<div class='remark-content'><strong>Remark:</strong> {$row['remark']}</div>";
                            echo "</td>";
                            echo "</tr>";
                        }
                    }
                } else {
                    echo "<tr><td colspan='8' class='no-data'>No pending reservations</td></tr>";
                }
                ?>
                </tbody>
            </table>
        </div>
        <div class="view-all-link">
            <a href="bookingList.php?filter=pending">View all pending reservations →</a>
        </div>
    </div>

    <div class="materials-section">
        <h2>Inventory Alerts</h2>
        <table>
            <thead>
            <tr>
                <th>Ingredient</th>
                <th>Current Stock</th>
                <th>Reorder Level</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <?php
            $sql = "SELECT m.mid, m.mname, m.unit, m.mqty, m.reorderLevel, 
                           c.category_name
                    FROM materials m
                    LEFT JOIN materials_category c ON m.category_id = c.category_id
                    WHERE m.mqty <= m.reorderLevel OR m.mqty IS NULL
                    ORDER BY (m.mqty / NULLIF(m.reorderLevel, 1)) ASC
                    LIMIT 3";
            $result = mysqli_query($conn, $sql);

            if (mysqli_num_rows($result) > 0) {
                while ($row = mysqli_fetch_assoc($result)) {
                    $currentStock = $row['mqty'];
                    $reorderLevel = $row['reorderLevel'];
                    $unit = $row['unit'] ? $row['unit'] : 'unit';

                    $percentage = $reorderLevel > 0 ? ($currentStock / $reorderLevel) * 100 : 0;

                    if ($currentStock <= 0) {
                        $status = 'Out of Stock';
                        $statusClass = 'insufficient';
                    } elseif ($percentage <= 30) {
                        $status = 'Critical';
                        $statusClass = 'insufficient';
                    } else {
                        $status = 'Low Stock';
                        $statusClass = 'danger';
                    }

                    echo "<tr>";
                    echo "<td>" . htmlspecialchars($row['mname']) . " (" . htmlspecialchars($row['category_name']) . ")</td>";
                    echo "<td>" . $currentStock . " " . $unit . "</td>";
                    echo "<td>" . $reorderLevel . " " . $unit . "</td>";
                    echo "<td><span class='stock-alert {$statusClass}'>{$status}</span></td>";
                    echo "<td><a href='newInventory.php?material_id=" . $row['mid'] . "' class='btn-view'>Reorder</a></td>";
                    echo "</tr>";
                }
            } else {
                echo "<tr><td colspan='5' class='no-data'>All inventory items are well-stocked</td></tr>";
            }
            ?>
            </tbody>
        </table>
    </div>

    <!--<div class="form-section">
        <h2>Daily Sales Summary</h2>
        <div class="report-summary">
            Total Sales: <span class="sales-amount">$8,542.00</span> |
            Orders Served: 124 |
            Top Dish: Dish A
        </div>

        <div class="form-group">
            <h2 class="form-label">Generate Custom Report</h2>
            <select>
                <option>Daily Report</option>
                <option>Weekly Report</option>
                <option>Monthly Report</option>
                <option>Custom Range</option>
            </select>
            <input type="submit" class="btn-submit" value="Generate Report">
        </div>
    </div> -->
</div>

<!-- Confirmation modal -->
<div id="confirmModal" class="modal" style="display: none;">
    <div class="modal-content">
        <h3>Confirm Action</h3>
        <p id="confirmMessage"></p>
        <div class="modal-actions">
            <button id="confirmBtn" class="btn-confirm">Confirm</button>
            <button id="cancelBtn" class="btn-cancel">Cancel</button>
        </div>
    </div>
</div>

<script>
    $(document).ready(function () {
        /* ===== Toast Notification Helper ===== */
        function showNotification(msg) {
            const toast = $('<div class="toast-notification">' + msg + '</div>');
            $('body').append(toast);
            toast.fadeIn(300);
            setTimeout(() => toast.fadeOut(300, () => toast.remove()), 2000);
        }

        /* ===== Unified Action Handler ===== */
        $(document).on('click', '#pendingBookingTable .btn-action', function () {
            const $btn  = $(this);
            const bid   = $btn.data('bid');
            const action = $btn.data('action'); // accept / reject

            /* ---- Execute AJAX request ---- */
            const executeAction = () => {
                $.ajax({
                    url: 'bookingStatus.php',
                    method: 'POST',
                    data: { bid: bid, action: action },
                    success: function () {
                        showNotification('✅ Booking ' + action + 'ed successfully!');
                        setTimeout(() => location.reload(), 500); // 延迟刷新让用户看到通知
                    },
                    error: function () {
                        alert('❌ An error occurred. Please try again.');
                    }
                });
            };

            /* ---- Accept: no confirmation ---- */
            if (action === 'accept') {
                executeAction();
                return;
            }

            /* ---- Reject: ask for confirmation ---- */
            $('#confirmMessage').text('Are you sure you want to reject this booking?');
            $('#confirmModal').show();

            $('#confirmBtn').off('click').on('click', function () {
                $('#confirmModal').hide();
                executeAction();
            });

            $('#cancelBtn').off('click').on('click', () => $('#confirmModal').hide());
        });

        /* ===== Allow close modal by clicking outside ===== */
        $(window).on('click', function (e) {
            if (e.target.id === 'confirmModal') $('#confirmModal').hide();
        });

        /* ===== 30-second auto-refresh (optional) ===== */
        setInterval(() => location.reload(), 30000);
    });
</script>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const hamburger = document.getElementById('hamburgerMenu');
        const mainNav = document.querySelector('.main-nav');
        const userActions = document.querySelector('.user-actions');

        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            mainNav.classList.toggle('active');
            document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
        });

        document.querySelectorAll('.nav-button').forEach(button => {
            button.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        document.querySelectorAll('.user-actions a').forEach(link => {
            link.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        document.addEventListener('click', function(e) {
            if (!hamburger.contains(e.target) && !mainNav.contains(e.target) && !userActions.contains(e.target)) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && mainNav.classList.contains('active')) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
    });
</script>
</body>
</html>