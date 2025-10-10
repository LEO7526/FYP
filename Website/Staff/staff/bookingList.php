<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

// Get filter parameter
$filter = isset($_GET['filter']) ? $_GET['filter'] : 'all';
$page = isset($_GET['page']) ? $_GET['page'] : 'staffIndex.php';

// Build SQL WHERE conditions
$whereConditions = [];
if ($filter === 'pending') {
    $whereConditions[] = "b.status = 1";
} elseif ($filter === 'confirmed') {
    $whereConditions[] = "b.status = 2";
} elseif ($filter === 'history') {
    $whereConditions[] = "b.status = 3";
} elseif ($filter === 'cancelled') {
    $whereConditions[] = "b.status = 0";
} else {
    // Default show all (except cancelled)
    $whereConditions[] = "b.status != 0";
}

$whereSql = implode(" AND ", $whereConditions);

$sql = "SELECT b.bid, b.bkcname, b.bktel, b.tid, b.bdate, b.btime, b.pnum, b.purpose, b.status, b.remark, 
               b.cid, c.cname as member_name
        FROM booking b
        LEFT JOIN customer c ON b.cid = c.cid
        WHERE $whereSql
        ORDER BY b.bdate ASC, b.btime ASC";
$result = mysqli_query($conn, $sql);

$bookings = [];
while ($row = mysqli_fetch_assoc($result)) {
    $bookings[] = $row;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Booking List</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/order.css">
    <link rel="stylesheet" href="../CSS/bookingList.css">
    <link rel="stylesheet" href="../CSS/bookingList-responsive.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
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
        <a href="MenuManagement.html" class="nav-button insert-items">Menu Management</a>
        <a href="Inventory.php" class="nav-button insert-materials">Inventory</a>
        <a href="bookingList.php" class="nav-button order-list">Reservations</a>
        <a href="SalesReport.php" class="nav-button report">Sales Reports</a>
        <a href="PurchaseReturn.php" class="nav-button delete">Purchase & Return</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php" class="profile-btn">Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>
<div class="back-link">
    <a href="<?= htmlspecialchars($page) ?>" class="back-link-text"> &#60; Back</a>
</div>
<div class="container">
    <div class="page-header">
        <h1>Booking List</h1>
        <button id="btn-add-booking">+ Add</button>

    </div>
    <div id="bookingForm" style="display:none;">
        <h3>Reserve a Table</h3>
        <form method="post" action="staff_booking.php" class="reservation-form">
            <label for="name">Name:</label><input id="reserve_cname" name="reserve_cname" type="text">
            <label for="tel_num"> Telephone number:</label><input id="tel_num" name="tel_num" type="text">
            <label for="date">Dining Date:</label>
            <input type="date" id="date" name="date" required>

            <label for="time">Dining Time:</label>
            <input type="time" id="time" name="time" required>
            <br>
            <label for="guests">Number of Guests:</label>
            <select id="guests" name="guests" required>
                <option value="">-- Select --</option>
                <?php for ($i = 1; $i <= 8; $i++) echo "<option value=\"$i\">$i</option>"; ?>
            </select>

            <label for="tid">Select Table:</label>
            <select id="tid" name="tid" required>
                <option value="">-- Please choose a table --</option>
            </select>
            <label for="purpose">Purpose of booking</label>
            <select id="purpose" name="purpose">
                <option value="Null">-- Please select the purpose --</option>
                <option value="Date Night">Date Night</option>
                <option value="Family Dinner">Family Dinner</option>
                <option value="Business Meeting">Business Meeting</option>
                <option value="Lunch Meeting">Lunch Meeting</option>
                <option value="Birthday Celebration">Birthday Celebration</option>

            </select>
            <label for="remark">Remark:</label>
            <input id="remark" name="remark" type="text">

            <button id="btn_Confirm_Reservation" type="submit">Confirm Reservation</button>
        </form>
    </div>

    <!-- Filter buttons -->
    <div class="filter-buttons">
        <a href="?filter=all&page=<?= htmlspecialchars($page) ?>" class="filter-btn <?= $filter === 'all' ? 'active' : '' ?>">All</a>
        <a href="?filter=pending&page=<?= htmlspecialchars($page) ?>" class="filter-btn <?= $filter === 'pending' ? 'active' : '' ?>">Pending</a>
        <a href="?filter=confirmed&page=<?= htmlspecialchars($page) ?>" class="filter-btn <?= $filter === 'confirmed' ? 'active' : '' ?>">Confirmed</a>
        <a href="?filter=history&page=<?= htmlspecialchars($page) ?>" class="filter-btn <?= $filter === 'history' ? 'active' : '' ?>">History</a>
        <a href="?filter=cancelled&page=<?= htmlspecialchars($page) ?>" class="filter-btn <?= $filter === 'cancelled' ? 'active' : '' ?>">Cancelled</a>
    </div>

    <!-- Desktop table view -->
    <div class="table-container">
        <table id="bookingTable">
            <thead>
            <tr>
                <th class="booking-id-col">Booking ID</th>
                <th class="customer-col">Customer</th>
                <th class="table-col">Table</th>
                <th class="time-col">Booking Time</th>
                <th class="guests-col">Guests</th>
                <th class="purpose-col">Purpose</th>
                <th class="status-col">
                    <div class="column-header">Status</div>
                </th>
                <th class="actions-col">Actions</th>
            </tr>
            </thead>
            <tbody>
            <?php
            foreach ($bookings as $row) {
                $hasRemark = !empty($row['remark']);
                $isMember = !is_null($row['cid']);
                $customerName = $isMember ? $row['member_name'] : $row['bkcname'];

                // 状态映射
                $statusMap = [
                    0 => ['text' => 'Cancelled', 'class' => 'cancelled'],
                    1 => ['text' => 'Pending', 'class' => 'pending'],
                    2 => ['text' => 'Confirmed', 'class' => 'confirmed'],
                    3 => ['text' => 'Completed', 'class' => 'completed']
                ];
                $statusInfo = $statusMap[$row['status']];

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
                echo "<div class='status {$statusInfo['class']}'>{$statusInfo['text']}</div>";
                echo "</td>";
                echo "<td class='actions-cell'>";
                // Actions will be set by jQuery based on status
                echo "</td>";
                echo "</tr>";

                // Append remark row if any
                if ($hasRemark) {
                    echo "<tr class='remark-row'>
                              <td colspan='8'><div class='remark-content'><strong>Remark:</strong> {$row['remark']}</div></td>
                            </tr>";
                }
            }
            ?>
            </tbody>
        </table>
    </div>

    <!-- Mobile card view -->
    <div class="booking-cards-container">
        <?php
        foreach ($bookings as $row) {
            $hasRemark = !empty($row['remark']);
            $isMember = !is_null($row['cid']);
            $customerName = $isMember ? $row['member_name'] : $row['bkcname'];

            $statusMap = [
                0 => ['text' => 'Cancelled', 'class' => 'cancelled'],
                1 => ['text' => 'Pending', 'class' => 'pending'],
                2 => ['text' => 'Confirmed', 'class' => 'confirmed'],
                3 => ['text' => 'Completed', 'class' => 'completed']
            ];
            $statusInfo = $statusMap[$row['status']];

            echo "<div class='booking-card {$statusInfo['class']}' data-bid='{$row['bid']}' data-status='{$row['status']}'>";
            echo "<div class='card-header'>";
            echo "<div class='card-title'>";
            echo "Booking ID: {$row['bid']}";
            if ($isMember) {
                echo "<span class='member-badge-card'>Member</span>";
            }
            echo "</div>";
            echo "<div class='card-status {$statusInfo['class']}'>{$statusInfo['text']}</div>";
            echo "</div>";

            echo "<div class='card-info'>";
            echo "<div class='info-item'>";
            echo "<span class='info-label'>Customer:</span>";
            echo "<span class='info-value'>{$customerName}</span>";
            echo "</div>";

            echo "<div class='info-item'>";
            echo "<span class='info-label'>Tel:</span>";
            echo "<span class='info-value'>{$row['bktel']}</span>";
            echo "</div>";

            echo "<div class='info-item'>";
            echo "<span class='info-label'>Table:</span>";
            echo "<span class='info-value'>{$row['tid']}</span>";
            echo "</div>";

            echo "<div class='info-item'>";
            echo "<span class='info-label'>Date:</span>";
            echo "<span class='info-value'>{$row['bdate']}</span>";
            echo "</div>";

            echo "<div class='info-item'>";
            echo "<span class='info-label'>Time:</span>";
            echo "<span class='info-value'>" . date('H:i', strtotime($row['btime'])) . "</span>";
            echo "</div>";

            echo "<div class='info-item'>";
            echo "<span class='info-label'>Guests:</span>";
            echo "<span class='info-value'>{$row['pnum']}</span>";
            echo "</div>";

            echo "<div class='info-item purpose'>";
            echo "<span class='info-label'>Purpose:</span>";
            echo "<span class='info-value'>{$row['purpose']}</span>";
            echo "</div>";
            echo "</div>";

            if ($hasRemark) {
                echo "<div class='card-remark'>";
                echo "<strong>Remark:</strong> {$row['remark']}";
                echo "</div>";
            }

            echo "<div class='card-actions'>";
            if ($row['status'] == 1) { // Pending
                echo "<button class='card-btn btn-action accept' data-bid='{$row['bid']}' data-action='accept'>Accept</button>";
                echo "<button class='card-btn btn-action reject' data-bid='{$row['bid']}' data-action='reject'>Reject</button>";
            } else if ($row['status'] == 2) { // Confirmed
                echo "<button class='card-btn btn-action cancel' data-bid='{$row['bid']}' data-action='cancel'>Cancel</button>";
            } else { // Cancelled / Completed
                echo "<button class='card-btn locked' disabled>Locked</button>";
            }
            echo "</div>";
            echo "</div>";
        }
        ?>
    </div>
</div>

<!-- Toast Notification -->
<div id="toastNotification" class="toast-notification" style="display: none;"></div>

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
    function showBookingForm() {
        document.getElementById("bookingForm").style.display = "block";
    }

    $(document).ready(function () {

        $(document).on('input', '#reserve_cname, #tel_num', function () {
            const $input = $(this);
            if ($input.val().trim().length > 0) {
                $input.addClass('filled');
            } else {
                $input.removeClass('filled');
            }
        });

        const dateInput = document.getElementById("date");
        const timeInput = document.getElementById("time");
        const guestsInput = document.getElementById("guests");
        const tableSelect = document.getElementById("tid");



        function wrapNameTel() {
            if ($('.name-tel-row').length) return;

            $('#reserve_cname').prev('label').addBack().wrapAll('<div class="name-tel-group name-wrap"/>');
            $('#tel_num').prev('label').addBack().wrapAll('<div class="name-tel-group tel-wrap"/>');
            $('.name-wrap,.tel-wrap').wrapAll('<div class="name-tel-row"/>');
        }

        $('#btn-add-booking').off('click').on('click', function () {
            const $form = $('#bookingForm');
            if ($form.is(':visible')) {
                $form.hide();
            } else {
                $form.show(0, wrapNameTel);
                $form[0].reset();
            }
        });
        function fetchTables() {
            const date = dateInput.value;
            const time = timeInput.value;
            const guests = guestsInput.value;

            if (date && time && guests) {
                $.ajax({
                    url: "available_tables.php",
                    method: "POST",
                    data: {
                        date: date,
                        time: time,
                        guests: guests
                    },
                    success: function(data) {
                        tableSelect.innerHTML = '<option value="">Please choose a table</option>';
                        data.forEach(function(table) {
                            const option = document.createElement("option");
                            option.value = table.tid;
                            option.textContent = `Table ${table.tid}`;
                            tableSelect.appendChild(option);
                        });
                    },
                    error: function() {
                        console.error("Error fetching available tables");
                        tableSelect.innerHTML = '<option value="">Error loading tables</option>';
                    }
                });
            } else {
                tableSelect.innerHTML = '<option value="">Please choose a table</option>';
            }
        }

        if (dateInput && timeInput && guestsInput) {
            dateInput.addEventListener("change", fetchTables);
            timeInput.addEventListener("change", fetchTables);
            guestsInput.addEventListener("change", fetchTables);

            if (dateInput.value && timeInput.value && guestsInput.value) {
                fetchTables();
            }
        }

        /* ===== Toast Notification Helper ===== */
        function showNotification(msg) {
            const toast = $('#toastNotification');
            toast.text(msg);
            toast.fadeIn(300);
            setTimeout(() => toast.fadeOut(300), 2000);
        }

        /* ===== Status Text & Style Mapping ===== */
        const statusMap = {
            0: { text: 'Cancelled',  class: 'cancelled' },
            1: { text: 'Pending',    class: 'pending' },
            2: { text: 'Confirmed',  class: 'confirmed' },
            3: { text: 'Completed',  class: 'completed' }
        };

        /* ===== Render Action Buttons ===== */
        function renderActionButtons($element, status, bid) {
            let actionsHtml = '';
            if (status === 1) { // Pending
                actionsHtml = `
            <div class="action-buttons-vertical">
                <button class="btn-action btn-accept" data-bid="${bid}" data-action="accept">Accept</button>
                <button class="btn-action btn-reject" data-bid="${bid}" data-action="reject">Reject</button>
            </div>`;
            } else if (status === 2) { // Confirmed
                actionsHtml = `<button class="btn-action btn-cancel" data-bid="${bid}" data-action="cancel">Cancel</button>`;
            } else { // Cancelled / Completed
                actionsHtml = `<button class="btn-action btn-locked" disabled>Locked</button>`;
            }
            $element.html(actionsHtml);
        }

        // Initialize table actions
        $('#bookingTable tbody tr').each(function () {
            const $row = $(this);
            const status = parseInt($row.data('status'));
            const bid = $row.data('bid');
            renderActionButtons($row.find('.actions-cell'), status, bid);
        });

        // Initialize card actions
        $('.booking-card').each(function () {
            const $card = $(this);
            const status = parseInt($card.data('status'));
            const bid = $card.data('bid');

            // Cards already have buttons, so we just need to add the appropriate classes
            $card.find('.card-btn').addClass('btn-action');
        });

        /* ===== Unified Action Handler ===== */
        $(document).on('click', '.btn-action', function () {
            const $btn   = $(this);
            const bid    = $btn.data('bid');
            const action = $btn.data('action'); // accept / reject / cancel

            if (action === 'locked') return;

            /* ---- Execute AJAX request ---- */
            const executeAction = () => {
                $.ajax({
                    url: 'bookingStatus.php',
                    method: 'POST',
                    data: { bid: bid, action: action },
                    success: function () {
                        showNotification('✅ Booking ' + action + 'ed successfully!');
                        setTimeout(() => location.reload(), 500);
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

            /* ---- Reject / Cancel: ask for confirmation ---- */
            const msg = action === 'reject'
                ? 'Are you sure you want to reject this booking?'
                : 'Are you sure you want to cancel this booking?';

            $('#confirmMessage').text(msg);
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
        $('#bookingForm form').on('submit', function (e) {
            e.preventDefault();
            const $form = $(this);

            $.ajax({
                url: 'staff_booking.php',
                method: 'post',
                data: $form.serialize(),
                success: function (res) {
                    if (res.includes('successfully')) {
                        $('#bookingForm').hide();
                        showNotification('✅ Reservation created!');
                        setTimeout(() => location.reload(), 600);
                    } else {
                        alert('❌ ' + res);
                    }
                    $form[0].reset();
                },
                error: function () {
                    alert('❌ Network error, please retry.');
                }
            });
        });
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
            // 防止背景滚动
            document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
        });

        // 点击菜单项后关闭菜单
        document.querySelectorAll('.nav-button').forEach(button => {
            button.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        // 点击用户操作链接后关闭菜单
        document.querySelectorAll('.user-actions a').forEach(link => {
            link.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        // 点击菜单外部区域关闭菜单
        document.addEventListener('click', function(e) {
            if (!hamburger.contains(e.target) && !mainNav.contains(e.target) && !userActions.contains(e.target)) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });

        // ESC键关闭菜单
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