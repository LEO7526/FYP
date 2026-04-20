<?php
// create_dish.php (支援圖片上傳版)
require_once 'db_connect.php';
header('Content-Type: application/json');

$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (!$data) {
    echo json_encode(["status" => "error", "message" => "Invalid JSON"]);
    exit();
}

$conn->begin_transaction();

try {
    // === 圖片處理邏輯 ===
    $image_url = "https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/1.jpg"; // 預設圖
    
    if (!empty($data['image_data'])) {
        // 1. 取得 Base64 字串
        $base64_string = $data['image_data'];
        
        // 2. 產生唯一檔名
        $file_name = "dish_" . time() . "_" . uniqid() . ".jpg";
        $file_path = "uploads/" . $file_name;
        
        // 3. 解碼並存檔
        $ifp = fopen($file_path, 'wb'); 
        fwrite($ifp, base64_decode($base64_string)); 
        fclose($ifp); 
        
        // 4. 產生完整的 URL (供 APP 讀取)
        // 記得改成你實際的 IP 和路徑
        $server_ip = "http://10.0.2.2/androidstaff_api/"; 
        $image_url = $server_ip . $file_path;
    }
    // ===================

    // 1. 插入 menu_item
    $cat_id = $data['category_id'];
    $price = $data['price'];
    $spice = $data['spice_level'];
    $avail = $data['is_available'] ? 1 : 0;

    $stmt = $conn->prepare("INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES (?, ?, ?, ?, ?)");
    $stmt->bind_param("idsii", $cat_id, $price, $image_url, $spice, $avail);
    $stmt->execute();
    $item_id = $conn->insert_id;
    $stmt->close();

    // 2. 插入翻譯
    $languages = ['en', 'zh-CN', 'zh-TW'];
    $stmtTrans = $conn->prepare("INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES (?, ?, ?, ?)");
    
    foreach ($languages as $lang) {
        if (isset($data['translations'][$lang])) {
            $t = $data['translations'][$lang];
            $name = $t['name'];
            $desc = $t['description'];
            if (!empty($name)) {
                $stmtTrans->bind_param("isss", $item_id, $lang, $name, $desc);
                $stmtTrans->execute();
            }
        }
    }
    $stmtTrans->close();

    // 3. 插入食譜
    if (!empty($data['recipe']) && is_array($data['recipe'])) {
        $stmtRecipe = $conn->prepare("INSERT INTO recipe_materials (item_id, mid, quantity) VALUES (?, ?, ?)");
        foreach ($data['recipe'] as $r) {
            $mid = $r['mid'];
            $qty = $r['quantity'];
            if ($mid > 0 && $qty > 0) {
                $stmtRecipe->bind_param("iid", $item_id, $mid, $qty);
                $stmtRecipe->execute();
            }
        }
        $stmtRecipe->close();
    }

    $conn->commit();
    echo json_encode(["status" => "success", "message" => "Dish Created with Image!"]);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["status" => "error", "message" => "DB Error: " . $e->getMessage()]);
}

$conn->close();
?>