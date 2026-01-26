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

// Determine payment method types based on user selection
$paymentMethodTypes = [];
$requestedMethod = $paymentMethod; // Track original request

error_log("create_payment_intent: paymentMethod = " . $paymentMethod);
error_log("create_payment_intent: amount = " . $amount);

if ($paymentMethod === 'alipay_hk' || $paymentMethod === 'alipay') {
    $paymentMethodTypes = ['alipay'];
    error_log("create_payment_intent: Using Alipay payment method (currency: hkd)");
} else {
    $paymentMethodTypes = ['card'];
    error_log("create_payment_intent: Using Card payment method");
}

error_log("create_payment_intent: paymentMethodTypes = " . json_encode($paymentMethodTypes));

try {
    error_log("create_payment_intent: Attempting to create PaymentIntent with " . json_encode($paymentMethodTypes));
    
    // Build payment intent parameters
    $intentParams = [
        'amount' => $amount,
        'currency' => 'hkd',
        'payment_method_types' => $paymentMethodTypes,
        'metadata' => [
            'customer_id' => $cid,
            'payment_method' => $paymentMethod,
            'description' => 'Yummy Restaurant Order'
        ]
    ];
    
    $intent = \Stripe\PaymentIntent::create($intentParams);

    error_log("create_payment_intent: SUCCESS - PaymentIntent created: " . $intent->id);
    error_log("create_payment_intent: Payment method types returned by Stripe: " . json_encode($intent->payment_method_types));
    error_log("create_payment_intent: clientSecret = " . $intent->client_secret);
    
    // Log the actual payment methods returned by Stripe for debugging
    if (empty($intent->payment_method_types)) {
        error_log("create_payment_intent: WARNING - Stripe returned empty payment_method_types. Payment method may not be supported.");
    }

    echo json_encode([
        'success' => true,
        'clientSecret' => $intent->client_secret,
        'paymentIntentId' => $intent->id,
        'paymentMethodsCreated' => $paymentMethodTypes
    ]);
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
    
    // If Alipay fails (not supported), retry with Card
    if (($paymentMethod === 'alipay_hk' || $paymentMethod === 'alipay') && $e->getHttpStatus() === 400) {
        error_log("create_payment_intent: Alipay failed (HTTP 400), retrying with Card");
        
        try {
            $fallbackIntent = \Stripe\PaymentIntent::create([
                'amount' => $amount,
                'currency' => 'hkd',
                'payment_method_types' => ['card'],
                'metadata' => [
                    'customer_id' => $cid,
                    'payment_method' => 'card_fallback_from_alipay',
                    'original_method' => 'alipay_hk',
                    'description' => 'Yummy Restaurant Order (Alipay fallback)'
                ]
            ]);
            
            error_log("create_payment_intent: FALLBACK SUCCESS - Card PaymentIntent created: " . $fallbackIntent->id);
            http_response_code(200); // Return 200 with fallback flag
            echo json_encode([
                'success' => true,
                'clientSecret' => $fallbackIntent->client_secret,
                'paymentIntentId' => $fallbackIntent->id,
                'fallback' => true,
                'fallbackReason' => 'Alipay not available, switched to Card',
                'paymentMethodsCreated' => ['card']
            ]);
        } catch (Exception $fallbackError) {
            error_log("create_payment_intent: FALLBACK FAILED - " . $fallbackError->getMessage());
            http_response_code(500);
            echo json_encode(['error' => 'Payment method unavailable: ' . $fallbackError->getMessage()]);
        }
    } else {
        http_response_code($e->getHttpStatus() ?? 500);
        echo json_encode(['error' => $e->getMessage()]);
    }
} catch (Exception $e) {
    error_log("create_payment_intent: Unexpected error - " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Unexpected error: ' . $e->getMessage()]);
}
?>