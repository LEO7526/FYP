# Logcat 驗證指南 - 自訂項 JSON 序列化修復

## 📋 修復內容總結

### 問題
```
日誌顯示: ✅ Using selectedChoices: [No Sweet]
最終 JSON: {"option_id":28,"option_name":"Sugar Level","selected_choices":null}
         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         selected_choices 變成 null 或消失！
```

### 修復方法
採用**手動 JSON 構造**而不依賴 Gson 的自動序列化，確保 `selected_choices` 一定被正確包含。

---

## 🧪 驗證步驟

### 1️⃣ 重新編譯應用
```bash
cd Android/YummyRestaurant
./gradlew clean build
./gradlew installDebug
```

### 2️⃣ 清除 Logcat 並啟動新進程
```bash
adb logcat -c  # 清除所有日誌
# 然後在應用中執行新的訂單操作
```

### 3️⃣ 篩選關鍵日誌
```bash
adb logcat | grep -E "TempPaymentActivity.*Detail|selected_choices"
```

---

## ✅ 預期看到的日誌

### Phase 1: 構建 JSON
```
D/TempPaymentActivity: Processing customization for item: Grape Oolong Tea, has details: 2
D/TempPaymentActivity: ✅ Using selectedChoices: ["No Sweet"]
D/TempPaymentActivity: ✅ Using selectedChoices: ["Less Ice"]
D/TempPaymentActivity: 📝 Detail map JSON (手動構造): {"option_id":28,"option_name":"Sugar Level","selected_choices":["No Sweet"],"additional_cost":0.0}
D/TempPaymentActivity: 📝 Detail map JSON (手動構造): {"option_id":29,"option_name":"Ice Level","selected_choices":["Less Ice"],"additional_cost":0.0}
```

### Phase 2: 準備發送
```
D/TempPaymentActivity: ✅ Added complete customization structure to item with 2 details
D/TempPaymentActivity: 📤 SENDING TO BACKEND:
D/TempPaymentActivity:    Items count: 1
D/TempPaymentActivity:    Item 0: id=15, qty=3, name=Grape Oolong Tea, has_customization=YES
D/TempPaymentActivity:       ✅ customization_details: 2 items
D/TempPaymentActivity:         Detail 0: {"option_id":28,"option_name":"Sugar Level","selected_choices":["No Sweet"],"additional_cost":0.0}
D/TempPaymentActivity:         Detail 1: {"option_id":29,"option_name":"Ice Level","selected_choices":["Less Ice"],"additional_cost":0.0}
```

### Phase 3: 最終 JSON
```
D/TempPaymentActivity: 📦 Complete orderHeader JSON: {"ostatus":1,...,"items":[{"item_id":15,"customization":{"customization_details":[{"option_id":28,"option_name":"Sugar Level","selected_choices":["No Sweet"],"additional_cost":0.0},{"option_id":29,"option_name":"Ice Level","selected_choices":["Less Ice"],"additional_cost":0.0}]},...}],...}
```

**關鍵檢查點**：
- ✅ `"selected_choices":["No Sweet"]` - 有實際的值，不是 null
- ✅ `"selected_choices":["Less Ice"]` - 第二個自訂項也有值

### Phase 4: 後端響應
```
I/TempPaymentActivity: Order saved directly. Server response: {"success":true,"order_id":23,"debug":{"packages_count":"0","items_count":"1"}}
```

---

## ❌ 如果看到以下日誌，則修復未成功

### Issue 1: selected_choices 仍然是 null
```
📝 Detail map JSON (手動構造): {"option_id":28,"option_name":"Sugar Level","selected_choices":null,"additional_cost":0.0}
                                                                          ^^^^
                                                                          這是問題
```
**解決方案**：檢查 `detail.getSelectedChoices()` 是否真的有值

### Issue 2: selected_choices 根本不在 JSON 中
```
📝 Detail map JSON (手動構造): {"option_id":28,"option_name":"Sugar Level","additional_cost":0.0}
                                                                          ^
                                                                          缺少 selected_choices
```
**解決方案**：檢查是否使用了最新的代碼版本

### Issue 3: ERROR 日誌出現
```
🔥 ERROR: selected_choices NOT in JSON! This is a critical bug!
```
**解決方案**：這表示手動 JSON 構造失敗，檢查字符串連接邏輯

---

## 🗄️ 後端驗證

### save_order.php 日誌應該顯示
```
✅ Processing customizations for item_id=15, oiid=123
   ✅ Found 2 customization details
   Processing detail #0: {"option_id":28,"option_name":"Sugar Level","selected_choices":["No Sweet"],"additional_cost":0.0}
   ✅ Converted selected_choices to: No Sweet
      Detail values: option_id=28, name=Sugar Level, choices=No Sweet, text=, cost=0
      ✅ Customization SAVED for oiid=123
```

### 數據庫驗證
```sql
SELECT * FROM order_item_customizations WHERE order_item_id = 123;
-- 應該返回 2 行，每行有 choice_names 填充
```

---

## 📊 完整驗證流程 (端到端)

```
1. 選擇自訂項 (Sugar Level = No Sweet, Ice Level = Less Ice)
                    ↓
2. 查看 Logcat - 確認 selected_choices 有值
                    ↓
3. 查看後端日誌 - 確認自訂項被保存
                    ↓
4. 重新加載訂單 - 確認自訂項顯示正確
                    ↓
5. 點擊「重新訂購」 - 確認自訂項被恢復
```

---

## 🔧 快速調試命令

### 只看 TempPaymentActivity 的日誌
```bash
adb logcat TempPaymentActivity:D *:S
```

### 看所有關於 "selected_choices" 的日誌
```bash
adb logcat | grep "selected_choices"
```

### 看 JSON 相關的日誌
```bash
adb logcat | grep -E "JSON|Detail map"
```

### 看錯誤相關的日誌
```bash
adb logcat | grep -E "ERROR|🔥"
```

### 實時監控 save_order.php
```bash
# 在 php 文件夾中
tail -f error_log
```

---

## ✨ 成功指標

✅ **修復成功** 當以下所有條件都滿足：

1. Logcat 中看到 `"selected_choices":["..."]` 而不是 `null`
2. save_order.php 記錄 `✅ Customization SAVED`
3. get_orders.php 返回 `customizations > 0` 而不是 `0`
4. 訂單歷史頁面顯示自訂項
5. 點擊「重新訂購」時自訂項被恢復

如果所有這些都出現了，恭喜！🎉 修復已經成功！
