<?php
/**
 * Test script to verify customization data is being saved correctly
 * This simulates what the Android app sends
 */

header('Content-Type: application/json');

$host = 'localhost';
$user = 'root';
$pass = '';
$dbname = 'ProjectDB';

// Simulated order data (matching what Android sends)
$testOrderData = [
    'cid' => 1,
    'ostatus' => 1,
    'odate' => time() * 1000,
    'orderRef' => 'test_order_' . time(),
    'sid' => null,
    'table_number' => 'not chosen',
    'total_amount' => 3200,
    'items' => [
        [
            'item_id' => 3,
            'qty' => 1,
            'name' => 'Mouthwatering Chicken',
            'category' => 'Appetizers',
            'customization' => [
                'customization_details' => [
                    [
                        'option_id' => 3,
                        'option_name' => 'Spice Level',
                        'group_id' => 1,
                        'group_name' => 'Spice Level',
                        'selected_choices' => ['Mild'],
                        'additional_cost' => 0.0
                    ]
                ],
                'extra_notes' => 'Please make it less spicy'
            ]
        ]
    ]
];

echo "TEST: Sending order data to save_order.php...\n\n";
echo "Order data:\n";
echo json_encode($testOrderData, JSON_PRETTY_PRINT) . "\n\n";

// Send the data to save_order.php
$ch = curl_init('http://localhost/projectapi/save_order.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($testOrderData));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "Response (HTTP $httpCode):\n";
echo $response . "\n\n";

$responseData = json_decode($response, true);

if ($responseData && isset($responseData['order_id'])) {
    $orderId = $responseData['order_id'];
    echo "✅ Order saved successfully with ID: $orderId\n\n";
    
    // Now verify the customization was saved
    $conn = new mysqli($host, $user, $pass, $dbname);
    
    if ($conn->connect_error) {
        echo "❌ DB Connection failed: " . $conn->connect_error . "\n";
        exit;
    }
    
    // Check order_items
    echo "Checking order_items...\n";
    $itemSql = "SELECT oid, item_id, qty, note FROM order_items WHERE oid = ?";
    $stmt = $conn->prepare($itemSql);
    $stmt->bind_param("i", $orderId);
    $stmt->execute();
    $result = $stmt->get_result();
    
    while ($row = $result->fetch_assoc()) {
        echo "  Item: " . json_encode($row) . "\n";
    }
    $stmt->close();
    echo "\n";
    
    // Check order_item_customizations
    echo "Checking order_item_customizations...\n";
    $customSql = "SELECT * FROM order_item_customizations WHERE oid = ?";
    $stmt = $conn->prepare($customSql);
    $stmt->bind_param("i", $orderId);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $customCount = 0;
    while ($row = $result->fetch_assoc()) {
        $customCount++;
        echo "  Customization #$customCount: " . json_encode($row) . "\n";
    }
    $stmt->close();
    
    if ($customCount > 0) {
        echo "\n✅ SUCCESS: Found $customCount customization(s) saved in database!\n";
    } else {
        echo "\n❌ FAILED: No customizations found in database!\n";
    }
    
    $conn->close();
} else {
    echo "❌ Order save failed!\n";
}
?>
