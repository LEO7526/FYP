<?php
$targetDir = "uploads/";
if (!file_exists($targetDir)) {
    mkdir($targetDir, 0777, true);
}

$response = [];

if (isset($_FILES["image"]) && isset($_POST["cemail"])) {
    $email = $_POST["cemail"];
    $fileName = basename($_FILES["image"]["name"]);
    $targetFile = $targetDir . $fileName;

    if (move_uploaded_file($_FILES["image"]["tmp_name"], $targetFile)) {
        // ✅ Save image path to database
        $host = "localhost";
        $db = "ProjectDB";
        $user = "root";
        $pass = "";

        $conn = new mysqli($host, $user, $pass, $db);
        if ($conn->connect_error) {
            $response["status"] = "error";
            $response["message"] = "Database connection failed";
        } else {
            $stmt = $conn->prepare("UPDATE customer SET cimageurl = ? WHERE cemail = ?");
            $stmt->bind_param("ss", $fileName, $email);

            if ($stmt->execute()) {
                $response["status"] = "success";
                $response["path"] = $targetFile;
            } else {
                $response["status"] = "error";
                $response["message"] = "Failed to update database";
            }

            $stmt->close();
            $conn->close();
        }
    } else {
        $response["status"] = "error";
        $response["message"] = "Failed to move file";
    }
} else {
    $response["status"] = "error";
    $response["message"] = "Missing image or email";
}

echo json_encode($response);
?>