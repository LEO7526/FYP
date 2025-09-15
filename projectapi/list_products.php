<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

$result = $conn->query("SELECT pid, pname, pcost FROM product");
$out = $result->fetch_all(MYSQLI_ASSOC);
echo json_encode($out, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
$conn->close();
?>