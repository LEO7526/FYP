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
    <link rel="stylesheet" href="../CSS/reservation.css">
    <script src="floorplan.js"></script>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
    </style>
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
    <div class="page-header">
        <h1>Booking List</h1>
        <button id="btn-add-booking">+ Add</button>
    </div>

    <!-- New booking form (improved version: includes Canvas floor plan) -->
    <div id="bookingForm" style="display:none;">
        <h3>Reserve a Table (Staff)</h3>
        <form method="post" action="staff_booking.php" class="reservation-form">
            <!-- Name, Phone (Staff input) -->
            <label for="reserve_cname">Name:</label>
            <input id="reserve_cname" name="reserve_cname" type="text" required>

            <label for="tel_num">Telephone number:</label>
            <input id="tel_num" name="tel_num" type="text" required>

            <!-- Date, Time, Guests -->
            <label for="date">Dining Date:</label>
            <input type="date" id="date" name="date" required>

            <label for="time">Dining Time:</label>
            <select id="time" name="time" required>
                <option value="">Select Time</option>
            </select>

            <label for="guests">Number of Guests:</label>
            <select id="guests" name="guests" required>
                <option value="">Select</option>
                <?php for ($i = 1; $i <= 8; $i++) echo "<option value=\"$i\">$i</option>"; ?>
            </select>

            <!-- Purpose, Remark (optional) -->
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

            <!-- Load tables button -->
            <button type="button" id="loadTablesBtn">Load Available Tables</button>

            <!-- Canvas floor plan -->
            <h4>Floor Plan</h4>
            <canvas id="floorCanvas" width="350" height="400"></canvas>

            <!-- Hidden field to store selected table number -->
            <input type="hidden" id="tid" name="tid" required>

            <!-- Submit button -->
            <button type="submit">Confirm Reservation</button>
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

                // Status mapping
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
    // ==================== Global variables and functions ====================
    // Define table coordinates (same as reservation.php)
    const tables = [
        { tid: "T16", x: 20, y: 50, available: false, selected: false },
        { tid: "T15", x: 20, y: 100, available: false, selected: false },
        { tid: "T14", x: 20, y: 150, available: false, selected: false },
        { tid: "T13", x: 20, y: 200, available: false, selected: false },
        { tid: "T12", x: 20, y: 250, available: false, selected: false },
        { tid: "T11", x: 20, y: 300, available: false, selected: false },
        { tid: "T10", x: 100, y: 200, available: false, selected: false },
        { tid: "T9", x: 100, y: 250, available: false, selected: false },
        { tid: "T8", x: 100, y: 300, available: false, selected: false },
        { tid: "T7", x: 180, y: 200, available: false, selected: false },
        { tid: "T6", x: 180, y: 250, available: false, selected: false },
        { tid: "T5", x: 180, y: 300, available: false, selected: false },
        { tid: "T18", x: 100, y: 50, available: false, selected: false },
        { tid: "T17", x: 180, y: 50, available: false, selected: false },
        { tid: "T4", x: 260, y: 50, available: false, selected: false },
        { tid: "T3", x: 260, y: 130, available: false, selected: false },
        { tid: "T2", x: 260, y: 210, available: false, selected: false },
        { tid: "T1", x: 260, y: 290, available: false, selected: false }
    ];

    // Draw canvas
    function drawTables() {
        const canvas = document.getElementById("floorCanvas");
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        tables.forEach(table => {
            if (table.selected) {
                ctx.fillStyle = "#ffeb3b"; // Yellow: selected
            } else {
                ctx.fillStyle = table.available ? "#a5d6a7" : "#ef9a9a"; // Green available / Red unavailable
            }

            ctx.fillRect(table.x, table.y, 60, 40);
            ctx.fillStyle = "#000";
            ctx.font = "bold 14px sans-serif";
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            ctx.fillText(table.tid, table.x + 30, table.y + 20);
        });
    }

    // Generate time slots (11:00 ~ 22:00 every 30 minutes, dining 90 minutes, last end not exceeding 23:00)
    function generateTimeSlots(selectElement) {
        const startHour = 11;
        const stepMinutes = 30;
        const durationMinutes = 90;
        const finalEndTime = 23; // Last end time must not exceed 23:00

        let current = new Date();
        current.setHours(startHour, 0, 0, 0);
        const endLimit = new Date();
        endLimit.setHours(finalEndTime, 0, 0, 0);

        while (true) {
            const end = new Date(current.getTime() + durationMinutes * 60000);
            if (end > endLimit) break;

            const startStr = current.toTimeString().slice(0, 5);
            const endStr = end.toTimeString().slice(0, 5);
            const label = `${startStr} - ${endStr}`;
            const value = startStr + ":00";

            const option = document.createElement("option");
            option.value = value;
            option.textContent = label;
            selectElement.appendChild(option);

            current.setMinutes(current.getMinutes() + stepMinutes);
        }
    }

    $(document).ready(function () {
        // ========== Initialize form related functions ==========
        // Set minimum date to tomorrow
        const dateInput = document.getElementById("date");
        if (dateInput) {
            const today = new Date();
            const tomorrow = new Date(today);
            tomorrow.setDate(today.getDate() + 1);
            const yyyy = tomorrow.getFullYear();
            const mm = String(tomorrow.getMonth() + 1).padStart(2, '0');
            const dd = String(tomorrow.getDate()).padStart(2, '0');
            const tomorrowStr = `${yyyy}-${mm}-${dd}`;
            dateInput.setAttribute("min", tomorrowStr);
            dateInput.value = tomorrowStr;
        }

        // Generate time options
        const timeSelect = document.getElementById("time");
        if (timeSelect) {
            generateTimeSlots(timeSelect);
        }

        // Name and phone input effect (original wrapNameTel logic retained)
        $(document).on('input', '#reserve_cname, #tel_num', function () {
            const $input = $(this);
            if ($input.val().trim().length > 0) {
                $input.addClass('filled');
            } else {
                $input.removeClass('filled');
            }
        });

        // Wrap name and phone into two-column layout (original function)
        function wrapNameTel() {
            if ($('.name-tel-row').length) return;
            $('#reserve_cname').prev('label').addBack().wrapAll('<div class="name-tel-group name-wrap"/>');
            $('#tel_num').prev('label').addBack().wrapAll('<div class="name-tel-group tel-wrap"/>');
            $('.name-wrap,.tel-wrap').wrapAll('<div class="name-tel-row"/>');
        }

        // Click +Add to show/hide form
        $('#btn-add-booking').off('click').on('click', function () {
            const $form = $('#bookingForm');
            if ($form.is(':visible')) {
                $form.hide();
            } else {
                $form.show(0, wrapNameTel);
                $form[0].reset();
                // Reset canvas selection status
                tables.forEach(t => t.selected = false);
                drawTables();
            }
        });

        // ========== Load available tables ==========
        $('#loadTablesBtn').click(function () {
            const date = $('#date').val();
            const time = $('#time').val();
            const guests = $('#guests').val();

            if (!date || !time || !guests) {
                alert("Please select date, time and number of guests first");
                return;
            }

            $.ajax({
                url: "available_tables.php",
                method: "POST",
                data: { date: date, time: time, guests: guests },
                dataType: "json",
                success: function (data) {
                    tables.forEach(t => t.available = false);
                    data.forEach(table => {
                        const target = tables.find(t => t.tid === "T" + table.tid);
                        if (target) target.available = table.available;
                    });
                    drawTables();
                },
                error: function () {
                    alert("Unable to load table information, please try again later.");
                }
            });
        });

        // ========== Click canvas to select table ==========
        $('#floorCanvas').on('click', function (e) {
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            tables.forEach(table => {
                if (x >= table.x && x <= table.x + 60 && y >= table.y && y <= table.y + 40) {
                    if (!table.available) {
                        alert(`Table ${table.tid} is unavailable`);
                        return;
                    }
                    // Deselect others
                    tables.forEach(t => t.selected = false);
                    table.selected = true;
                    // Update hidden field (remove "T" prefix)
                    $('#tid').val(table.tid.replace("T", ""));
                    drawTables();
                }
            });
        });

        // Initial canvas draw (all tables default unavailable, wait for load)
        drawTables();

        // ========== Form submission (AJAX) ==========
        $('#bookingForm form').on('submit', function (e) {
            e.preventDefault();
            const $form = $(this);

            // Ensure tid has a value
            if (!$('#tid').val()) {
                alert("Please select a table first");
                return;
            }

            $.ajax({
                url: 'staff_booking.php',
                method: 'post',
                data: $form.serialize(),
                success: function (res) {
                    if (res.includes('successfully')) {
                        $('#bookingForm').hide();
                        // Show notification
                        $('#toastNotification').text('✅ Reservation created!').fadeIn(300);
                        setTimeout(() => $('#toastNotification').fadeOut(300), 2000);
                        setTimeout(() => location.reload(), 600);
                    } else {
                        alert('Error : ' + res);
                    }
                    $form[0].reset();
                },
                error: function () {
                    alert('Network error, please retry.');
                }
            });
        });

        // ========== Original action button logic (unchanged) ==========
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

        // Toast notification helper function
        function showNotification(msg) {
            const toast = $('#toastNotification');
            toast.text(msg);
            toast.fadeIn(300);
            setTimeout(() => toast.fadeOut(300), 2000);
        }
    });

    // Hamburger menu function (retain original)
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