<?php
require 'stripe-demo/vendor/autoload.php';

// ✅ 使用你的 Secret Key
\Stripe\Stripe::setApiKey('sk_test_51S56Q5CEiSaWf7OeDUguzBcEbvJdjYZCYYfWjx4Ctu1iQPLcusx9YpFtXJahOYZftMTh3DBFtCW0NhxpqP8SIS9000ylQ4xZat');

header('Content-Type: application/json');
date_default_timezone_set('Asia/Hong_Kong');

const DEFAULT_ORDER_TIMEZONE = 'Asia/Hong_Kong';
const ORDER_WINDOW_START_MINUTES = 11 * 60;
const ORDER_WINDOW_END_MINUTES = 21 * 60 + 30;

// Read payload first so timezone can be provided by client.
$input = json_decode(file_get_contents('php://input'), true);
if (!is_array($input)) {
    $input = [];
}

function resolveOrderTimezone(array $input): DateTimeZone {
    $candidate = '';

    if (!empty($input['client_timezone']) && is_string($input['client_timezone'])) {
        $candidate = trim($input['client_timezone']);
    }

    if ($candidate === '' && !empty($_SERVER['HTTP_X_CLIENT_TIMEZONE'])) {
        $candidate = trim((string)$_SERVER['HTTP_X_CLIENT_TIMEZONE']);
    }

    if ($candidate === '') {
        $envTz = getenv('RESTAURANT_TIMEZONE');
        if ($envTz !== false) {
            $candidate = trim((string)$envTz);
        }
    }

    if ($candidate === '') {
        $candidate = DEFAULT_ORDER_TIMEZONE;
    }

    try {
        return new DateTimeZone($candidate);
    } catch (Exception $e) {
        error_log('create_payment_intent: invalid timezone "' . $candidate . '", fallback to ' . DEFAULT_ORDER_TIMEZONE);
        return new DateTimeZone(DEFAULT_ORDER_TIMEZONE);
    }
}

function getOrderWindowContext(array $input): array {
    $tz = resolveOrderTimezone($input);
    $now = new DateTimeImmutable('now', $tz);
    $minutes = ((int)$now->format('H')) * 60 + (int)$now->format('i');
    $allowed = $minutes >= ORDER_WINDOW_START_MINUTES && $minutes < ORDER_WINDOW_END_MINUTES;

    return [
        'allowed' => $allowed,
        'timezone' => $tz->getName(),
        'server_time' => $now->format('Y-m-d H:i:s P'),
        'minutes' => $minutes,
        'window' => '11:00-21:29'
    ];
}

$window = getOrderWindowContext($input);
if (!$window['allowed']) {
    error_log('create_payment_intent: order window blocked; tz=' . $window['timezone'] . ', server_time=' . $window['server_time']);
    http_response_code(403);
    echo json_encode([
        'success' => false,
        'message' => 'Only available during business hours (11:00-21:29).',
        'error' => 'Outside ordering hours',
        'timezone_used' => $window['timezone'],
        'server_time' => $window['server_time'],
        'window' => $window['window']
    ]);
    exit;
}
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
