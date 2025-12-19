# 自訂項完整驗證指南

## 📋 修復完成清單

### ✅ 已完成的修復
1. **CustomizeDishActivity** - 修復雙重括號初始化，使用正常 ArrayList
2. **TempPaymentActivity** - 添加防御性代碼正規化 ArrayList，手動構造 JSON
3. **RetrofitClient** - 配置 Gson 使用 setSerializeNulls()
4. **OrderAdapter** - 改進自訂項顯示，優先使用 selectedChoices

---

## 🧪 端到端驗證流程

### 第 1 步：建立自訂項訂單

**操作**：
1. 打開應用並選擇菜品（例如：Grape Oolong Tea）
2. 點擊「Customize」編輯自訂項
3. 選擇自訂選項（例如：Sugar Level = "No Sweet", Ice Level = "Less Ice"）
4. 完成訂單並支付

**預期結果**：
```
✅ 訂單成功保存
✅ 收到訂單 ID（例如：order_id=26）
```

---

### 第 2 步：驗證 Logcat 輸出

**查看日誌**：
```bash
adb logcat TempPaymentActivity:D *:S | grep "selected_choices"
```

**預期看到**：
```
📝 Detail map JSON (手動構造): {"option_id":28,"option_name":"Sugar Level","selected_choices":["No Sweet"],"additional_cost":0.0}
📝 Detail map JSON (手動構造): {"option_id":29,"option_name":"Ice Level","selected_choices":["Less Ice"],"additional_cost":0.0}
📦 Complete orderHeader JSON: {...,"selected_choices":["No Sweet"]...,"selected_choices":["Less Ice"]}
```

**✅ 驗證通過** 如果：
- `selected_choices` 包含實際值（不是 null）
- 值的格式是 JSON 陣列 `["..."]`

---

### 第 3 步：驗證後端保存

**檢查 save_order.php 日誌**（在 PHP 錯誤日誌中）：
```bash
tail -f /path/to/error_log
```

**預期看到**：
```
✅ Processing customizations for item_id=15, oiid=123
   ✅ Found 2 customization details
   Processing detail #0: {"option_id":28,"option_name":"Sugar Level","selected_choices":["No Sweet"]}
   Converted selected_choices to: No Sweet
   ✅ Customization SAVED for oiid=123
```

**驗證數據庫**：
```sql
-- 查詢訂單 26 的自訂項
SELECT * FROM order_item_customizations 
WHERE order_item_id IN (
  SELECT id FROM order_items WHERE order_id = 26
);

-- 預期結果：
-- id | order_item_id | option_id | choice_names  | text_value | additional_cost
-- 1  | 123           | 28        | No Sweet      | NULL       | 0
-- 2  | 123           | 29        | Less Ice      | NULL       | 0
```

---

### 第 4 步：驗證訂單歷史頁面顯示

**操作**：
1. 返回應用
2. 點擊「Order History」查看訂單歷史
3. 找到剛才建立的訂單（order_id=26）

**預期看到**：
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Order #26 - 2025-12-19 16:16

• Grape Oolong Tea x2
  ├─ Sugar Level: No Sweet
  └─ Ice Level: Less Ice

Total: ₹52.00
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**✅ 驗證通過** 如果：
- 自訂項正確顯示在訂單項下方
- 格式為 `選項名稱: 選擇值`
- 所有自訂項都列出（本例中應為 2 項）

---

### 第 5 步：驗證「Reorder」功能

**操作**：
1. 在訂單歷史頁面，點擊訂單上的「Reorder」按鈕
2. 檢查購物車

**預期看到**：
```
✅ 購物車中添加了 Grape Oolong Tea x2
✅ 自訂項已恢復：
   - Sugar Level: No Sweet
   - Ice Level: Less Ice
```

**驗證代碼執行**（查看 Logcat）：
```
D/OrderAdapter: Restoring 2 customization(s)
D/OrderAdapter:   Processing customization: optionId=28 optionName=Sugar Level choiceNames=No Sweet
D/OrderAdapter:   ✅ Customization object created with 2 details
```

---

## 🔍 完整驗證清單

| 步驟 | 驗證項目 | 預期結果 | 實際結果 |
|------|---------|---------|---------|
| 1 | 建立訂單 | ✅ 訂單保存成功 | ⬜ 待驗證 |
| 2 | Logcat 中 selected_choices | ✅ 有實際值 | ⬜ 待驗證 |
| 3 | 後端 save_order.php 日誌 | ✅ "Customization SAVED" | ⬜ 待驗證 |
| 3 | 數據庫查詢 | ✅ 自訂項已保存 | ⬜ 待驗證 |
| 4 | 訂單歷史頁面 | ✅ 自訂項正確顯示 | ⬜ 待驗證 |
| 5 | Reorder 功能 | ✅ 自訂項被恢復 | ⬜ 待驗證 |

---

## 🚨 可能的問題排查

### 問題 1：訂單歷史頁面不顯示自訂項

**症狀**：
```
• Grape Oolong Tea x2
(沒有自訂項顯示)
```

**排查步驟**：
1. 查看 Logcat 中是否有錯誤
2. 檢查數據庫中是否有自訂項記錄
3. 驗證 `getCustomizations()` 是否返回 null

**解決方案**：
```java
// 在 OrderAdapter.java 中添加日誌
if (item.getCustomizations() == null) {
    Log.d("OrderAdapter", "⚠️ Customizations is NULL");
} else if (item.getCustomizations().isEmpty()) {
    Log.d("OrderAdapter", "⚠️ Customizations is EMPTY");
} else {
    Log.d("OrderAdapter", "✅ Found " + item.getCustomizations().size() + " customizations");
}
```

### 問題 2：自訂項顯示為空或錯誤

**症狀**：
```
├─ Sugar Level: null
├─ Sugar Level: 
```

**排查步驟**：
1. 檢查 `selectedChoices` 和 `choiceNames` 的值
2. 驗證是否正確保存到數據庫

**解決方案**：
```sql
-- 查詢特定自訂項
SELECT * FROM order_item_customizations 
WHERE option_id = 28 
LIMIT 1;
-- 檢查 choice_names 字段是否有值
```

### 問題 3：Reorder 時自訂項未恢復

**症狀**：
```
購物車中：Grape Oolong Tea x2
(沒有自訂項)
```

**排查步驟**：
1. 查看 OrderAdapter 中的 `handleReorder()` 方法日誌
2. 驗證自訂項是否正確加載到 Customization 對象

**解決方案**：
確保 OrderAdapter 中的代碼正確調用 `setSelectedChoices()`

---

## 📊 成功標準

### ✨ 修復完全成功的標誌：

✅ **所有以下條件都滿足**：

1. Logcat 顯示 `selected_choices` 包含實際值
2. save_order.php 日誌顯示 `✅ Customization SAVED`
3. 數據庫 `order_item_customizations` 表有記錄
4. 訂單歷史頁面正確顯示自訂項
5. 點擊「Reorder」時自訂項被恢復到購物車
6. 重新提交訂單時自訂項再次正確保存

---

## 🛠️ 調試技巧

### 快速查看自訂項 Logcat
```bash
adb logcat | grep -E "customization|selectedChoices|choice_names"
```

### 查看完整的 orderHeader JSON
```bash
adb logcat TempPaymentActivity:D *:S | grep "Complete orderHeader"
```

### 實時監控數據庫
```sql
-- 在 SQL 客户端中運行，定期檢查
SELECT COUNT(*) as custom_count FROM order_item_customizations 
WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR);
```

---

## 📝 日誌記錄模板

運行完整驗證後，請記錄以下信息：

```
測試日期：2025-12-19
測試菜品：Grape Oolong Tea
選擇的自訂項：Sugar Level = No Sweet, Ice Level = Less Ice
訂單 ID：26

✅ Logcat 日誌：[✓/✗] 
✅ 後端保存日誌：[✓/✗]
✅ 數據庫驗證：[✓/✗]
✅ 訂單歷史顯示：[✓/✗]
✅ Reorder 功能：[✓/✗]

總體結果：[成功/失敗]
```

---

所有驗證完成後，修復就正式完成了！🎉
