# 訂單歷史頁面詳細日誌追蹤總結

## 概述
已為 OrderAdapter 添加全面的調試日誌，用於追蹤訂單數據流、自訂項恢復和重新訂購功能。所有日誌使用標籤 `OrderAdapter` 便於在 Logcat 中過濾。

---

## 主要日誌點

### 1. 訂單綁定 (onBindViewHolder)

```
=== BINDING ORDER #X at position Y ===
```

**作用**：每次 RecyclerView 將訂單綁定到視圖時的頭部標記

**日誌位置**：順序歷史列表中顯示的每個訂單

**範例**：
```
D/OrderAdapter: === BINDING ORDER #1001 at position 0 ===
```

---

### 2. 項目處理日誌 (Item Detail Extraction)

```
Item: {name} qty={qty} price=HK${price} customizations={count}
  Option: {optionName} = {choiceNames}
  └─ Additional Cost: HK${cost}
```

**作用**：記錄訂單中每個項目的詳細信息

**包含內容**：
- 項目名稱
- 數量
- 單價
- 自訂項數量
- 每個自訂選項的詳細信息

**範例**：
```
D/OrderAdapter:   Item: Iced Latte qty=2 price=HK$35.00 customizations=2
D/OrderAdapter:     Option: Sugar Level = More Sweet
D/OrderAdapter:     Option: Ice Level = More Ice
```

---

### 3. 套餐項目日誌 (Package Items Processing)

```
Package Item: {name} qty={qty}
  Customizations: {count}
    Option: {optionName} = {choiceNames}
```

**作用**：記錄套餐訂單中的每個項目

**包含內容**：
- 套餐項目名稱
- 數量
- 自訂項詳細信息

**範例**：
```
D/OrderAdapter:   Package Item: Coffee Combo qty=1
D/OrderAdapter:     Customizations: 1
D/OrderAdapter:       Option: Cup Size = Large
```

---

### 4. 統計信息日誌 (Summary Calculation)

```
Total item count: X
Total amount: HK$XXX.XX
Order Status: {status}
Order Date: {date} ({timeAgo})
```

**作用**：記錄訂單的總體統計信息

**包含內容**：
- 總項目數（按數量計算）
- 訂單總額
- 訂單狀態
- 訂單日期和時間

**範例**：
```
D/OrderAdapter:   Total item count: 6
D/OrderAdapter:   Total amount: HK$245.50
D/OrderAdapter:   Order Status: Completed
D/OrderAdapter:   Order Date: 2024-01-15 (2 days ago)
```

---

### 5. 按鈕點擊日誌 (Button Interaction)

#### Reorder 按鈕
```
🔄 REORDER BUTTON CLICKED for order #{oid}
```

#### Details 按鈕
```
ℹ️ DETAILS BUTTON CLICKED for order #{oid}
```

**作用**：追蹤用戶與訂單卡片的交互

**範例**：
```
D/OrderAdapter: 🔄 REORDER BUTTON CLICKED for order #1001
D/OrderAdapter: ℹ️ DETAILS BUTTON CLICKED for order #1001
```

---

### 6. 重新訂購流程 (Reorder Flow)

#### 初始化
```
=== HANDLING REORDER FOR ORDER #{oid} ===
```

#### 套餐訂單路徑
```
This is a PACKAGE order
Prefill items count: X
Prefill item #{prefillId}: {count} items
✅ Prefill data stored: {count} items
🔄 Navigating to BuildSetMenuActivity...
✅ BuildSetMenuActivity started
```

#### 常規訂單路徑
```
This is a REGULAR order with X items
Processing item #{itemCount}
  Item: {name} qty={qty} customizations={count}
```

**範例**：
```
D/OrderAdapter: === HANDLING REORDER FOR ORDER #1001 ===
D/OrderAdapter: This is a REGULAR order with 2 items
D/OrderAdapter: Processing item #1
D/OrderAdapter:   Item: Iced Latte qty=2 customizations=2
```

---

### 7. 自訂項恢復 (Customization Restoration)

```
Restoring {count} customization(s)
  Processing customization: optionId={id} optionName={name} choiceNames={choices} cost={cost}
    ✅ Converted choiceNames to selectedChoices: [choice1, choice2, ...]
    📝 TextValue set: {value}
  ✅ Customization object created with {count} details
  ✅ CartItem added: {name} x{qty}
```

**作用**：詳細追蹤自訂項從數據庫恢復到購物車的過程

**包含內容**：
- 自訂項總數
- 每個自訂選項的轉換過程
- 選擇轉換（逗號分隔字符串 → List）
- 特殊要求文本值

**範例**：
```
D/OrderAdapter:   Restoring 2 customization(s)
D/OrderAdapter:     Processing customization: optionId=5 optionName=Sugar Level choiceNames=More Sweet cost=0.00
D/OrderAdapter:       ✅ Converted choiceNames to selectedChoices: [More Sweet]
D/OrderAdapter:     Processing customization: optionId=6 optionName=Ice Level choiceNames=More Ice cost=0.00
D/OrderAdapter:       ✅ Converted choiceNames to selectedChoices: [More Ice]
D/OrderAdapter:     ✅ Customization object created with 2 details
D/OrderAdapter:     ✅ CartItem added: Iced Latte x2
```

---

### 8. 完成和導航 (Completion & Navigation)

```
✅ ALL ITEMS RESTORED TO CART - Ready for reorder
🔄 Navigating to CartActivity...
✅ CartActivity started
```

**作用**：確認重新訂購流程成功完成

**範例**：
```
D/OrderAdapter: ✅ ALL ITEMS RESTORED TO CART - Ready for reorder
D/OrderAdapter: 🔄 Navigating to CartActivity...
D/OrderAdapter: ✅ CartActivity started
```

---

### 9. 訂單詳情顯示 (Order Details Dialog)

```
ℹ️ SHOWING DETAILS FOR ORDER #{oid}
  Order Date: {date}, Status: {status}, Items: {count}
  Processing {count} items for details display
    Item: {name} qty={qty} customizations={count}
      Displaying {count} customizations
        Special note: {text}
        Option: {optionName} = {choiceNames}
```

**作用**：追蹤訂單詳情對話框的構建過程

**包含內容**：
- 訂單基本信息
- 項目數量
- 每個項目的自訂信息

**範例**：
```
D/OrderAdapter: ℹ️ SHOWING DETAILS FOR ORDER #1001
D/OrderAdapter:   Order Date: 2024-01-15, Status: completed, Items: 2
D/OrderAdapter:   Processing 2 items for details display
D/OrderAdapter:     Item: Iced Latte qty=2 customizations=2
D/OrderAdapter:       Displaying 2 customizations
D/OrderAdapter:         Option: Sugar Level = More Sweet
```

---

### 10. 錯誤處理 (Error Handling)

```
❌ No customizations to restore
❌ No items found in order
❌ Error reordering order #{oid}: {error message}
Stack trace: {full stack trace}
```

**作用**：記錄處理過程中的錯誤和異常

**包含內容**：
- 缺失的自訂項
- 空訂單
- 異常和堆棧跟蹤

**範例**：
```
D/OrderAdapter: ❌ No items found in order
E/OrderAdapter: ❌ Error reordering order #1001: NullPointerException
E/OrderAdapter: Stack trace: java.lang.NullPointerException...
```

---

## 在 Logcat 中使用這些日誌

### 過濾日誌
```
1. 開啟 Android Studio Logcat
2. 在過濾框中輸入: tag:OrderAdapter
3. 檢視所有 OrderAdapter 相關日誌
```

### 按優先級過濾
- **Debug**: `D/OrderAdapter` - 詳細流程信息
- **Error**: `E/OrderAdapter` - 異常和錯誤

### 追蹤完整流程

#### 瀏覽訂單歷史
```
=== BINDING ORDER #1001 at position 0 ===
  Item: Iced Latte qty=2 customizations=2
  Item: Coffee Combo qty=1 customizations=1
  Total item count: 4
  Order Status: Completed
```

#### 點擊 Reorder
```
🔄 REORDER BUTTON CLICKED for order #1001
=== HANDLING REORDER FOR ORDER #1001 ===
This is a REGULAR order with 2 items
Processing item #1
  Item: Iced Latte qty=2 customizations=2
  Restoring 2 customization(s)
    Processing customization: optionId=5 ...
    ✅ Converted choiceNames to selectedChoices: [More Sweet]
    ✅ CartItem added: Iced Latte x2
✅ ALL ITEMS RESTORED TO CART - Ready for reorder
🔄 Navigating to CartActivity...
✅ CartActivity started
```

#### 點擊 Details
```
ℹ️ DETAILS BUTTON CLICKED for order #1001
ℹ️ SHOWING DETAILS FOR ORDER #1001
  Order Date: 2024-01-15, Status: completed, Items: 2
  Processing 2 items for details display
    Item: Iced Latte qty=2 customizations=2
      Displaying 2 customizations
        Option: Sugar Level = More Sweet
```

---

## 日誌符號含義

| 符號 | 含義 |
|------|------|
| `===` | 主要操作的開始/分隔 |
| `✅` | 成功的操作 |
| `❌` | 失敗或缺失的數據 |
| `🔄` | 導航或重新訂購操作 |
| `ℹ️` | 信息或詳情操作 |
| `📝` | 特殊要求或文本值 |
| `├─` | 列表中間項目 |
| `└─` | 列表最後項目 |

---

## 故障排除指南

### 自訂項未顯示
**要查找的日誌**：
```
customizations=0  // 表示未加載
No customizations to restore
```

**檢查清單**：
1. 驗證 get_orders.php 返回 customizations 數據
2. 驗證 OrderItem.customizations 字段被正確填充
3. 檢查 API 響應中的 customization_details 陣列

### 重新訂購失敗
**要查找的日誌**：
```
❌ Error reordering order #XXXX
Stack trace: ...
```

**檢查清單**：
1. 驗證 MenuItems 存在於數據庫
2. 檢查 CartManager 是否正確存儲項目
3. 驗證 CartActivity 正確啟動

### 數量計算錯誤
**要查找的日誌**：
```
Total item count: X  // 應該等於所有 qty 的總和
```

**檢查清單**：
1. 驗證每個 OrderItem.quantity 值正確
2. 確認數量求和邏輯正確

---

## 修改的文件

- **OrderAdapter.java**
  - Lines 53-133: onBindViewHolder 日誌增強
  - Lines 227-263: handleReorder 日誌增強
  - Lines 463-530: showOrderDetails 日誌增強
  - Lines 395-450: 自訂項恢復日誌增強

---

## 下一步

1. 在 Android 設備上運行應用
2. 打開訂單歷史頁面
3. 在 Logcat 中使用 `tag:OrderAdapter` 過濾
4. 執行以下操作並檢查日誌：
   - 滾動訂單列表
   - 點擊 Reorder 按鈕
   - 點擊 Details 按鈕
5. 使用日誌驗證數據流和自訂項正確恢復

---

**最後更新**：2024 年
**日誌版本**：1.0
**適用於**：YummyRestaurant Android 應用 - 訂單歷史功能
