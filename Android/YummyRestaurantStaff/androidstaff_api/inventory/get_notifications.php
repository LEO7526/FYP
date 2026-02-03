<?php
// get_notifications.php

header('Content-Type: application/json');
require_once 'db_config.php';

$response = ['success' => false, 'data' => []];

// Find all unread notifications
$sql_get = "SELECT id, title, message FROM notifications WHERE is_read = FALSE";
$result = mysqli_query($conn, $sql_get);

if ($result) {
    $response['success'] = true;
    $notification_ids = [];
    while ($row = mysqli_fetch_assoc($result)) {
        $response['data'][] = $row;
        $notification_ids[] = $row['id'];
    }

    // After fetching, mark them as read so we don't fetch them again
    if (!empty($notification_ids)) {
        $ids_string = implode(',', $notification_ids);
        $sql_update = "UPDATE notifications SET is_read = TRUE WHERE id IN ($ids_string)";
        mysqli_query($conn, $sql_update);
    }
}

mysqli_close($conn);
echo json_encode($response);
?>