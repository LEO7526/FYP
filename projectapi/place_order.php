<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

$data = json_decode(file_get_contents("php://input"), true);
$cid   = $data['cid'];
$pid   = $data['pid'];
$oqty  = $data['oqty'];

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) die(json_encode(["error" => "DB fail"]));

// 计算总成本
$stmt = $conn->prepare("SELECT pcost FROM product WHERE pid=?");
$stmt->bind_param("i", $pid);
$stmt->execute();
$stmt->bind_result($pcost);
$stmt->fetch();
$stmt->close();

$ocost = $pcost * $oqty;

$stmt = $conn->prepare(
    "INSERT INTO orders (odate, pid, oqty, ocost, cid, ostatus)
     VALUES (NOW(), ?, ?, ?, ?, 1)"
);
$stmt->bind_param("iidi", $pid, $oqty, $ocost, $cid);
$ok = $stmt->execute();
echo json_encode(["status"=>$ok?"ok":"fail","oid"=>$conn->insert_id]);
$stmt->close(); $conn->close();
?>