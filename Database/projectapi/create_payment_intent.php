<?php
require 'stripe-demo/vendor/autoload.php';

// ✅ 使用你的 Secret Key
\Stripe\Stripe::setApiKey('sk_test_51S56Q5CEiSaWf7OeDUguzBcEbvJdjYZCYYfWjx4Ctu1iQPLcusx9YpFtXJahOYZftMTh3DBFtCW0NhxpqP8SIS9000ylQ4xZat');

header('Content-Type: application/json');

$input = json_decode(file_get_contents('php://input'), true);
$amount = $input['amount'] ?? 0;
$cid = $input['cid'] ?? null;

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
        'payment_method_types' => ['card'],
        'metadata' => [
            'customer_id' => $cid,
            'description' => 'Yummy Restaurant Order'
        ]
    ]);

    echo json_encode([
        'success' => true,
        'clientSecret' => $intent->client_secret,
        'paymentIntentId' => $intent->id
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}
?>