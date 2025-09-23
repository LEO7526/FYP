<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>XXX Restaurant</title>
    <link rel="stylesheet" href="Com.css">
    <link rel="stylesheet" href="reservation.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function () {
            $("#bookingBtn").click(function () {
                $("#bookingForm").show();
                $("#myBookingForm").hide();
            });

            $("#myBookingBtn").click(function () {
                $("#myBookingForm").show();
                $("#bookingForm").hide();
            });
        });
    </script>

    <script>
        $(document).ready(function () {
            $("#myBookingBtn").click(function () {
                $("#myBookingForm").show();
                $("#bookingForm").hide();

                $.ajax({
                    url: "mybooking.php",
                    method: "POST",
                    success: function (response) {
                        $("#bookingResult").html(response);
                    },
                    error: function () {
                        $("#bookingResult").html("<p>查詢失敗，請稍後再試。</p>");
                    }
                });
            });
        });
    </script>

    <script>
        function openLogin() {
            document.getElementById("loginPopup").style.display = "flex";
        }

        function closeLogin() {
            document.getElementById("loginPopup").style.display = "none";
        }
    </script>

    <script>
        document.addEventListener("DOMContentLoaded", function () {
            const dateInput = document.getElementById("date");
            const timeInput = document.getElementById("time");
            const guestsInput = document.getElementById("guests");
            const tableSelect = document.getElementById("tid");

            function fetchTables() {
                const date = dateInput.value;
                const time = timeInput.value;
                const guests = guestsInput.value;

                if (date && time && guests) {
                    fetch("available_tables.php", {
                        method: "POST",
                        headers: { "Content-Type": "application/x-www-form-urlencoded" },
                        body: `date=${date}&time=${time}&guests=${guests}`
                    })
                        .then(response => response.json())
                        .then(data => {
                            tableSelect.innerHTML = '<option value="">Please choose a table</option>';
                            data.forEach(table => {
                                const option = document.createElement("option");
                                option.value = table.tid;
                                option.textContent = `Table ${table.tid}`;
                                tableSelect.appendChild(option);
                            });
                        });
                } else {
                    tableSelect.innerHTML = '<option value="">Please choose a table</option>';
                }
            }

            dateInput.addEventListener("change", fetchTables);
            timeInput.addEventListener("change", fetchTables);
            guestsInput.addEventListener("change", fetchTables);
        });
    </script>


    <script>
        function showBookingForm() {
            document.getElementById("bookingForm").style.display = "block";
        }

        function checkMyBooking() {
            window.location.href = "mybooking.php";
        }
    </script>

    <script>
        $(document).on("submit", ".edit-form", function (e) {
            e.preventDefault();
            $.post("mybooking.php", $(this).serialize(), function (response) {
                $("#bookingResult").html(response);
            });
        });

        $(document).on("submit", ".cancel-form", function (e) {
            e.preventDefault();
            if (confirm("確定取消這筆訂位嗎？")) {
                $.post("mybooking.php", $(this).serialize(), function (response) {
                    $("#bookingResult").html(response);
                });
            }
        });

        $(document).on("submit", ".update-form", function (e) {
            e.preventDefault();
            $.post("mybooking.php", $(this).serialize(), function (response) {
                $("#bookingResult").html(response);
            });
        });
    </script>

    <script>

        $(document).on("change", "#bookingResult input[name='bdate'], #bookingResult input[name='btime'], #bookingResult input[name='pnum']", function () {
            const bdate = $("#bookingResult input[name='bdate']").val();
            const btime = $("#bookingResult input[name='btime']").val();
            const pnum = $("#bookingResult input[name='pnum']").val();
            const tableSelect = $("#bookingResult select[name='tid']");
            const currentTid = tableSelect.val();

            if (bdate && btime && pnum) {
                $.post("mybooking.php", {
                    action: "fetch_tables",
                    bdate: bdate,
                    btime: btime,
                    pnum: pnum
                }, function (data) {
                    tableSelect.empty().append('<option value="">請選擇座位</option>');
                    if (data.length === 0) {
                        tableSelect.append('<option value="">目前無可用座位</option>');
                    } else {
                        data.forEach(table => {
                            const selected = table.tid == currentTid ? "selected" : "";
                            tableSelect.append(`<option value="${table.tid}" ${selected}>Table ${table.tid}</option>`);
                        });
                    }
                }, "json");
            }
        });
    </script>



</head>
<body>
<div class="header">
    <a href="home.php">
        <img src="Image/logo.png" alt="Logo">
    </a>
    <ul>
        <li><a href="menu.php">MENU</a></li>
        <li><a href="reservation.php">RESERVATION</a></li>
        <li><a href="order.php">ORDER</a></li>
    </ul>

    <?php
    if (isset($_COOKIE['user'])) {
        echo '<a href="profile.php" class="avatar-link">
    <img src="Image/head.jpg" alt="Member Avatar" class="avatar">
    </a>';
    } else {
    echo '<a href="#" class="login-btn" onclick="openLogin()">LOGIN</a>';
    }
    ?>

    <div class="login-popup" id="loginPopup">
        <div class="login-box">
            <span class="close-btn" onclick="closeLogin()">&times;</span>
            <h2>Login</h2>
            <form method="post" action="login.php">
                <label for="username">Username:</label>
                <input type="text" id="username" name="username" required>

                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required>

                <button type="submit">Enter</button>
            </form>
        </div>
    </div>
</div>

<div class="reservation-section">
    <h2>Welcome to XXX Restaurant</h2>
    <div class="action-buttons">
        <button id="bookingBtn">Booking Now</button>
        <button id="myBookingBtn">My Booking</button>
    </div>

    <div id="bookingForm" style="display:none;">
        <h3>Reserve a Table</h3>
        <form method="post" action="booking.php" class="reservation-form">
            <label for="date">Dining Date:</label>
            <input type="date" id="date" name="date" required>

            <label for="time">Dining Time:</label>
            <input type="time" id="time" name="time" required>

            <label for="guests">Number of Guests:</label>
            <select id="guests" name="guests" required>
                <option value="">Select</option>
                <?php for ($i = 1; $i <= 8; $i++) echo "<option value=\"$i\">$i</option>"; ?>
            </select>

            <label for="tid">Select Table:</label>
            <select id="tid" name="tid" required>
                <option value="">Please choose a table</option>
            </select>
            <label for="purpose">Purpose of booking</label>
            <select id="purpose" name="purpose">
                <option value="Date Night">Date Night</option>
                <option value="Family Dinner">Family Dinner</option>
                <option value="Business Meeting">Business Meeting</option>
                <option value="Lunch Meeting">Lunch Meeting</option>
                <option value="Birthday Celebration">Birthday Celebration</option>

            </select>
            <label for="remark">Remark:</label>
            <input id="remark" name="remark" type="text">

            <button type="submit">Confirm Reservation</button>
        </form>
    </div>


    <div id="myBookingForm" style="display:none;">
        <h3>Check Your Booking</h3>
        <div id="bookingResult" class="booking-result"></div>
    </div>


</div>




    <div></div>
</body>
</html>
