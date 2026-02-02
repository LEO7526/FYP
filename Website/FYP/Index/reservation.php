<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>XXX Restaurant</title>
    <link rel="stylesheet" href="../Com.css">
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

                $.ajax({
                    url: "../booking/mybooking.php",
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

    <script> // cancel booking
        $(document).on("submit", ".cancel-form", function (e) {
            e.preventDefault();

            if (confirm("確定取消這筆訂位嗎？")) {
                $.post("mybooking.php", $(this).serialize(), function (response) {
                    $("#bookingResult").html(response);
                });
            }
        });
    </script>

    <script> // edit booking
        $(document).on("submit", ".edit-form", function (e) {
            e.preventDefault();
            $.post("mybooking.php", $(this).serialize(), function (response) {
                $("#bookingResult").html(response);
            });
        });

        $(document).on("click", "#edit-loadTablesBtn", function () {
            const date = $("#edit-date").val();
            const time = $("#edit-time").val();
            const guests = $("#edit-guests").val();
            const tableSelect = $("#edit-tid");

            if (!date || !time || !guests) {
                alert("請先選擇日期、時間和人數");
                return;
            }

            fetch("available_tables.php", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: `date=${date}&time=${time}&guests=${guests}`
            })
                .then(response => response.json())
                .then(data => {
                    tableSelect.html('<option value="">Please choose a table</option>');
                    if (data.length === 0) {
                        tableSelect.append('<option value="">No tables available</option>');
                    } else {
                        data.forEach(table => {
                            const option = $("<option>").val(table.tid).text(`Table ${table.tid}`);
                            tableSelect.append(option);
                        });
                    }
                })
                .catch(error => {
                    console.error("Failed to load tables:", error);
                    tableSelect.html('<option value="">Error loading tables</option>');
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

    <script> //booking
        document.addEventListener("DOMContentLoaded", function () {
            const loadBtn = document.getElementById("loadTablesBtn");
            const dateInput = document.getElementById("date");
            const timeInput = document.getElementById("time");
            const guestsInput = document.getElementById("guests");
            const tableSelect = document.getElementById("tid");


            const today = new Date();
            dateInput.min = today.toISOString().split("T")[0];


            loadBtn.addEventListener("click", function () {
                const date = dateInput.value;
                const time = timeInput.value;
                const guests = guestsInput.value;

                if (!date || !time || !guests) {
                    alert("請先選擇日期、時間和人數");
                    return;
                }

                fetch("../booking/available_tables.php", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: `date=${date}&time=${time}&guests=${guests}`
                })
                    .then(response => response.json())
                    .then(data => {
                        tableSelect.innerHTML = '<option value="">Please choose a table</option>';
                        if (data.length === 0) {
                            tableSelect.innerHTML += '<option value="">No tables available</option>';
                        } else {
                            data.forEach(table => {
                                const option = document.createElement("option");
                                option.value = table.tid;
                                option.textContent = `Table ${table.tid}`;
                                tableSelect.appendChild(option);
                            });
                        }
                    })
                    .catch(error => {
                        console.error("Failed to load tables:", error);
                        tableSelect.innerHTML = '<option value="">Error loading tables</option>';
                    });
            });
        });
    </script>

    <script>
        document.addEventListener("DOMContentLoaded", function () {
            const timeSelect = document.getElementById("time");

            function generateTimeSlots(startHour, endHour, stepMinutes, durationMinutes, finalEndTime) {
                const slots = [];
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

                    slots.push({ label, value });

                    current.setMinutes(current.getMinutes() + stepMinutes);
                }

                slots.forEach(slot => {
                    const option = document.createElement("option");
                    option.value = slot.value;
                    option.textContent = slot.label;
                    timeSelect.appendChild(option);
                });
            }

            generateTimeSlots(11, 22, 30, 90, 23);
        });
    </script>



</head>
<body>
<div class="header">
    <a href="home.php">
        <img src="../Image/logo.png" alt="Logo">
    </a>
    <ul>
        <li><a href="menu.php">MENU</a></li>
        <li><a href="reservation.php">RESERVATION</a></li>
        <li><a href="../order/order.php">ORDER</a></li>
        <li>
            <a href="coupon.php">COUPON</a>
        </li>
    </ul>

    <?php
    if (isset($_COOKIE['user'])) {
        $username = ($_COOKIE['username']);
        echo '<div class="user-bar">
            <a class="username" href="profile.php">Hello,' . $username . '</a>
            <a href="../login/logout.php" class="logout-btn">LOGOUT</a>
          </div>';
    } else {
        echo '<a href="#" class="login-btn" onclick="openLogin()">LOGIN</a>';
    }
    ?>

    <div class="login-popup" id="loginPopup">
        <div class="login-box">
            <span class="close-btn" onclick="closeLogin()">&times;</span>
            <h2>Login</h2>
            <form method="post" action="../login/login.php">
                <label for="useremail">Email:</label>
                <input type="text" id="useremail" name="useremail" required>

                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required>

                <button type="submit">Login</button>
            </form>

            <!-- Sign Up button -->
            <div class="register-link">
                <p>Don't have an account? <a href="../register/register.php">Sign Up</a></p>
            </div>
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
        <form method="post" action="../booking/booking.php" class="reservation-form">
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
            <button type="button" id="loadTablesBtn">Load Available Tables</button>
            <label for="tid">Select Table:</label>
            <select id="tid" name="tid" required>
                <option value="">Please choose a table</option>
            </select>
            <button type="submit">Confirm Reservation</button>
        </form>
    </div>

    <div id="myBookingForm" style="display:none;">
        <h3>Check Your Booking</h3>
        <div id="bookingResult" class="booking-result"></div>
    </div>

</div>
</body>
</html>
