<?php
header('Content-Type: application/json');

$conn = new mysqli('localhost', 'root', '', 'ProjectDB');
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'status' => 'error',
        'message' => $conn->connect_error
    ]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'status' => 'error',
        'message' => 'Only POST requests are allowed'
    ]);
    exit;
}

if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    $errorCode = $_FILES['image']['error'] ?? 'undefined';
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'status' => 'error',
        'message' => 'No file uploaded or file upload failed',
        'errorCode' => $errorCode
    ]);
    exit;
}

$email = $_POST['cemail'] ?? null;
if (!$email) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'status' => 'error',
        'message' => 'Missing customer email'
    ]);
    exit;
}

$uploadedFile = $_FILES['image'];
$allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
$fileType = mime_content_type($uploadedFile['tmp_name']);
if (!in_array($fileType, $allowedTypes, true)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'status' => 'error',
        'message' => 'Only JPEG, PNG, GIF, or WebP images are allowed',
        'detectedType' => $fileType
    ]);
    exit;
}

$extension = strtolower(pathinfo($uploadedFile['name'], PATHINFO_EXTENSION));
if ($extension === '') {
    $mimeToExt = [
        'image/jpeg' => 'jpg',
        'image/png' => 'png',
        'image/gif' => 'gif',
        'image/webp' => 'webp'
    ];
    $extension = $mimeToExt[$fileType] ?? 'jpg';
}

$safeEmail = preg_replace('/[^a-zA-Z0-9]/', '_', $email);
$fileName = $safeEmail . '.' . $extension;
$relativePath = 'Image/Profile_image/Customer/' . $fileName;
$targetDir = dirname(__DIR__, 2) . DIRECTORY_SEPARATOR . 'Image' . DIRECTORY_SEPARATOR . 'Profile_image' . DIRECTORY_SEPARATOR . 'Customer';
$targetPath = $targetDir . DIRECTORY_SEPARATOR . $fileName;

if (!is_dir($targetDir) && !mkdir($targetDir, 0775, true)) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'status' => 'error',
        'message' => 'Failed to create image directory'
    ]);
    exit;
}

if (!move_uploaded_file($uploadedFile['tmp_name'], $targetPath)) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'status' => 'error',
        'message' => 'Failed to save uploaded file'
    ]);
    exit;
}

$scriptPath = $_SERVER['SCRIPT_NAME'] ?? '';
$projectPath = dirname(dirname(dirname($scriptPath)));
$projectPath = rtrim(str_replace('\\', '/', $projectPath), '/');
$scheme = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
$host = $_SERVER['HTTP_HOST'] ?? 'localhost';
$imageUrl = $scheme . '://' . $host . $projectPath . '/' . $relativePath;

$updateQuery = 'UPDATE customer SET cimageurl = ? WHERE cemail = ?';
$stmt = $conn->prepare($updateQuery);
if (!$stmt) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'status' => 'error',
        'message' => 'Failed to prepare database update'
    ]);
    exit;
}

$stmt->bind_param('ss', $imageUrl, $email);
if (!$stmt->execute()) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'status' => 'error',
        'message' => 'Failed to update customer image path'
    ]);
    exit;
}

$stmt->close();
$conn->close();

echo json_encode([
    'success' => true,
    'status' => 'success',
    'message' => 'Customer profile image uploaded and saved',
    'path' => $relativePath,
    'imageUrl' => $imageUrl,
    'fileName' => $fileName
]);
