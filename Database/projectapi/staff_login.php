<?php
// staff_login.php - For staff authentication
require_once '../../conn.php';
header('Content-Type: application/json');

$sname = $_POST['sname'] ?? '';
$spassword = $_POST['spassword'] ?? '';

if (empty($sname) || empty($spassword)) {
    echo json_encode(["status" => "error", "message" => "Missing credentials"]);
    exit();
}

$sql = "SELECT sid, sname, spassword FROM staff WHERE sname = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $sname);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    // Simple password verification (in production, use proper hashing)
    if ($spassword === $row['spassword']) {
        echo json_encode([
            "status" => "success",
            "message" => "Login successful",
            "staff" => [
                "sid" => $row['sid'],
                "sname" => $row['sname']
            ]
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid password"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Staff not found"]);
}

$conn->close();
?>