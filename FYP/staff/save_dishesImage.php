<?php
require_once '../conn.php';

// GitHub configuration
$githubToken = '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';
$repoOwner = 'LEO7526';
$repoName = 'FYP';
$branch = 'main';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Only POST requests are allowed']);
    exit;
}

if (!isset($_FILES['dishImage']) || $_FILES['dishImage']['error'] !== UPLOAD_ERR_OK) {
    $errorCode = $_FILES['dishImage']['error'] ?? 'undefined';
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'No file uploaded or file upload failed',
        'errorCode' => $errorCode
    ]);
    exit;
}

$uploadedFile = $_FILES['dishImage'];
$itemId = isset($_POST['itemId']) ? $_POST['itemId'] : null;

// Validate file type
$allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
$fileType = mime_content_type($uploadedFile['tmp_name']);
if (!in_array($fileType, $allowedTypes)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Only JPEG, PNG, GIF, or WebP images are allowed',
        'detectedType' => $fileType
    ]);
    exit;
}

// Validate file size (max 5MB)
if ($uploadedFile['size'] > 5 * 1024 * 1024) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'File size must not exceed 5MB',
        'fileSize' => $uploadedFile['size']
    ]);
    exit;
}

try {
    // Generate filename: use itemId as filename
    $fileExtension = pathinfo($uploadedFile['name'], PATHINFO_EXTENSION);
    if ($itemId) {
        // Modified here: use itemId directly as filename, no 'dish_' prefix
        $fileName = $itemId . '.' . $fileExtension;
    } else {
        $fileName = uniqid() . '_' . time() . '.' . $fileExtension;
    }
    $filePathInRepo = 'Image/dish/' . $fileName;

    // Read file content and encode as base64
    $fileContent = base64_encode(file_get_contents($uploadedFile['tmp_name']));

    // Prepare GitHub API request - first check if file exists to get SHA
    $apiUrl = "https://api.github.com/repos/{$repoOwner}/{$repoName}/contents/{$filePathInRepo}";

    // First, check if file already exists
    $ch = curl_init();
    curl_setopt_array($ch, [
        CURLOPT_URL => $apiUrl,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => [
            'Authorization: token ' . $githubToken,
            'User-Agent: PHP-Script',
            'Accept: application/vnd.github.v3+json'
        ],
        CURLOPT_SSL_VERIFYPEER => false,
        CURLOPT_TIMEOUT => 30
    ]);

    $checkResponse = curl_exec($ch);
    $checkHttpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    $sha = null;
    if ($checkHttpCode === 200) {
        $fileData = json_decode($checkResponse, true);
        $sha = $fileData['sha'] ?? null;
    }

    curl_close($ch);

    // Prepare upload data
    $postData = [
        'message' => 'Upload dish image: ' . $fileName,
        'content' => $fileContent,
        'branch' => $branch
    ];

    // If file exists, include SHA
    if ($sha) {
        $postData['sha'] = $sha;
    }

    // Initialize cURL for upload
    $ch = curl_init();

    curl_setopt_array($ch, [
        CURLOPT_URL => $apiUrl,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_CUSTOMREQUEST => 'PUT',
        CURLOPT_POSTFIELDS => json_encode($postData),
        CURLOPT_HTTPHEADER => [
            'Authorization: token ' . $githubToken,
            'User-Agent: PHP-Script',
            'Content-Type: application/json',
            'Accept: application/vnd.github.v3+json'
        ],
        CURLOPT_SSL_VERIFYPEER => false,
        CURLOPT_TIMEOUT => 30
    ]);

    // Execute request
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError = curl_error($ch);

    curl_close($ch);

    if ($httpCode === 201 || $httpCode === 200) {
        // Upload success (201 = created, 200 = updated)
        $responseData = json_decode($response, true);
        $imageUrl = $responseData['content']['download_url'];

        echo json_encode([
            'success' => true,
            'message' => 'Image uploaded successfully',
            'imageUrl' => $imageUrl,
            'fileName' => $fileName
        ]);
    } else {
        // Upload failed
        $errorData = json_decode($response, true);
        $errorMessage = $errorData['message'] ?? 'Upload failed, HTTP status code: ' . $httpCode;

        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => $errorMessage,
            'httpCode' => $httpCode,
            'response' => $response
        ]);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Server error: ' . $e->getMessage()
    ]);
}
?>