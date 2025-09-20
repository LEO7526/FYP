<?php
// Include authentication check to ensure only staff can access this page
require_once '../auth_check.php';
check_staff_auth();

// Include database connection
include '../conn.php';

header('Content-Type: application/json');

// Check if required parameters are present
if (isset($_POST['bid']) && isset($_POST['action'])) {
    $bid = intval($_POST['bid']);
    $action = $_POST['action'];

    // Define allowed actions
    $allowedActions = ['accept', 'reject', 'cancel'];
    if (!in_array($action, $allowedActions)) {
        echo json_encode(['success' => false, 'error' => 'Invalid action']);
        exit();
    }

    // Determine new status based on action
    if ($action === 'accept') {
        $newStatus = 2; // Confirmed
    } elseif ($action === 'reject' || $action === 'cancel') {
        $newStatus = 0; // Cancelled
    }

    // Update booking status in the database
    $updateSql = "UPDATE booking SET status = $newStatus WHERE bid = $bid";
    if (!mysqli_query($conn, $updateSql)) {
        echo json_encode(['success' => false, 'error' => 'Database error: ' . mysqli_error($conn)]);
        exit();
    }

    echo json_encode(['success' => true]);
    exit();
} else {
    echo json_encode(['success' => false, 'error' => 'Missing parameters']);
    exit();
}
?>