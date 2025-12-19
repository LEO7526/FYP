# OrderAdapter 日誌快速參考

## 📊 日誌統計
- **總日誌點數**: 47
- **Debug 級別**: 43
- **Error 級別**: 2
- **日誌標籤**: `OrderAdapter`

---

## 🎯 主要日誌類別

### 1️⃣ 訂單綁定流程 (5 個日誌點)
```
Line 65: === BINDING ORDER #X at position Y ===
Line 66: Packages: X
Line 67: Items: X
Line 76: Processing X regular items (no packages)
Line 120: Skipping X items display - order has packages
```

### 2️⃣ 項目處理流程 (8 個日誌點)
```
Line 81-83: Item: {name} qty={qty} price={price} customizations={count}
Line 95: Found X customizations
Line 103: Customization details display
Line 125: Displaying X packages
```

### 3️⃣ 訂單統計 (4 個日誌點)
```
Line 226: Total item count: X
Line 230: Total amount: HK$XXX.XX
Line 235: Order date: {date} ({timeAgo})
Line 239: Order status: {status}
```

### 4️⃣ 按鈕互動 (3 個日誌點)
```
Line 246: 🔄 Reorder clicked for order #X
Line 251: ℹ️ Details clicked for order #X
Line 252: Details clicked for order X
```

### 5️⃣ 重新訂購流程 - 套餐 (5 個日誌點)
```
Line 343: === HANDLING REORDER FOR ORDER #X ===
Line 348: This is a PACKAGE order with X package(s)
Line 350: Package: id={id} count={count}
Line 363: Prefill item: {name} (id={id})
Line 367: Storing X items in CartManager for prefill
Line 374: Navigating to BuildSetMenuActivity with package_id=X
```

### 6️⃣ 重新訂購流程 - 常規訂單 (10 個日誌點)
```
Line 380: This is a REGULAR order with X item(s)
Line 382: Cart cleared
Line 392-394: Processing item: {name} qty={qty} customizations={count}
Line 401: Restoring X customization(s)
Line 404-407: Processing customization: optionId={id} optionName={name} choiceNames={choices} cost={cost}
Line 422: ✅ Converted choiceNames to selectedChoices: [...]
Line 424: ❌ No choiceNames found
Line 430: 📝 TextValue set: {value}
Line 437: ✅ Customization object created with X details
Line 439: No customizations to restore
Line 445: ✅ CartItem added: {name} x{qty}
```

### 7️⃣ 完成和導航 (3 個日誌點)
```
Line 449: ✅ ALL ITEMS RESTORED TO CART - Ready for reorder
Line 452: 🔄 Navigating to CartActivity...
Line 455: ✅ CartActivity started
```

### 8️⃣ 錯誤處理 (2 個日誌點)
```
Line 457: ❌ No items found in order
Line 461-462: ❌ Error reordering order #X: {message} + Stack trace
```

### 9️⃣ 訂單詳情顯示 (7 個日誌點)
```
Line 469: ℹ️ SHOWING DETAILS FOR ORDER #X
Line 470: Order Date: {date}, Status: {status}, Items: {count}
Line 508: Processing X items for details display
Line 513-516: Item: {name} qty={qty} customizations={count}
Line 530: Displaying X customizations
Line 536: Special note: {text}
Line 540: Option: {optionName} = {choiceNames}
```

---

## 🔍 在 Logcat 中使用

### 過濾所有日誌
```
tag:OrderAdapter
```

### 過濾特定功能

**訂單列表顯示**:
```
tag:OrderAdapter BINDING
```

**重新訂購流程**:
```
tag:OrderAdapter HANDLING REORDER
```

**訂單詳情**:
```
tag:OrderAdapter SHOWING DETAILS
```

**錯誤**:
```
tag:OrderAdapter Error
```

### 進階過濾

**只看自訂項相關**:
```
tag:OrderAdapter customization
```

**只看成功操作**:
```
tag:OrderAdapter ✅
```

**只看失敗/錯誤**:
```
tag:OrderAdapter (❌|Error|error)
```

**只看導航操作**:
```
tag:OrderAdapter 🔄
```

---

## 📋 完整示例流程

### 場景 1: 用戶打開訂單歷史頁面

**預期日誌順序**:
```
D/OrderAdapter: === BINDING ORDER #1001 at position 0 ===
D/OrderAdapter: Packages: null
D/OrderAdapter: Items: 2
D/OrderAdapter: Processing 2 regular items (no packages)
D/OrderAdapter:   Item: Iced Latte qty=2 price=HK$35.00 customizations=2
D/OrderAdapter:     Found 2 customizations
D/OrderAdapter:       - Sugar Level: More Sweet
D/OrderAdapter:       - Ice Level: More Ice
D/OrderAdapter:   Item: Coffee Combo qty=1 price=HK$45.00 customizations=1
D/OrderAdapter:     Found 1 customizations
D/OrderAdapter:       - Cup Size: Large
D/OrderAdapter:   Total item count: 3
D/OrderAdapter:   Total amount: HK$115.00
D/OrderAdapter:   Order date: 2024-01-15 (2 days ago)
D/OrderAdapter:   Order status: completed
```

### 場景 2: 用戶點擊 Reorder

**預期日誌順序**:
```
D/OrderAdapter: 🔄 Reorder clicked for order #1001
D/OrderAdapter: === HANDLING REORDER FOR ORDER #1001 ===
D/OrderAdapter: This is a REGULAR order with 2 item(s)
D/OrderAdapter: Cart cleared
D/OrderAdapter: Processing item: Iced Latte qty=2 customizations=2
D/OrderAdapter:   Restoring 2 customization(s)
D/OrderAdapter:     Processing customization: optionId=5 optionName=Sugar Level choiceNames=More Sweet cost=0.00
D/OrderAdapter:       ✅ Converted choiceNames to selectedChoices: [More Sweet]
D/OrderAdapter:     Processing customization: optionId=6 optionName=Ice Level choiceNames=More Ice cost=0.00
D/OrderAdapter:       ✅ Converted choiceNames to selectedChoices: [More Ice]
D/OrderAdapter:     ✅ Customization object created with 2 details
D/OrderAdapter:     ✅ CartItem added: Iced Latte x2
D/OrderAdapter: Processing item: Coffee Combo qty=1 customizations=1
D/OrderAdapter:   Restoring 1 customization(s)
D/OrderAdapter:     Processing customization: optionId=7 optionName=Cup Size choiceNames=Large cost=0.00
D/OrderAdapter:       ✅ Converted choiceNames to selectedChoices: [Large]
D/OrderAdapter:     ✅ Customization object created with 1 details
D/OrderAdapter:     ✅ CartItem added: Coffee Combo x1
D/OrderAdapter: ✅ ALL ITEMS RESTORED TO CART - Ready for reorder
D/OrderAdapter: 🔄 Navigating to CartActivity...
D/OrderAdapter: ✅ CartActivity started
```

### 場景 3: 用戶點擊 Details

**預期日誌順序**:
```
D/OrderAdapter: ℹ️ Details clicked for order #1001
D/OrderAdapter: Details clicked for order 1001
D/OrderAdapter: ℹ️ SHOWING DETAILS FOR ORDER #1001
D/OrderAdapter:   Order Date: 2024-01-15, Status: completed, Items: 2
D/OrderAdapter:   Processing 2 items for details display
D/OrderAdapter:     Item: Iced Latte qty=2 customizations=2
D/OrderAdapter:       Displaying 2 customizations
D/OrderAdapter:         Option: Sugar Level = More Sweet
D/OrderAdapter:         Option: Ice Level = More Ice
D/OrderAdapter:     Item: Coffee Combo qty=1 customizations=1
D/OrderAdapter:       Displaying 1 customizations
D/OrderAdapter:         Option: Cup Size = Large
```

---

## ⚠️ 故障排除

### 症狀: 自訂項未顯示

**檢查這些日誌**:
```
Line 95: Found X customizations  // 應該 > 0
Line 401: Restoring X customization(s)  // 應該 > 0
Line 437: Customization object created  // 應該出現
```

**可能原因**:
- [ ] API 未返回 customizations 數據
- [ ] OrderItem.customizations 為 null
- [ ] 數據庫中無自訂項記錄

---

### 症狀: 重新訂購失敗

**檢查這些日誌**:
```
Line 461-462: ❌ Error reordering...  // 查看錯誤信息
Line 457: ❌ No items found in order
```

**可能原因**:
- [ ] 訂單沒有項目
- [ ] CartManager 添加失敗
- [ ] CartActivity 啟動失敗

---

### 症狀: 數量計算錯誤

**檢查這些日誌**:
```
Line 226: Total item count: X  // 驗證計算是否正確
```

**驗證**:
- 計算應該 = 所有項目的 qty 總和
- 例如: 2 + 1 = 3，而不是項目數 = 2

---

## 🛠️ 日誌級別說明

| 級別 | 用途 | 例子 |
|------|------|------|
| `D` (Debug) | 普通流程追蹤 | 項目處理、自訂項恢復 |
| `E` (Error) | 異常情況 | 重新訂購失敗、堆棧跟蹤 |

---

## 📱 從 Android Studio 導出日誌

1. 打開 Logcat（View → Tool Windows → Logcat）
2. 設置過濾: `tag:OrderAdapter`
3. 右鍵點擊日誌區域
4. 選擇 "Export"
5. 保存為 `.txt` 文件進行分析

---

## 🎓 日誌符號速查表

| 符號 | 含義 |
|------|------|
| `===` | 主要操作分隔線 |
| `✅` | 成功 |
| `❌` | 失敗/缺失 |
| `🔄` | 導航/重新訂購 |
| `ℹ️` | 信息/詳情 |
| `📝` | 文本/特殊要求 |
| `├─` | 列表中間項 |
| `└─` | 列表末項 |

---

**最後更新**: 2024 年
**版本**: 1.0
**適用於**: YummyRestaurant OrderAdapter
