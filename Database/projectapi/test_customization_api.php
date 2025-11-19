<?php
/**
 * 測試自訂選項 API 和資料庫
 * 訪問此檔案以檢查系統狀態
 */
header('Content-Type: text/html; charset=utf-8');

echo "<h1>自訂選項系統診斷</h1>";
echo "<hr>";

// ====================================
// 1. 資料庫連接測試
// ====================================
echo "<h2>1️⃣ 資料庫連接測試</h2>";
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    echo "❌ <span style='color:red'>資料庫連接失敗: " . $conn->connect_error . "</span><br>";
    exit;
} else {
    echo "✅ <span style='color:green'>資料庫連接成功</span><br>";
}

// ====================================
// 2. 檢查表是否存在
// ====================================
echo "<h2>2️⃣ 檢查表是否存在</h2>";
$tables = ['item_customization_options', 'customization_option_choices', 'order_item_customizations'];
foreach ($tables as $table) {
    $result = $conn->query("SHOW TABLES LIKE '$table'");
    if ($result && $result->num_rows > 0) {
        echo "✅ <span style='color:green'>表 `$table` 存在</span><br>";
    } else {
        echo "❌ <span style='color:red'>表 `$table` 不存在！</span><br>";
    }
}

// ====================================
// 3. 檢查資料數量
// ====================================
echo "<h2>3️⃣ 檢查資料數量</h2>";
$countQuery1 = $conn->query("SELECT COUNT(*) as cnt FROM item_customization_options");
$count1 = $countQuery1 ? $countQuery1->fetch_assoc()['cnt'] : 0;
echo "📊 item_customization_options: <strong>$count1</strong> 條記錄<br>";

$countQuery2 = $conn->query("SELECT COUNT(*) as cnt FROM customization_option_choices");
$count2 = $countQuery2 ? $countQuery2->fetch_assoc()['cnt'] : 0;
echo "📊 customization_option_choices: <strong>$count2</strong> 條記錄<br>";

if ($count1 == 0) {
    echo "<p style='color:orange'>⚠️ <strong>警告：沒有自訂選項數據！</strong><br>";
    echo "請執行 <code>createProjectDB_4.3.sql</code> 或手動插入數據。</p>";
}

// ====================================
// 4. 顯示現有的自訂選項
// ====================================
echo "<h2>4️⃣ 現有的自訂選項（前10條）</h2>";
$optionsQuery = $conn->query("
    SELECT 
        ico.option_id,
        ico.item_id,
        ico.option_name,
        ico.option_type,
        ico.is_required,
        COUNT(coc.choice_id) as num_choices
    FROM item_customization_options ico
    LEFT JOIN customization_option_choices coc ON ico.option_id = coc.option_id
    GROUP BY ico.option_id
    ORDER BY ico.item_id
    LIMIT 10
");

if ($optionsQuery && $optionsQuery->num_rows > 0) {
    echo "<table border='1' cellpadding='5' style='border-collapse:collapse;'>";
    echo "<tr><th>option_id</th><th>item_id</th><th>option_name</th><th>option_type</th><th>is_required</th><th>選擇項數量</th></tr>";
    while ($row = $optionsQuery->fetch_assoc()) {
        echo "<tr>";
        echo "<td>{$row['option_id']}</td>";
        echo "<td>{$row['item_id']}</td>";
        echo "<td>{$row['option_name']}</td>";
        echo "<td>{$row['option_type']}</td>";
        echo "<td>" . ($row['is_required'] ? 'Yes' : 'No') . "</td>";
        echo "<td>{$row['num_choices']}</td>";
        echo "</tr>";
    }
    echo "</table>";
} else {
    echo "<p style='color:red'>❌ 沒有找到任何自訂選項</p>";
}

// ====================================
// 5. 測試 API 端點（麻婆豆腐 item_id=6）
// ====================================
echo "<h2>5️⃣ 測試 API 端點（item_id=6）</h2>";
$testItemId = 6;

$stmt = $conn->prepare("
    SELECT option_id, item_id, option_name, option_type, is_required, max_selections
    FROM item_customization_options
    WHERE item_id = ?
    ORDER BY option_id
");
$stmt->bind_param("i", $testItemId);
$stmt->execute();
$result = $stmt->get_result();

$options = [];
while ($row = $result->fetch_assoc()) {
    $option_id = $row['option_id'];
    
    $choiceStmt = $conn->prepare("
        SELECT choice_id, choice_name, additional_cost
        FROM customization_option_choices
        WHERE option_id = ?
        ORDER BY display_order, choice_id
    ");
    $choiceStmt->bind_param("i", $option_id);
    $choiceStmt->execute();
    $choiceResult = $choiceStmt->get_result();
    
    $choices = [];
    while ($choiceRow = $choiceResult->fetch_assoc()) {
        $choices[] = [
            "choice_id" => intval($choiceRow['choice_id']),
            "choice_name" => $choiceRow['choice_name'],
            "additional_cost" => floatval($choiceRow['additional_cost'])
        ];
    }
    $choiceStmt->close();
    
    $options[] = [
        "option_id" => intval($row['option_id']),
        "item_id" => intval($row['item_id']),
        "option_name" => $row['option_name'],
        "option_type" => $row['option_type'],
        "is_required" => intval($row['is_required']),
        "max_selections" => intval($row['max_selections']),
        "choices" => $choices
    ];
}
$stmt->close();

$apiResponse = [
    "success" => true,
    "options" => $options
];

echo "<h3>API 回應（JSON）：</h3>";
echo "<pre style='background:#f4f4f4; padding:10px; overflow:auto;'>";
echo json_encode($apiResponse, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
echo "</pre>";

if (empty($options)) {
    echo "<p style='color:orange'>⚠️ <strong>item_id=6 沒有自訂選項！</strong><br>";
    echo "請執行以下 SQL：</p>";
    echo "<pre style='background:#fff3cd; padding:10px; overflow:auto;'>";
    echo "INSERT INTO item_customization_options (item_id, option_name, option_type, is_required, max_selections)\n";
    echo "VALUES (6, 'Spice Level', 'single_choice', 1, 1);\n\n";
    echo "SET @spice_option_id = LAST_INSERT_ID();\n\n";
    echo "INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)\n";
    echo "VALUES \n";
    echo "(@spice_option_id, 'Mild', 0, 1),\n";
    echo "(@spice_option_id, 'Medium', 0, 2),\n";
    echo "(@spice_option_id, 'Hot', 0, 3),\n";
    echo "(@spice_option_id, 'Numbing', 0, 4);\n";
    echo "</pre>";
}

// ====================================
// 6. 直接訪問測試連結
// ====================================
echo "<h2>6️⃣ 測試連結</h2>";
$protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https" : "http";
$host = $_SERVER['HTTP_HOST'];
$basePath = dirname($_SERVER['PHP_SELF']);
$apiUrl = "$protocol://$host$basePath/get_customization_options.php?item_id=6";

echo "<p>點擊以下連結測試 API：</p>";
echo "<a href='$apiUrl' target='_blank' style='color:blue; text-decoration:underline;'>$apiUrl</a>";

echo "<hr>";
echo "<p style='color:gray; font-size:12px;'>測試完成於 " . date('Y-m-d H:i:s') . "</p>";

$conn->close();
?>
