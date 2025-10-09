<?php
// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => $conn->connect_error]);
    exit;
}

// ⚠️ Hardcoded GitHub token (for demo; not safe for production)
$githubToken = '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'; // replace with your token
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

// Validate file upload
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
$email        = $_POST['cemail'] ?? null;

if (!$email) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Missing customer email'
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
$fileName = preg_replace('/[^a-zA-Z0-9]/', '_', $email) . '.' . $fileExtension;
$filePathInRepo = 'Image/Profile_image/Customer/' . $fileName;
$fileContent = base64_encode(file_get_contents($uploadedFile['tmp_name']));
$apiUrl = "https://api.github.com/repos/{$repoOwner}/{$repoName}/contents/{$filePathInRepo}";

// Step 1: Check if file exists
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
    'message' => 'Upload customer profile image: ' . $fileName,
    'content' => $fileContent,
    'branch'  => $branch
];
if ($sha) {
    $postData['sha'] = $sha;
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

    // ✅ Save full GitHub URL into database
    $updateQuery = "UPDATE customer SET cimageurl = ? WHERE cemail = ?";
    $stmt = $conn->prepare($updateQuery);
    $stmt->bind_param("ss", $imageUrl, $email);
    $stmt->execute();

    // ✅ Return full GitHub URL in response
    echo json_encode([
        'success'   => true,
        'status'    => 'success',
        'message'   => 'Customer profile image uploaded and saved',
        'imageUrl'  => $imageUrl,   // full GitHub URL
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