<?php
session_start();

if (!isset($_SESSION['user'])) {
    header('HTTP/1.1 403 Forbidden');
    exit();
}

include '../conn.php';

$userId = $_SESSION['user']['id'];
$sql = "SELECT * FROM staff WHERE sid = '$userId'";
$result = mysqli_query($conn, $sql);

if (mysqli_num_rows($result) === 1) {
    $user = mysqli_fetch_assoc($result);
    $profileData = [
        'id' => $user['sid'],
        'name' => $user['sname'],
        'role' => $user['srole'],
        'tel' => $user['stel']
    ];
    header('Content-Type: application/json');
    echo json_encode($profileData);
} else {
    header('HTTP/1.1 404 Not Found');
    echo json_encode(['error' => 'User not found']);
}

mysqli_close($conn);
?>