<?php
// get_coupon_detail.php
require_once 'db_connect.php';
header('Content-Type: application/json');

$id = $_GET['coupon_id'] ?? 0;
if ($id == 0) { echo json_encode(["status"=>"error", "message"=>"No ID"]); exit(); }

// 1. 基本資料 & 規則
$sql = "SELECT c.*, r.* 
        FROM coupons c 
        JOIN coupon_rules r ON c.coupon_id = r.coupon_id 
        WHERE c.coupon_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id);
$stmt->execute();
$result = $stmt->get_result();
$data = $result->fetch_assoc();

if (!$data) { echo json_encode(["status"=>"error", "message"=>"Not Found"]); exit(); }

// 2. 適用範圍名稱 (Scope Names)
$scopeNames = [];
if ($data['applies_to'] == 'category') {
    $sqlS = "SELECT mc.category_name FROM coupon_applicable_categories cac JOIN menu_category mc ON cac.category_id = mc.category_id WHERE cac.coupon_id = ?";
    $stmtS = $conn->prepare($sqlS); $stmtS->bind_param("i", $id); $stmtS->execute(); $resS = $stmtS->get_result();
    while($row = $resS->fetch_assoc()) $scopeNames[] = $row['category_name'];
} elseif ($data['applies_to'] == 'item') {
    $sqlS = "SELECT mit.item_name FROM coupon_applicable_items cai JOIN menu_item_translation mit ON cai.item_id = mit.item_id WHERE cai.coupon_id = ? AND mit.language_code = 'en'";
    $stmtS = $conn->prepare($sqlS); $stmtS->bind_param("i", $id); $stmtS->execute(); $resS = $stmtS->get_result();
    while($row = $resS->fetch_assoc()) $scopeNames[] = $row['item_name'];
} elseif ($data['applies_to'] == 'package') {
    $sqlS = "SELECT mp.package_name FROM coupon_applicable_package cap JOIN menu_package mp ON cap.package_id = mp.package_id WHERE cap.coupon_id = ?";
    $stmtS = $conn->prepare($sqlS); $stmtS->bind_param("i", $id); $stmtS->execute(); $resS = $stmtS->get_result();
    while($row = $resS->fetch_assoc()) $scopeNames[] = $row['package_name'];
}

// 3. 多語言資料 (標題 & 描述 & 條款)
$trans = [];
$langs = ['en', 'zh-CN', 'zh-TW'];
foreach($langs as $lang) {
    // 標題描述
    $sqlT = "SELECT title, description FROM coupon_translation WHERE coupon_id = ? AND language_code = ?";
    $stmtT = $conn->prepare($sqlT); $stmtT->bind_param("is", $id, $lang); $stmtT->execute(); $resT = $stmtT->get_result();
    $rowT = $resT->fetch_assoc();
    
    // 條款
    $terms = [];
    $sqlTerm = "SELECT term_text FROM coupon_terms WHERE coupon_id = ? AND language_code = ?";
    $stmtTerm = $conn->prepare($sqlTerm); $stmtTerm->bind_param("is", $id, $lang); $stmtTerm->execute(); $resTerm = $stmtTerm->get_result();
    while($rowTerm = $resTerm->fetch_assoc()) $terms[] = $rowTerm['term_text'];

    $trans[$lang] = [
        "title" => $rowT['title'] ?? "",
        "desc" => $rowT['description'] ?? "",
        "terms" => $terms
    ];
}

echo json_encode([
    "status" => "success",
    "data" => [
        "info" => $data,
        "scope_list" => $scopeNames,
        "translations" => $trans
    ]
]);
$conn->close();
?>