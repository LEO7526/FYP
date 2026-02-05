<?php
// Check Material Availability API
// Checks if there are enough materials to make food items or packages

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

require_once 'db_connect.php';

// Input validation and logging
error_log("check_material_availability.php - Starting request");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Only POST method allowed']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
error_log("check_material_availability.php - Input received: " . json_encode($input));

if (!isset($input['items']) || !is_array($input['items'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Items array required']);
    exit;
}

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    $materialRequirements = []; // [material_id => total_required_quantity]
    $unavailableItems = [];
    
    // Process each item in the request
    foreach ($input['items'] as $item) {
        $itemId = (int)$item['item_id'];
        $quantity = (int)$item['quantity'];
        $isPackage = isset($item['is_package']) ? $item['is_package'] : false;
        
        error_log("Processing item_id: $itemId, quantity: $quantity, is_package: " . ($isPackage ? 'true' : 'false'));
        
        if ($isPackage) {
            // Handle packages - get all items in the package
            $packageItems = getPackageItems($pdo, $itemId);
            if (empty($packageItems)) {
                error_log("Warning: Package $itemId has no items defined");
                continue;
            }
            
            foreach ($packageItems as $packageItem) {
                $itemMaterials = getItemMaterials($pdo, $packageItem['item_id']);
                if (empty($itemMaterials)) {
                    error_log("Warning: Item {$packageItem['item_id']} in package $itemId has no materials defined");
                    // For items without materials, consider them as unavailable for large quantities
                    $unavailableItems[] = [
                        'item_id' => $packageItem['item_id'], 
                        'reason' => 'No material recipe defined'
                    ];
                    continue;
                }
                
                foreach ($itemMaterials as $material) {
                    $materialId = $material['mid'];
                    $requiredQty = $material['quantity'] * $quantity; // Package quantity
                    
                    if (!isset($materialRequirements[$materialId])) {
                        $materialRequirements[$materialId] = 0;
                    }
                    $materialRequirements[$materialId] += $requiredQty;
                }
            }
        } else {
            // Handle individual items
            $itemMaterials = getItemMaterials($pdo, $itemId);
            if (empty($itemMaterials)) {
                error_log("Warning: Item $itemId has no materials defined");
                
                // Special case: Wooden Chopsticks (item_id: 22) - directly purchased, allow up to 1000 units
                if ($itemId == 22) {
                    if ($quantity > 1000) {
                        $unavailableItems[] = [
                            'item_id' => $itemId, 
                            'reason' => 'Wooden chopsticks limited to 1000 units per order'
                        ];
                        continue;
                    }
                    // Allow wooden chopsticks up to 1000 units
                    continue;
                }
                
                // For other items without materials, consider them as unavailable for large quantities (>5)
                if ($quantity > 5) {
                    $unavailableItems[] = [
                        'item_id' => $itemId, 
                        'reason' => 'No material recipe defined - limited to 5 units'
                    ];
                    continue;
                }
                // Allow small quantities for items without material requirements
                continue;
            }
            
            foreach ($itemMaterials as $material) {
                $materialId = $material['mid'];
                $requiredQty = $material['quantity'] * $quantity;
                
                if (!isset($materialRequirements[$materialId])) {
                    $materialRequirements[$materialId] = 0;
                }
                $materialRequirements[$materialId] += $requiredQty;
            }
        }
    }
    
    error_log("Total material requirements: " . json_encode($materialRequirements));
    
    // Check availability for each required material
    $availabilityCheck = [];
    foreach ($materialRequirements as $materialId => $requiredQty) {
        $availableQty = getMaterialQuantity($pdo, $materialId);
        $isAvailable = $availableQty >= $requiredQty;
        
        $availabilityCheck[] = [
            'material_id' => $materialId,
            'material_name' => getMaterialName($pdo, $materialId),
            'required_quantity' => $requiredQty,
            'available_quantity' => $availableQty,
            'is_sufficient' => $isAvailable
        ];
        
        if (!$isAvailable) {
            error_log("Insufficient material: ID $materialId, required: $requiredQty, available: $availableQty");
        }
    }
    
    // Determine overall availability
    $allAvailable = true;
    $messages = [];
    
    // Check for items without material requirements that exceed limits
    if (!empty($unavailableItems)) {
        $allAvailable = false;
        foreach ($unavailableItems as $unavailable) {
            $messages[] = "Item {$unavailable['item_id']}: {$unavailable['reason']}";
        }
    }
    
    // Check material sufficiency
    foreach ($availabilityCheck as $check) {
        if (!$check['is_sufficient']) {
            $allAvailable = false;
            break;
        }
    }
    
    // Build response message
    $message = $allAvailable ? 'All materials available' : 'Some materials insufficient';
    if (!empty($messages)) {
        $message .= '. Additional issues: ' . implode('; ', $messages);
    }
    
    $response = [
        'success' => true,
        'all_available' => $allAvailable,
        'material_check' => $availabilityCheck,
        'unavailable_items' => $unavailableItems,
        'message' => $message
    ];
    
    error_log("check_material_availability.php - Response: " . json_encode($response));
    echo json_encode($response);
    
} catch (Exception $e) {
    error_log("check_material_availability.php - Error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode([
        'success' => false, 
        'message' => 'Database error: ' . $e->getMessage()
    ]);
}

// Helper function to get materials required for an item
function getItemMaterials($pdo, $itemId) {
    $stmt = $pdo->prepare("
        SELECT rm.mid, rm.quantity, m.mname 
        FROM recipe_materials rm 
        JOIN materials m ON rm.mid = m.mid 
        WHERE rm.item_id = ?
    ");
    $stmt->execute([$itemId]);
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}

// Helper function to get items in a package
function getPackageItems($pdo, $packageId) {
    $stmt = $pdo->prepare("
        SELECT DISTINCT pd.item_id 
        FROM package_dish pd 
        WHERE pd.package_id = ?
    ");
    $stmt->execute([$packageId]);
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}

// Helper function to get current material quantity
function getMaterialQuantity($pdo, $materialId) {
    $stmt = $pdo->prepare("SELECT mqty FROM materials WHERE mid = ?");
    $stmt->execute([$materialId]);
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    return $result ? $result['mqty'] : 0;
}

// Helper function to get material name
function getMaterialName($pdo, $materialId) {
    $stmt = $pdo->prepare("SELECT mname FROM materials WHERE mid = ?");
    $stmt->execute([$materialId]);
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    return $result ? $result['mname'] : 'Unknown';
}
?>