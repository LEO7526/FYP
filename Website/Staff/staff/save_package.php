<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true);

    try {
        mysqli_begin_transaction($conn);

        // Insert package
        $packageName = mysqli_real_escape_string($conn, $input['packageName']);
        $amounts = floatval($input['amounts']);
        $numOfType = count($input['types']);

        $packageQuery = "INSERT INTO menu_package (package_name, num_of_type, amounts) 
                         VALUES ('$packageName', $numOfType, $amounts)";
        mysqli_query($conn, $packageQuery);
        $packageId = mysqli_insert_id($conn);

        // Insert types and dishes
        foreach ($input['types'] as $type) {
            $optionalQuantity = intval($type['optional_quantity']);

            $typeQuery = "INSERT INTO package_type (package_id, optional_quantity) 
                          VALUES ($packageId, $optionalQuantity)";
            mysqli_query($conn, $typeQuery);
            $typeId = mysqli_insert_id($conn);

            // Insert type translations
            $languages = [
                'en' => $type['name_en'],
                'zh-CN' => $type['name_zh_cn'],
                'zh-TW' => $type['name_zh_tw']
            ];

            foreach ($languages as $lang => $name) {
                $name = mysqli_real_escape_string($conn, $name);
                $translationQuery = "INSERT INTO package_type_translation (type_id, type_language_code, type_name) 
                                     VALUES ($typeId, '$lang', '$name')";
                mysqli_query($conn, $translationQuery);
            }

            // Insert package dishes with price_modifier
            foreach ($type['dishes'] as $dish) {
                $dishId = intval($dish['item_id']);
                $priceModifier = floatval($dish['price_modifier']);
                $dishQuery = "INSERT INTO package_dish (package_id, type_id, item_id, price_modifier) 
                              VALUES ($packageId, $typeId, $dishId, $priceModifier)";
                mysqli_query($conn, $dishQuery);
            }
        }

        mysqli_commit($conn);
        echo json_encode(['success' => true, 'message' => 'Package created successfully']);

    } catch (Exception $e) {
        mysqli_rollback($conn);
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Error creating package: ' . $e->getMessage()]);
    }
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
}
?>