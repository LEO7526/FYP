<?php
header('Content-Type: application/json');
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success"=>false,"error"=>"DB connection failed"]);
    exit;
}

$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
if ($cid === 0) {
    echo json_encode(["success"=>true,"points"=>0]);
    exit;
}

$res = $conn->query("SELECT points FROM coupon_point WHERE cid=$cid LIMIT 1");
if ($row = $res->fetch_assoc()) {
    echo json_encode(["success"=>true,"points"=>(int)$row['points']]);
} else {
    echo json_encode(["success"=>true,"points"=>0]);
}
$conn->close();