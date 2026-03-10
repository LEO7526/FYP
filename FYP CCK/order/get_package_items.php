<?php
$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $packageId = $_GET['package_id'] ?? 0;

    $stmt = $pdo->prepare("SELECT pd.item_id, mit.item_name 
                           FROM package_dish pd
                           JOIN menu_item_translation mit ON pd.item_id = mit.item_id
                           WHERE pd.package_id = ? AND mit.language_code = 'en'");
    $stmt->execute([$packageId]);
    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode($items);
} catch (PDOException $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
