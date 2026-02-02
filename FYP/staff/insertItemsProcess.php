<?php
// Start output buffering to prevent any output before headers
ob_start();

require_once '../auth_check.php';
check_staff_auth(); // Check staff authentication status

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    include '../conn.php'; // Database connection

    // Sanitize input data for product
    $name = mysqli_real_escape_string($conn, $_POST['productName']);
    $description = mysqli_real_escape_string($conn, $_POST['productDescription']);
    $cost = (float)$_POST['productCost'];

    // Start a database transaction
    mysqli_begin_transaction($conn);
    try {
        // Insert product information
        $sql = "INSERT INTO product (pname, pdesc, pcost) VALUES (?, ?, ?)";
        $stmt = mysqli_prepare($conn, $sql);
        mysqli_stmt_bind_param($stmt, "ssd", $name, $description, $cost);
        mysqli_stmt_execute($stmt);
        $productID = mysqli_insert_id($conn); // Get the auto-generated product ID

        // Insert material relationships for the product
        if (isset($_POST['materials']) && is_array($_POST['materials'])) {
            foreach ($_POST['materials'] as $material) {
                $mid = (int)$material['mid'];
                $qty = (int)$material['qty'];

                // Prepare and execute the insert query for prodmat table
                $sql2 = "INSERT INTO prodmat (pid, mid, pmqty) VALUES (?, ?, ?)";
                $stmt2 = mysqli_prepare($conn, $sql2);
                mysqli_stmt_bind_param($stmt2, "iii", $productID, $mid, $qty);
                mysqli_stmt_execute($stmt2);
            }
        }

        // Commit the transaction
        mysqli_commit($conn);

        // Process uploaded product image
        if (isset($_FILES['productImage']) && $_FILES['productImage']['error'] === UPLOAD_ERR_OK) {
            // Get file details
            $file = $_FILES['productImage'];
            $fileTmpPath = $file['tmp_name'];
            $fileName = $file['name'];
            $fileSize = $file['size'];
            $fileType = $file['type'];

            // Define allowed image types
            $allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
            if (!in_array($fileType, $allowedTypes)) {
                throw new Exception("Invalid file type. Only JPG, PNG, and GIF are allowed.");
            }

            // Set maximum file size (2MB)
            $maxFileSize = 2 * 1024 * 1024; // 2MB
            if ($fileSize > $maxFileSize) {
                throw new Exception("File size exceeds the maximum limit of 2MB.");
            }

            // Set the upload directory
            $uploadDir = dirname(__DIR__) . '/Sample Images/product/';

            // Create the directory if it doesn't exist
            if (!file_exists($uploadDir)) {
                mkdir($uploadDir, 0777, true);
            }

            // Set the new filename (using the product ID and .jpg extension)
            $newFileName = $productID . '.jpg';
            $destination = $uploadDir . $newFileName;

            // Move the uploaded file to the destination
            if (!move_uploaded_file($fileTmpPath, $destination)) {
                throw new Exception("Failed to move uploaded file.");
            }

        }

        // Clear the output buffer and redirect to success page
        ob_end_clean();
        header('Location: StaffComplete.php');
        exit();
    } catch (Exception $e) {
        // Roll back the transaction in case of error
        mysqli_rollback($conn);

        // Clean the output buffer and show error message
        ob_end_clean();
        die("Operation failed: " . $e->getMessage());
    } finally {
        mysqli_close($conn);
    }
} else {
    // If not a POST request, clean buffer and return error
    ob_end_clean();
    header('HTTP/1.1 400 Bad Request');
    echo "Invalid request";
    exit();
}
?>