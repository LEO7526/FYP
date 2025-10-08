<?php
// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error]);
    exit;
}

// ⚠️ Hardcoded GitHub token (for demo; not safe for production)
$githubToken = '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'; 
$repoOwner   = 'LEO7526';
$repoName    = 'FYP';
$branch      = 'main';

header('Content-Type: application/json');

// Allow only POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Only POST requests are allowed']);
    exit;
}

// Validate file upload (Android sends "image")
if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    $errorCode = $_FILES['image']['error'] ?? 'undefined';
    http_response_code(400);
    echo json_encode([
        'success'    => false,
        'status'     => 'error',
        'message'    => 'No file uploaded or file upload failed',
        'errorCode'  => $errorCode
    ]);
    exit;
}

$uploadedFile = $_FILES['image'];
$email        = $_POST['cemail'] ?? null; // customer email

// Validate file type
$allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
$fileType     = mime_content_type($uploadedFile['tmp_name']);
if (!in_array($fileType, $allowedTypes)) {
    http_response_code(400);
    echo json_encode([
        'success'      => false,
        'status'       => 'error',
        'message'      => 'Only JPEG, PNG, GIF, or WebP images are allowed',
        'detectedType' => $fileType
    ]);
    exit;
}

// Generate filename (use email if available)
$fileExtension = pathinfo($uploadedFile['name'], PATHINFO_EXTENSION);
$fileName = $email ? (preg_replace('/[^a-zA-Z0-9]/', '_', $email) . '.' . $fileExtension)
                   : (uniqid() . '_' . time() . '.' . $fileExtension);

// Save under staff profile folder
$filePathInRepo = 'Image/Profile_image/Customer/' . $fileName;

// Read file content
$fileContent = base64_encode(file_get_contents($uploadedFile['tmp_name']));

// GitHub API URL
$apiUrl = "https://api.github.com/repos/{$repoOwner}/{$repoName}/contents/{$filePathInRepo}";

// Step 1: Check if file exists (to get SHA if updating)
$ch = curl_init();
curl_setopt_array($ch, [
    CURLOPT_URL            => $apiUrl,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_HTTPHEADER     => [
        'Authorization: token ' . $githubToken,
        'User-Agent: PHP-Script',
        'Accept: application/vnd.github.v3+json'
    ],
    CURLOPT_TIMEOUT        => 30
]);

$checkResponse = curl_exec($ch);
$checkHttpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$sha = null;

if ($checkHttpCode === 200) {
    $fileData = json_decode($checkResponse, true);
    $sha      = $fileData['sha'] ?? null;
}
curl_close($ch);

// Step 2: Prepare upload data
$postData = [
    'message' => 'Upload staff profile image: ' . $fileName,
    'content' => $fileContent,
    'branch'  => $branch
];
if ($sha) {
    $postData['sha'] = $sha; // update existing file
}

// Step 3: Upload/Update file
$ch = curl_init();
curl_setopt_array($ch, [
    CURLOPT_URL            => $apiUrl,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_CUSTOMREQUEST  => 'PUT',
    CURLOPT_POSTFIELDS     => json_encode($postData),
    CURLOPT_HTTPHEADER     => [
        'Authorization: token ' . $githubToken,
        'User-Agent: PHP-Script',
        'Content-Type: application/json',
        'Accept: application/vnd.github.v3+json'
    ],
    CURLOPT_TIMEOUT        => 30
]);

$response  = curl_exec($ch);
$httpCode  = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curlError = curl_error($ch);
curl_close($ch);

// Step 4: Handle response
if ($httpCode === 201 || $httpCode === 200) {
    $responseData = json_decode($response, true);
    $imageUrl     = $responseData['content']['download_url'] ?? null;

    echo json_encode([
        'success'   => true,
        'status'    => 'success',
        'message'   => 'Staff profile image uploaded successfully',
        'path'      => $filePathInRepo,
        'imageUrl'  => $imageUrl,
        'fileName'  => $fileName
    ]);
} else {
    $errorData    = json_decode($response, true);
    $errorMessage = $errorData['message'] ?? 'Upload failed, HTTP status code: ' . $httpCode;

    http_response_code(400);
    echo json_encode([
        'success'   => false,
        'status'    => 'error',
        'message'   => $errorMessage,
        'httpCode'  => $httpCode,
        'response'  => $response,
        'curlError' => $curlError
    ]);
}