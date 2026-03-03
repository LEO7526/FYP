<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET");
date_default_timezone_set("Asia/Hong_Kong");

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "DB connection failed"]);
    exit();
}

$cid = isset($_GET['cid']) ? (int)$_GET['cid'] : 0;
if ($cid <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "cid is required."]);
    $conn->close();
    exit();
}

$sql = "SELECT bid, cid, bkcname, bktel, tid, bdate, btime, pnum, purpose, remark, status
        FROM booking
        WHERE cid = ?
        ORDER BY bdate ASC, btime ASC";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result();

$bookings = [];
while ($row = $result->fetch_assoc()) {
    $bookings[] = [
        "bid" => (int)$row["bid"],
        "cid" => (int)$row["cid"],
        "bkcname" => $row["bkcname"],
        "bktel" => $row["bktel"],
        "tid" => (int)$row["tid"],
        "bdate" => $row["bdate"],
        "btime" => $row["btime"],
        "pnum" => (int)$row["pnum"],
        "purpose" => $row["purpose"],
        "remark" => $row["remark"],
        "status" => (int)$row["status"]
    ];
}

echo json_encode([
    "success" => true,
    "data" => $bookings
]);

$stmt->close();
$conn->close();
?>
