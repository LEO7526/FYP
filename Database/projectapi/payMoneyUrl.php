<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// ✅ Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database connection failed: " . $conn->connect_error]);
    exit;
}

// ✅ Parse incoming JSON
$input = json_decode(file_get_contents("php://input"), true);
if (!is_array($input)) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid JSON payload']);
    exit;
}

// ✅ Validate required fields
if (!isset($input['amount']) || !isset($input['cid'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Missing required fields: amount or cid']);
    exit;
}

// ✅ Sanitize and extract values
$amount = filter_var($input['amount'], FILTER_VALIDATE_INT);
$cid = filter_var($input['cid'], FILTER_VALIDATE_INT);
$currency = htmlspecialchars($input['currency'] ?? '344'); // HKD
$payType = htmlspecialchars($input['payType'] ?? 'ALIPAY');

if ($amount === false || $cid === false) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid amount or cid']);
    exit;
}

// ✅ PayDollar credentials
$merchantId = 'your_merchant_id';
$secureHashKey = 'your_secure_hash_key';
$payDollarEndpoint = 'https://www.paydollar.com/b2c2/eng/payment/payForm.jsp';

// ✅ Generate order reference
$orderRef = uniqid('order_');
$amountFormatted = number_format($amount / 100, 2, '.', '');

// ✅ Build parameters
$params = [
    'merchantId' => $merchantId,
    'amount' => $amountFormatted,
    'orderRef' => $orderRef,
    'currCode' => $currency,
    'payType' => $payType,
    'successUrl' => 'http://10.0.2.2/NewFolder/Database/projectapi/api/payment-success.php?orderRef=' . $orderRef,
    'failUrl' => 'http://10.0.2.2/NewFolder/Database/projectapi/api/payment-fail.php?orderRef=' . $orderRef,
    'cancelUrl' => 'http://10.0.2.2/NewFolder/Database/projectapi/api/payment-cancel.php?orderRef=' . $orderRef,
];

// ✅ Generate secure hash
$concatenated = implode('', $params) . $secureHashKey;
$params['secureHash'] = hash('sha256', $concatenated);

// ✅ Build payment URL
$query = http_build_query($params);
$paymentUrl = $payDollarEndpoint . '?' . $query;

// ✅ Insert order into database
$odate = date('Y-m-d H:i:s');
$ostatus = 0;

$stmt = $conn->prepare("INSERT INTO orders (odate, cid, ostatus, orderRef) VALUES (?, ?, ?, ?)");
if (!$stmt) {
    http_response_code(500);
    echo json_encode(['error' => 'Database prepare failed']);
    exit;
}

$stmt->bind_param("siss", $odate, $cid, $ostatus, $orderRef);
if (!$stmt->execute()) {
    http_response_code(500);
    echo json_encode(['error' => 'Failed to insert order']);
    exit;
}

$stmt->close();
$conn->close();

// ✅ Optional logging
file_put_contents('log.txt', date('Y-m-d H:i:s') . " - OrderRef: $orderRef - Amount: $amountFormatted" . PHP_EOL, FILE_APPEND);

// ✅ Return response
echo json_encode([
    'paymentUrl' => $paymentUrl,
    'orderRef' => $orderRef
]);