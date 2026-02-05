<?php
require 'vendor/autoload.php';
\Stripe\Stripe::setApiKey("sk_test_51SwY8w8PP5awDROKq5rfpj5DUtA8vSchsPOUTZNBJZ7pbHSCfCJg2AVBTEn1oqlRAImddRFHb1OQf6VFufAVT1O000NWHTvRjY");

$payload = @file_get_contents('php://input');
$event = json_decode($payload);

if ($event->type == 'payment_intent.succeeded') {
    $paymentIntent = $event->data->object;
    $orderRef = $paymentIntent->metadata->orderRef;

    $pdo = new PDO("mysql:host=localhost;dbname=projectdb;charset=utf8", "root", "");
    $stmt = $pdo->prepare("UPDATE orders SET ostatus = 'paid' WHERE orderRef = ?");
    $stmt->execute([$orderRef]);
}

