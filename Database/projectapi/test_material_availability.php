<?php
// Test material availability API with various scenarios

require_once 'db_connect.php';

echo "<h2>Material Availability API Tests</h2>";

// Test 1: Normal quantity that should be available
echo "<h3>Test 1: Normal quantity (2 items)</h3>";
$test1 = json_encode([
    "items" => [
        ["item_id" => 3, "quantity" => 2, "is_package" => false]
    ]
]);
echo "Request: " . $test1 . "<br>";
makeApiCall($test1);

// Test 2: High quantity that should exceed availability
echo "<br><h3>Test 2: High quantity (50 items)</h3>";
$test2 = json_encode([
    "items" => [
        ["item_id" => 3, "quantity" => 50, "is_package" => false]
    ]
]);
echo "Request: " . $test2 . "<br>";
makeApiCall($test2);

// Test 3: Package test
echo "<br><h3>Test 3: Package (1 package)</h3>";
$test3 = json_encode([
    "items" => [
        ["item_id" => 1, "quantity" => 1, "is_package" => true]
    ]
]);
echo "Request: " . $test3 . "<br>";
makeApiCall($test3);

// Test 4: Mixed cart with multiple items
echo "<br><h3>Test 4: Mixed cart</h3>";
$test4 = json_encode([
    "items" => [
        ["item_id" => 3, "quantity" => 3, "is_package" => false],
        ["item_id" => 6, "quantity" => 2, "is_package" => false]
    ]
]);
echo "Request: " . $test4 . "<br>";
makeApiCall($test4);

function makeApiCall($jsonData) {
    $url = 'http://localhost/newFolder/Database/projectapi/check_material_availability.php';
    
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
    curl_setopt($ch, CURLOPT_POSTFIELDS, $jsonData);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Content-Type: application/json',
        'Content-Length: ' . strlen($jsonData))
    );
    
    $result = curl_exec($ch);
    curl_close($ch);
    
    $response = json_decode($result, true);
    if ($response) {
        echo "<strong>Result:</strong> " . ($response['all_available'] ? '✅ Available' : '❌ Insufficient') . "<br>";
        echo "<strong>Message:</strong> " . htmlspecialchars($response['message']) . "<br>";
        
        if (isset($response['material_check'])) {
            echo "<strong>Material Details:</strong><br>";
            foreach ($response['material_check'] as $material) {
                $status = $material['is_sufficient'] ? '✅' : '❌';
                echo "&nbsp;&nbsp;{$status} {$material['material_name']}: need {$material['required_quantity']}, have {$material['available_quantity']}<br>";
            }
        }
    } else {
        echo "<strong>Error:</strong> " . htmlspecialchars($result) . "<br>";
    }
}
?>