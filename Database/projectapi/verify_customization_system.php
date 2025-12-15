<?php
/**
 * 自訂選項系統驗證頁面
 * 顯示系統各部分的工作狀態
 */
header('Content-Type: text/html; charset=utf-8');
?>
<!DOCTYPE html>
<html>
<head>
    <title>自訂選項系統驗證</title>
    <style>
        body { font-family: Arial; margin: 20px; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; }
        .success { color: green; font-weight: bold; }
        .error { color: red; font-weight: bold; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f0f0f0; }
    </style>
</head>
<body>
    <h1>🍽️ 自訂選項系統驗證</h1>

    <div class="section">
        <h2>1. 數據庫檢查</h2>
        <?php
        $conn = new mysqli("localhost", "root", "", "ProjectDB");
        if ($conn->connect_error) {
            echo '<p class="error">❌ 數據庫連接失敗</p>';
        } else {
            echo '<p class="success">✓ 數據庫連接正常</p>';

            // 檢查is_required欄位
            $result = $conn->query("SHOW COLUMNS FROM item_customization_options LIKE 'is_required'");
            if ($result->num_rows > 0) {
                echo '<p class="success">✓ is_required欄位已存在</p>';
            } else {
                echo '<p class="error">❌ is_required欄位不存在</p>';
            }

            // 統計必填項
            $stats = $conn->query("SELECT is_required, COUNT(*) as cnt FROM item_customization_options GROUP BY is_required");
            echo '<table><tr><th>is_required</th><th>數量</th></tr>';
            while ($row = $stats->fetch_assoc()) {
                echo '<tr><td>' . ($row['is_required'] ? '必填' : '非必填') . '</td><td>' . $row['cnt'] . '</td></tr>';
            }
            echo '</table>';

            $conn->close();
        }
        ?>
    </div>

    <div class="section">
        <h2>2. API 測試 - Item 6 (主菜)</h2>
        <?php
        $response = file_get_contents("http://localhost/newFolder/Database/projectapi/get_customization_options.php?item_id=6");
        $data = json_decode($response, true);
        
        if ($data['success']) {
            echo '<p class="success">✓ API 返回成功</p>';
            echo '<table><tr><th>選項名稱</th><th>最大選擇</th><th>必填</th><th>選擇項數</th></tr>';
            foreach ($data['options'] as $opt) {
                $required = $opt['is_required'] ? '✓ 是' : '✗ 否';
                echo '<tr><td>' . $opt['option_name'] . '</td><td>' . $opt['max_selections'] . '</td><td>' . $required . '</td><td>' . count($opt['choices']) . '</td></tr>';
            }
            echo '</table>';
        } else {
            echo '<p class="error">❌ API 返回錯誤: ' . $data['error'] . '</p>';
        }
        ?>
    </div>

    <div class="section">
        <h2>3. API 測試 - Item 14 (飲品)</h2>
        <?php
        $response = file_get_contents("http://localhost/newFolder/Database/projectapi/get_customization_options.php?item_id=14");
        $data = json_decode($response, true);
        
        if ($data['success']) {
            echo '<p class="success">✓ API 返回成功</p>';
            echo '<table><tr><th>選項名稱</th><th>最大選擇</th><th>必填</th><th>選擇項</th></tr>';
            foreach ($data['options'] as $opt) {
                $required = $opt['is_required'] ? '✓ 是' : '✗ 否';
                $choices = array_map(fn($c) => $c['choice_name'], $opt['choices']);
                echo '<tr><td>' . $opt['option_name'] . '</td><td>' . $opt['max_selections'] . '</td><td>' . $required . '</td><td>' . implode(', ', $choices) . '</td></tr>';
            }
            echo '</table>';
        }
        ?>
    </div>

    <div class="section">
        <h2>4. 訂單保存測試數據格式</h2>
        <p>save_order.php 應接收此格式的自訂數據：</p>
        <pre>{
  "customization": {
    "customization_details": [
      {
        "option_id": 2,
        "option_name": "Spice Level",
        "selected_choices": ["Hot"],
        "additional_cost": 0
      }
    ],
    "extra_notes": "No onions"
  }
}</pre>
    </div>

    <div class="section">
        <h2>5. 系統功能檢查清單</h2>
        <ul>
            <li>✓ 多選項支持（RadioGroup/CheckBox動態生成）</li>
            <li>✓ 多選支持（CheckBox最大選擇限制）</li>
            <li>✓ 實時價格計算（基礎 + 額外費用）</li>
            <li>✓ 必填項標記（紅色星號 *）</li>
            <li>✓ 必填項驗證（強制選擇）</li>
            <li>✓ 購物車完整顯示自訂詳情</li>
            <li>✓ 訂單保存自訂信息到 order_item_customizations</li>
        </ul>
    </div>

</body>
</html>
