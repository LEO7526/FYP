<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$role = strtolower($_POST['role'] ?? '');

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Database connection failed"]);
    exit();
}

if ($role == "staff") {
    $stmt = $conn->prepare("SELECT sid AS id, sname AS name, srole AS role FROM staff WHERE semail=? AND spassword=?");
    $stmt->bind_param("ss", $email, $password);
} else if ($role == "customer") {
    $stmt = $conn->prepare("SELECT cid AS id, cname AS name, crole AS role FROM customer WHERE cemail=? AND cpassword=?");
    $stmt->bind_param("ss", $email, $password);
} else {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid role"]);
    exit();
}

$stmt->execute();
$res = $stmt->get_result();

if ($row = $res->fetch_assoc()) {
    echo json_encode([
    "success" => true,
    "message" => "Login successful",
    "role" => $row['role'],
    "userId" => $row['id'],
    "userName" => $row['name']
]);

} else {
    http_response_code(401);
    echo json_encode([
        "success" => false,
        "message" => "Invalid email or password"
    ]);
}
$stmt->close();
$conn->close();
?>