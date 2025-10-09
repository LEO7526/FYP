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
        'message'    => 'No file uploaded or file upload failed',
        'errorCode'  => $errorCode
    ]);
    exit;
}

$uploadedFile = $_FILES['image'];
$staffEmail   = $_POST['semail'] ?? null;

if (!$staffEmail) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Missing staff email'
    ]);
    exit;
}

// Validate file type
$allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
$fileType     = mime_content_type($uploadedFile['tmp_name']);
if (!in_array($fileType, $allowedTypes)) {
    http_response_code(400);
    echo json_encode([
        'success'      => false,
        'message'      => 'Only JPEG, PNG, GIF, or WebP images are allowed',
        'detectedType' => $fileType
    ]);
    exit;
}

// Generate filename
$fileExtension = pathinfo($uploadedFile['name'], PATHINFO_EXTENSION);
$fileName = preg_replace('/[^a-zA-Z0-9]/', '_', $staffEmail) . '.' . $fileExtension;

// Save under staff profile folder
$filePathInRepo = 'Image/Profile_image/Staff/' . $fileName;

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
        'Authorization: Bearer ' . $githubToken,
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
        'Authorization: Bearer ' . $githubToken,
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

    if (!$imageUrl) {
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'Upload succeeded but no image URL returned'
        ]);
        exit;
    }

    // ✅ Update staff table with full GitHub URL
    $updateQuery = "UPDATE staff SET simageurl = ? WHERE semail = ?";
    $stmt = $conn->prepare($updateQuery);
    $stmt->bind_param("ss", $imageUrl, $staffEmail);
    $stmt->execute();

    // ✅ Return full GitHub URL in response
    echo json_encode([
        'success'   => true,
        'status'    => 'success',
        'message'   => 'Staff profile image uploaded and saved',
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