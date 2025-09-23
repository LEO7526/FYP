<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => $conn->connect_error]));
}

// Get POST data
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

// Validate credentials
$query = "SELECT cid, cname, ctel, caddr, company, cimageurl FROM customer WHERE cemail = '$email' AND cpassword = '$password'";
$result = $conn->query($query);

if ($result && $result->num_rows > 0) {
    $data = $result->fetch_assoc();
    echo json_encode([
        "success" => true,
        "role" => "customer",
        "userId" => $data["cid"],
        "userName" => $data["cname"],
		"userTel" => $data["ctel"],
		"userImageUrl" => $data["cimageurl"],
        "message" => "Login successful"
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Invalid credentials"]);
}

$conn->close();
?>