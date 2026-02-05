<?php
session_start();
require __DIR__ . '/../vendor/autoload.php'; // Stripe SDK

\Stripe\Stripe::setApiKey("sk_test_51SwY8w8PP5awDROKq5rfpj5DUtA8vSchsPOUTZNBJZ7pbHSCfCJg2AVBTEn1oqlRAImddRFHb1OQf6VFufAVT1O000NWHTvRjY"); // 換成你的 Stripe 測試 Secret Key

$totalPrice = $_SESSION['total_price'] ?? 0;
$amount = intval($totalPrice * 100); // Stripe 金額單位是分

$orderRef = uniqid("ORD");

// 建立 PaymentIntent
$paymentIntent = \Stripe\PaymentIntent::create([
        'amount' => $amount,
        'currency' => 'hkd',
        'payment_method_types' => ['card'],
        'metadata' => [
                'orderRef' => $orderRef,
                'customer_id' => $_SESSION['cid'] ?? 0
        ]
]);

$clientSecret = $paymentIntent->client_secret;
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Credit Card Payment</title>
    <script src="https://js.stripe.com/v3/"></script>
    <link rel="stylesheet" href="insertOrder.css">
</head>
<body>
<form id="payment-form">
    <label>
        Cardholder Name
        <input type="text" id="cardholder-name" placeholder="Your Name" required>
    </label>
    <label>
        Card Number
        <div id="card-number"></div>
    </label>
    <label>
        Expiration Date
        <div id="card-expiry"></div>
    </label>
    <label>
        CVC
        <div id="card-cvc"></div>
    </label>
    <div id="card-errors" role="alert"></div>
    <button id="submit">Pay HK$<?= number_format($totalPrice, 2) ?></button>
</form>

<script>
    const stripe = Stripe("pk_test_51SwY8w8PP5awDROKDdi4e3BRNXxqNWMFmhjOfocxocN8n6ug2WpSuBkJYWvUWgm0FiVRvhyCEJRiSLXUM9UDpI9h001KN32oil");

    const elements = stripe.elements({
        style: {
            base: {
                color: "#32325d",
                fontFamily: "Arial, sans-serif",
                fontSize: "16px",
                "::placeholder": { color: "#a0a0a0" }
            },
            invalid: {
                color: "#fa755a"
            }
        }
    });

    const cardNumberElement = elements.create('cardNumber');
    cardNumberElement.mount('#card-number');

    const cardExpiryElement = elements.create('cardExpiry');
    cardExpiryElement.mount('#card-expiry');

    const cardCvcElement = elements.create('cardCvc');
    cardCvcElement.mount('#card-cvc');

    const clientSecret = "<?= $clientSecret ?>";

    const form = document.getElementById('payment-form');
    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const cardholderName = document.getElementById('cardholder-name').value;

        const {error, paymentIntent} = await stripe.confirmCardPayment(clientSecret, {
            payment_method: {
                card: cardNumberElement,
                billing_details: {
                    name: cardholderName
                }
            }
        });

        if (error) {
            document.getElementById('card-errors').textContent = error.message;
        } else if (paymentIntent.status === "succeeded") {
            window.location.href = "order_paid.php?orderRef=<?= $orderRef ?>";
        }
    });
</script>
</body>
</html>
