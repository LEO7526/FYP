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
                $.post("../booking/myBooking.php", $(this).serialize(), function (response) {
                    $("#bookingResult").html(response);
                });
            }
        });
    </script>

    <script> // edit booking
        // 1. 按下 Edit → 載入修改表單
        $(document).on("submit", ".edit-form", function (e) {
            e.preventDefault();
            $.post("../booking/myBooking.php", $(this).serialize(), function (response) {
                $("#bookingResult").html(response); // 在這裡顯示修改表單
            });
        });

        // 2. 按下 Confirm Modification → 更新訂單
        $(document).on("submit", ".update-form", function (e) {
            e.preventDefault();
            if (!$("#edit-tid").val()) {
                alert("請先選擇座位");
                return;
            }
            console.log($(this).serialize()); // Debug: 看送出的資料
            $.post("../booking/myBooking.php", $(this).serialize(), function (response) {
                $("#bookingResult").html(response);
            });
        });



        $(document).on("click", ".table.available", function () {
            $(".table").removeClass("selected");
            $(this).addClass("selected");
            $("#edit-tid").val($(this).data("tid")); // ✅ 更新 hidden input
        });


        // 修改訂位：載入座位圖
        $(document).on("click", "#edit-loadTablesBtn", function () {
            const date = $("#edit-date").val();
            const time = $("#edit-time").val();
            const guests = $("#edit-guests").val();
            const tableMap = $("#edit-table-map");

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
                    tableMap.html(""); // 清空

                    if (data.length === 0) {
                        tableMap.html("<p>No tables available</p>");
                    } else {
                        data.forEach(table => {
                            const div = $("<div>")
                                .addClass("table")
                                .attr("data-tid", table.tid) // 加上 data-tid
                                .text(`T${table.tid}`);


                            if (table.available) {
                                div.addClass("available");
                                div.on("click", function () {
                                    $(".table").removeClass("selected");
                                    div.addClass("selected");
                                    $("#edit-tid").val(table.tid);
                                    console.log("選擇座位:", table.tid); // Debug
                                });

                            } else {
                                div.addClass("unavailable");
                            }
                            tableMap.append(div);
                        });
                    }
                })
                .catch(error => {
                    console.error("Failed to load tables:", error);
                    tableMap.html("<p>Error loading tables</p>");
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
            const canvas = document.getElementById("floorCanvas");
            const ctx = canvas.getContext("2d");

            // 定義座位
            const tables = [
                { tid: "T16", x: 20, y: 50, available: false, selected: false },
                { tid: "T15", x: 20, y: 100, available: false, selected: false  },
                { tid: "T14", x: 20, y: 150, available: false, selected: false  },
                { tid: "T13", x: 20, y: 200, available: false, selected: false  },
                { tid: "T12", x: 20, y: 250, available: false, selected: false  },
                { tid: "T11", x: 20, y: 300, available: false, selected: false  },
                { tid: "T10", x: 100, y: 200, available: false, selected: false  },
                { tid: "T9", x: 100, y: 250, available: false, selected: false  },
                { tid: "T8", x: 100, y: 300, available: false, selected: false  },
                { tid: "T7", x: 180, y: 200, available: false, selected: false  },
                { tid: "T6", x: 180, y: 250, available: false, selected: false  },
                { tid: "T5", x: 180, y: 300, available: false, selected: false  },
                { tid: "T18", x: 100, y: 50, available: false, selected: false  },
                { tid: "T17", x: 180, y: 50, available: false, selected: false  },
                { tid: "T4", x: 260, y: 50, available: false, selected: false  },
                { tid: "T3", x: 260, y: 130, available: false, selected: false  },
                { tid: "T2", x: 260, y: 210, available: false, selected: false  },
                { tid: "T1", x: 260, y: 290, available: false, selected: false  }
            ];

            // 繪製座位
            function drawTables() {
                ctx.clearRect(0, 0, canvas.width, canvas.height);

                tables.forEach(table => {
                    if (table.selected) {
                        ctx.fillStyle = "#ffeb3b"; // 黃色：已選中
                    } else {
                        ctx.fillStyle = table.available ? "#a5d6a7" : "#ef9a9a"; // 綠色可選 / 紅色不可選
                    }

                    ctx.fillRect(table.x, table.y, 60, 40);
                    ctx.fillStyle = "#000";
                    ctx.font = "bold 14px sans-serif";
                    ctx.textAlign = "center";
                    ctx.textBaseline = "middle";
                    ctx.fillText(table.tid, table.x + 30, table.y + 20);
                });

                // 入口、窗戶等標示保持不變
                ctx.fillStyle = "#90caf9";
                ctx.fillRect(200, 370, 70, 30);
                ctx.fillStyle = "#000";
                ctx.font = "bold 16px sans-serif";
                ctx.fillText("Counter", 235, 385);

                ctx.fillStyle = "#4caf50";   // 綠色實心
                ctx.fillRect(0, 10, 350, 20); // 實心矩形
                ctx.fillStyle = "#000";      // 黑色文字
                ctx.font = "bold 16px sans-serif";
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";
                ctx.fillText("Window", 175, 20); // 文字置中顯示

                ctx.fillStyle = "#4caf50";   // 綠色實心
                ctx.fillRect(330, 10, 20, 350); // 實心矩形
                ctx.fillStyle = "#000";      // 黑色文字
                ctx.font = "bold 16px sans-serif";
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";

                ctx.fillStyle = "brown";
                ctx.lineWidth = 3;
                ctx.fillRect(0, 10, 20, 390);

                ctx.fillStyle = "#red";
                ctx.fillRect(300, 370, 70, 30);
                ctx.fillStyle = "#000";
                ctx.font = "bold 16px sans-serif";
                ctx.fillText("Door", 325, 385);

// 儲存當前狀態
                ctx.save();

// 移動座標系到文字要顯示的位置
                ctx.translate(15, 170);

// 旋轉 90 度（PI/2 弧度）
                ctx.rotate(-Math.PI / 2);

// 繪製文字（因為已經旋轉過座標系）
                ctx.fillStyle = "#000";
                ctx.font = "bold 16px sans-serif";
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";
                ctx.fillText("Well", 0, 0);

// 還原狀態
                ctx.restore();

            }

            // 點擊選座位
            canvas.addEventListener("click", function (e) {
                const rect = canvas.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;

                tables.forEach(table => {
                    if (x >= table.x && x <= table.x + 60 &&
                        y >= table.y && y <= table.y + 40) {
                        if (!table.available) {
                            alert(`座位 ${table.tid} 不可用`);
                            return;
                        }

                        // 清除其他選中
                        tables.forEach(t => t.selected = false);
                        table.selected = true;

                        // 更新 hidden input
                        document.getElementById("tid").value = table.tid.replace("T", "");

                        // Debug：確認值是否更新
                        console.log("選中座位:", document.getElementById("tid").value);

                        // 重新繪製
                        drawTables();
                    }
                });
            });



            // 載入可用座位
            document.getElementById("loadTablesBtn").addEventListener("click", function () {
                const date = document.getElementById("date").value;
                const time = document.getElementById("time").value;
                const guests = document.getElementById("guests").value;

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
                        // 全部設為不可用
                        tables.forEach(t => t.available = false);

                        // 更新可用座位
                        data.forEach(table => {
                            const target = tables.find(t => t.tid === "T" + table.tid);
                            if (target) target.available = table.available;
                        });

                        drawTables(); // 重新繪製
                    })
                    .catch(error => {
                        console.error("Failed to load tables:", error);
                    });
            });

            // 初始繪製
            drawTables();
        });
    </script>

    <script>
        document.addEventListener("DOMContentLoaded", function () {
            const dateInput = document.getElementById("date");

            // 取得今天日期
            const today = new Date();

            // 計算明天日期
            const tomorrow = new Date(today);
            tomorrow.setDate(today.getDate() + 1);

            // 格式化成 YYYY-MM-DD
            const yyyy = tomorrow.getFullYear();
            const mm = String(tomorrow.getMonth() + 1).padStart(2, '0');
            const dd = String(tomorrow.getDate()).padStart(2, '0');
            const tomorrowStr = `${yyyy}-${mm}-${dd}`;

            // 設定最小日期為明天
            dateInput.setAttribute("min", tomorrowStr);

            // 預設值也設為明天
            dateInput.value = tomorrowStr;
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
        Yummy Restaurant
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
    <h2>Welcome to Yummy Restaurant</h2>
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
            <h4>Floor Plan</h4>
            <canvas id="floorCanvas" width="350" height="400" style="border:0px solid #ccc;"></canvas>
            <input type="hidden" id="tid" name="tid" required>
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
