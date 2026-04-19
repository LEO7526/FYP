<?php
$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $packageId = $_GET['package_id'] ?? 0;

    $stmt = $pdo->prepare("
    SELECT 
        pd.item_id, 
        mit.item_name, 
        pd.price_modifier, 
        pd.type_id, 
        mi.image_url, 
        ptt.type_name
    FROM package_dish pd
    JOIN menu_item mi 
        ON pd.item_id = mi.item_id
    JOIN menu_item_translation mit 
        ON pd.item_id = mit.item_id 
        AND mit.language_code = 'en'
    JOIN package_type_translation ptt 
        ON pd.type_id = ptt.type_id 
        AND ptt.type_language_code = 'en'
    WHERE pd.package_id = ?
");
    $stmt->execute([$packageId]);
    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);


    $stmt = $pdo->prepare("SELECT type_id, optional_quantity FROM package_type WHERE package_id = ?");
    $stmt->execute([$packageId]);
    $typeLimits = $stmt->fetchAll(PDO::FETCH_ASSOC);


// 一起輸出
    echo json_encode([
        "items" => $items,
        "limits" => $typeLimits
    ]);

} catch (PDOException $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
