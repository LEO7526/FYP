<?php
// staff_booking.php

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "projectdb";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$conn->set_charset("utf8");

$name  = $_POST['reserve_cname'] ?? '';
$tel_num = isset($_POST['tel_num']) ? $conn->real_escape_string($_POST['tel_num']) : '';
$date = isset($_POST['date']) ? $conn->real_escape_string($_POST['date']) : '';
$time = isset($_POST['time']) ? $conn->real_escape_string($_POST['time']) : '';
$guests = isset($_POST['guests']) ? intval($_POST['guests']) : 0;
$table = intval($_POST['tid']     ?? 0);
$purpose = isset($_POST['purpose']) && $_POST['purpose'] !== 'Null'
    ? "'" . $conn->real_escape_string($_POST['purpose']) . "'"
    : "NULL";
$remark = isset($_POST['remark']) ? $conn->real_escape_string($_POST['remark']) : '';

if (empty($name) || empty($tel_num) || empty($date) || empty($time) || $guests <= 0 || $table <= 0) {
    echo "Error: Please fill in all required fields.";
    exit;
}

$checkTableSql = "SELECT * FROM booking WHERE tid = $table AND bdate = '$date' AND btime BETWEEN DATE_SUB('$time', INTERVAL 90 MINUTE) AND DATE_ADD('$time', INTERVAL 90 MINUTE) AND status != 0";
$tableResult = $conn->query($checkTableSql);

if ($tableResult->num_rows > 0) {
    echo "Error: The selected table is not available at the chosen time. Please select a different table or time.";
    exit;
}

$sql = "INSERT INTO booking (cid, bkcname, bktel, tid, bdate, btime, pnum, purpose, remark, status) 
        VALUES (NULL, '$name', '$tel_num', $table, '$date', '$time', $guests, $purpose, '$remark', 2)";

if ($conn->query($sql)) {
    echo "Reservation created successfully!";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

$conn->close();
?>