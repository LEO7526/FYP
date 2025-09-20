<?php
require 'vendor/autoload.php';

\Stripe\Stripe::setApiKey('sk_test_51S56QLC1wirzkW6G7v8Ckh7IZEbXZrlj8aAFsIM6IYpxt0dh2x0uglpncPOUJ4d62zFW38nHmNvAtZk14EFdf7au00GsAaPdIS');

header('Content-Type: application/json');

$input = json_decode(file_get_contents('php://input'), true);
$amount = $input['amount'] ?? 0;

// Stripe minimum for HKD is HK$5.00 = 500 cents
if ($amount < 500) {
    http_response_code(400);
    echo json_encode(['error' => 'Amount must be at least HK$5.00']);
    exit;
}

try {
    $intent = \Stripe\PaymentIntent::create([
        'amount' => $amount,
        'currency' => 'hkd',
        'automatic_payment_methods' => ['enabled' => true],
    ]);

    echo json_encode(['clientSecret' => $intent->client_secret]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}