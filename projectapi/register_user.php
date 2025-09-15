<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

$servername = "localhost";
$username = "root";
$password = ""; 
$dbname = "ProjectDB";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Database connection failed: " . $conn->connect_error]);
    exit();
}

$data = json_decode(file_get_contents("php://input"), true);

$name = $_POST['name'] ?? null;
$email = $_POST['email'] ?? null;
$password_from_app = $_POST['password'] ?? null;

if (empty($name) || empty($email) || empty($password_from_app)) {
    http_response_code(400); 
    echo json_encode(["success" => false, "message" => "All fields are required."]);
    exit();
}

$sql_check = "SELECT cid FROM customer WHERE cemail = ?";
$stmt_check = $conn->prepare($sql_check);
$stmt_check->bind_param("s", $email);
$stmt_check->execute();
$stmt_check->store_result();

if ($stmt_check->num_rows > 0) {
    http_response_code(409);
    echo json_encode(["success" => false, "message" => "This email is already in use."]);
    $stmt_check->close();
    $conn->close();
    exit();
}
$stmt_check->close();
// are based on the current test DB，adjust if the final DB  is different
$sql_insert = "INSERT INTO customer (cname, cemail, cpassword) VALUES (?, ?, ?)";
$stmt_insert = $conn->prepare($sql_insert);
$stmt_insert->bind_param("sss", $name, $email, $password_from_app);

if ($stmt_insert->execute()) {
    http_response_code(201);
    echo json_encode(["success" => true, "message" => "Registration successful!"]);
} else {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Registration failed due to a server error."]);
}

$stmt_insert->close();
$conn->close();

?>