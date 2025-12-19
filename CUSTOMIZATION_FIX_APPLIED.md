# ✅ 自訂功能修復已應用

## 問題根源
**日誌發現**：PHP 錯誤 - `Unknown column 'oiid' in 'field list'`

### 根本原因
在 `save_order.php` 中，INSERT 語句使用了不存在的欄位 `oiid`：

```php
// ❌ 錯誤的代碼
INSERT INTO order_item_customizations 
(oid, item_id, oiid, option_id, option_name, choice_names, text_value, additional_cost)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
```

但實際資料庫表結構（在 `createProjectDB_4.4.sql` 中）是：

```sql
CREATE TABLE order_item_customizations (
  customization_id INT,
  oid INT,           -- 訂單 ID
  item_id INT,       -- 項目 ID
  option_id INT,     -- 選項 ID（不是 oiid！）
  option_name VARCHAR(255),
  choice_ids JSON,
  choice_names JSON, -- 選擇名稱
  text_value VARCHAR(500),
  additional_cost DECIMAL(10,2),
  ...
)
```

### 修復內容

**文件**: [Database/projectapi/save_order.php](Database/projectapi/save_order.php#L220-L230)

**變更**:
1. ❌ 移除了不存在的 `oiid` 欄位
2. ✅ 修正 INSERT 語句只使用實際存在的欄位
3. ✅ 修正 `bind_param` 類型字符串
   - 之前：`"iiiissd"` （8 個參數）
   - 現在：`"iiissd"` （7 個參數）
4. ✅ 修正了邏輯錯誤：execute() 成功時記錄成功

**修復前**:
```php
$customStmt = $conn->prepare("
    INSERT INTO order_item_customizations 
    (oid, item_id, oiid, option_id, option_name, choice_names, text_value, additional_cost)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
");

$customStmt->bind_param("iiiissd", 
    $order_id, $item_id, $oiid, $option_id, 
    $option_name, $choice_names, $text_value, $additional_cost
);
```

**修復後**:
```php
$customStmt = $conn->prepare("
    INSERT INTO order_item_customizations 
    (oid, item_id, option_id, option_name, choice_names, text_value, additional_cost)
    VALUES (?, ?, ?, ?, ?, ?, ?)
");

$customStmt->bind_param("iiissd", 
    $order_id, $item_id, $option_id, 
    $option_name, $choice_names, $text_value, $additional_cost
);
```

## 修復流程總結

### Phase 1: 客戶端修復 ✅
- CustomizeDishActivity: 修正 ArrayList 初始化（移除匿名類）
- TempPaymentActivity: 添加 ArrayList 正規化 + 手動 JSON 構造
- RetrofitClient: 增強 Gson 配置（serializeNulls）

### Phase 2: 後端增強 ✅
- save_order.php: 添加詳細的 selected_choices 處理

### Phase 3: 根本原因發現 ✅
- 定位日誌：C:\xampp\apache\logs\error.log
- 識別 SQL 錯誤：`Unknown column 'oiid'`
- 修正 INSERT 語句和 bind_param

## 驗證步驟

### 1. 在 Android 應用中:
- 選擇飲品：Grape Oolong Tea
- 選擇自訂選項：Sugar Level = "No Sweet", Ice Level = "No Ice"
- 提交訂單

### 2. 檢查 PHP 日誌:
```bash
Get-Content -Path "C:\xampp\apache\logs\error.log" -Tail 50
```

**應該看到**:
```
✅ Customization SAVED: item=15, option=Sugar Level, choices=No Sweet, cost=0
✅ Customization SAVED: item=15, option=Ice Level, choices=No Ice, cost=0
```

NOT:
```
❌ Prepare failed for customization: Unknown column 'oiid' in 'field list'
```

### 3. 在 MySQL 中驗證:
```sql
SELECT * FROM order_item_customizations 
WHERE oid = <最新訂單 ID> 
ORDER BY customization_id DESC;
```

應該顯示類似：
```
| customization_id | oid | item_id | option_id | option_name | choice_names | text_value | additional_cost |
|---|---|---|---|---|---|---|---|
| 123 | 27 | 15 | 28 | Sugar Level | "No Sweet" | NULL | 0 |
| 124 | 27 | 15 | 29 | Ice Level | "No Ice" | NULL | 0 |
```

### 4. 在訂單歷史中驗證:
- 打開 OrderHistory
- 應該看到自訂功能顯示（不再是 `customizations=0`）
- 應該顯示："Sugar Level: No Sweet, Ice Level: No Ice"

## 技術細節

### 資料庫表結構 vs 代碼對比

**表有的欄位**:
- oid ✅
- item_id ✅
- option_id ✅
- option_name ✅
- choice_names ✅
- text_value ✅
- additional_cost ✅

**表沒有的欄位**:
- ~~oiid~~ ❌

### 為什麼之前出現 `customizations=0`

1. ✅ 客戶端：selected_choices 正確序列化為 JSON
2. ✅ PHP 接收：received JSON 包含 selected_choices 陣列
3. ❌ SQL 插入失敗：因為欄位 `oiid` 不存在
4. ❌ 自訂未保存：insert 失敗，customizations 表為空
5. ❌ 查詢返回 0：`SELECT COUNT(*) FROM order_item_customizations` 返回 0

## 完整修復清單

| 項目 | 文件 | 問題 | 修復 | 狀態 |
|---|---|---|---|---|
| 1 | CustomizeDishActivity | 雙括號 ArrayList 創建匿名類 | 使用正常 ArrayList | ✅ |
| 2 | TempPaymentActivity | Gson 無法序列化匿名類 | ArrayList 正規化 + 手動 JSON | ✅ |
| 3 | RetrofitClient | Gson 配置不完整 | 添加 setSerializeNulls() | ✅ |
| 4 | save_order.php | 日誌不詳細 | 添加 selected_choices 檢查 | ✅ |
| 5 | save_order.php | SQL 錯誤: oiid 欄位不存在 | 移除 oiid，修正 bind_param | ✅ |

## 下一步

1. **立即測試**：在 Android 應用中提交新訂單（帶自訂選項）
2. **檢查日誌**：確認沒有 "Unknown column" 錯誤，看到 "✅ Customization SAVED"
3. **驗證資料庫**：查詢 order_item_customizations 表
4. **測試 UI**：在 OrderHistory 中查看自訂功能是否顯示
5. **重新訂購**：測試從 OrderHistory 重新訂購是否正確恢復自訂

---

**修復時間**: 2025-12-19 16:30+
**修復狀態**: ✅ 完成
**下次測試**: 立即
