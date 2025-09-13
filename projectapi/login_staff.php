<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

// Accept POST parameters as sent by Android app
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$role = $_POST['role'] ?? '';

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Database connection failed"]);
    exit();
}

// Look for user with matching email, password, and role
$stmt = $conn->prepare("SELECT sid, sname, srole FROM staff WHERE semail=? AND spassword=? AND srole=?");
$stmt->bind_param("sss", $email, $password, $role);
$stmt->execute();
$res = $stmt->get_result();

if ($row = $res->fetch_assoc()) {
    echo json_encode([
        "success" => true,
        "message" => "Login successful",
        "role" => $row['srole'],
        "sid" => $row['sid'],
        "sname" => $row['sname']
    ]);
} else {
    http_response_code(401);
    echo json_encode([
        "success" => false,
        "message" => "Invalid email, password, or role"
    ]);
}
$stmt->close();
$conn->close();
?>