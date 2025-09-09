<?php
ob_start();
require_once '../auth_check.php';
check_staff_auth();



if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    include '../conn.php';

    // Check if image was uploaded successfully
    $imageUploaded = isset($_FILES['materialImage']) && $_FILES['materialImage']['error'] === UPLOAD_ERR_OK;

    // Define allowed image types and max size (2MB)
    $allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
    $maxFileSize = 2 * 1024 * 1024; // 2MB

    // Process form data
    $name = mysqli_real_escape_string($conn, $_POST['materialName']);
    $unit = mysqli_real_escape_string($conn, $_POST['unit']);
    $physicalQty = (int)$_POST['physicalQuantity'];
    $reorderQty = (int)$_POST['reorderQuantity'];
    $reservedQty = 0;

    // Validate reorder quantity
    if ($reorderQty > $physicalQty) {
        ob_end_clean();
        die("Error: Re-order quantity cannot exceed physical quantity");
    }

    // Insert material record into database
    $sql = "INSERT INTO material (mname, munit, mqty, mrqty, mreorderqty) 
            VALUES (?, ?, ?, ?, ?)";

    $stmt = mysqli_prepare($conn, $sql);
    mysqli_stmt_bind_param($stmt, "ssiii", $name, $unit, $physicalQty, $reservedQty, $reorderQty);

    // Execute SQL statement
    if (!mysqli_stmt_execute($stmt)) {
        ob_end_clean();
        die("Error inserting material: " . mysqli_error($conn));
    }

    // Get the auto-generated material ID
    $newMaterialId = mysqli_insert_id($conn);

    // Process image upload if provided
    if ($imageUploaded) {
        $fileInfo = $_FILES['materialImage'];

        // Validate file type and size
        if (in_array($fileInfo['type'], $allowedTypes) && $fileInfo['size'] <= $maxFileSize) {
            // Define image storage directory
            $imageDir = $_SERVER['DOCUMENT_ROOT'] . '/Sample Images/material/';

            // Create directory if it doesn't exist
            if (!is_dir($imageDir)) {
                mkdir($imageDir, 0777, true);
            }

            // Get file extension and create new filename
            $fileExt = pathinfo($fileInfo['name'], PATHINFO_EXTENSION);
            $newFileName = $newMaterialId . '.' . $fileExt;
            $targetPath = $imageDir . $newFileName;

            // Move uploaded file to permanent location
            move_uploaded_file($fileInfo['tmp_name'], $targetPath);
        }
    }

    // Clean up database resources
    mysqli_stmt_close($stmt);
    mysqli_close($conn);

    // Redirect to success page
    ob_end_clean();
    header('Location: StaffComplete.php');
    exit();
} else {
    exit();
}
?>