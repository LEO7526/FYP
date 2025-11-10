<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => $conn->connect_error]));
}

// Get POST data
$email = $_POST['cemail'] ?? '';
$birthday = $_POST['cbirthday'] ?? '';

// Validate input
if (empty($email) || empty($birthday)) {
    echo json_encode(["success" => false, "message" => "Missing parameters"]);
    $conn->close();
    exit;
}

// Only update if birthday is currently NULL or empty
$query = "UPDATE customer 
          SET cbirthday = ? 
          WHERE cemail = ? AND (cbirthday IS NULL OR cbirthday = '')";

$stmt = $conn->prepare($query);
$stmt->bind_param("ss", $birthday, $email);

if ($stmt->execute() && $stmt->affected_rows > 0) {
    echo json_encode([
        "success" => true,
        "message" => "Birthday updated successfully",
        "cbirthday" => $birthday
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => "Birthday already set or update failed"
    ]);
}

$stmt->close();
$conn->close();
?>