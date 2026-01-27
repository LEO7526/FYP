<?php
require 'stripe-demo/vendor/autoload.php';

// ✅ 使用你的 Secret Key
\Stripe\Stripe::setApiKey('sk_test_51S56Q5CEiSaWf7OeDUguzBcEbvJdjYZCYYfWjx4Ctu1iQPLcusx9YpFtXJahOYZftMTh3DBFtCW0NhxpqP8SIS9000ylQ4xZat');

header('Content-Type: application/json');

$input = json_decode(file_get_contents('php://input'), true);
$amount = $input['amount'] ?? 0;
$cid = $input['cid'] ?? null;
$paymentMethod = $input['paymentMethod'] ?? 'card'; // Default to card if not specified

// Stripe minimum for HKD is HK$5.00 = 500 cents
if ($amount < 500) {
    http_response_code(400);
    echo json_encode(['error' => 'Amount must be at least HK$5.00']);
    exit;
}

error_log("create_payment_intent: paymentMethod = " . $paymentMethod);
error_log("create_payment_intent: amount = " . $amount);

// PaymentSheet only supports card payments
$paymentMethodTypes = ['card'];
error_log("create_payment_intent: Using Card payment method (PaymentSheet only supports card)");

try {
    error_log("create_payment_intent: Attempting to create PaymentIntent with " . json_encode($paymentMethodTypes));
    
    $intentParams = [
        'amount' => $amount,
        'currency' => 'hkd',
        'payment_method_types' => $paymentMethodTypes,
        'metadata' => [
            'customer_id' => $cid,
            'payment_method' => $paymentMethod,
            'description' => 'Yummy Restaurant Order',
            'customer_location' => 'HK'
        ]
    ];
    
    error_log("create_payment_intent: Final payment methods sent to Stripe: " . json_encode($paymentMethodTypes));
    
    $intent = \Stripe\PaymentIntent::create($intentParams);

    error_log("create_payment_intent: SUCCESS - PaymentIntent created: " . $intent->id);
    error_log("create_payment_intent: Payment method types returned by Stripe: " . json_encode($intent->payment_method_types));
    error_log("create_payment_intent: clientSecret = " . $intent->client_secret);
    
    // Log the actual payment methods returned by Stripe for debugging
    if (empty($intent->payment_method_types)) {
        error_log("create_payment_intent: WARNING - Stripe returned empty payment_method_types. Payment method may not be supported.");
    }

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'clientSecret' => $intent->client_secret,
        'paymentIntentId' => $intent->id,
        'paymentMethodsCreated' => $paymentMethodTypes
    ]);
    exit;

} catch (\Stripe\Exception\ApiErrorException $e) {
    // Handle Stripe API errors
    error_log("create_payment_intent: Stripe API Error - " . $e->getMessage());
    error_log("create_payment_intent: Error type: " . get_class($e));
    error_log("create_payment_intent: HTTP Status: " . $e->getHttpStatus());
    error_log("create_payment_intent: Stripe Error Code: " . $e->getStripeCode());
    error_log("create_payment_intent: Full error details: " . json_encode([
        'message' => $e->getMessage(),
        'code' => $e->getStripeCode(),
        'http_status' => $e->getHttpStatus(),
        'type' => get_class($e)
    ]));
    
    error_log("create_payment_intent: Card payment failed, no fallback available");
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error' => 'Payment processing failed. Please try again.',
        'details' => $e->getMessage()
    ]);
    exit;

} catch (Exception $e) {
    error_log("create_payment_intent: Unexpected error - " . $e->getMessage());
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'Unexpected error: ' . $e->getMessage()
    ]);
    exit;
}
?>
