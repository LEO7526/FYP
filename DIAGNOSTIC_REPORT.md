# 訂單自訂項未顯示 - 診斷報告

## 🔍 問題根源定位

根據日誌分析，已找到根本問題：

### 現象
```
OrderAdapter: Item: Grape Oolong Tea qty=2 price=26.0 customizations=0
             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
```
**`customizations=0`** - OrderItem 中的自訂項列表為空或未被加載

### 時間線對比

| 階段 | 自訂項狀態 | 位置 |
|------|-----------|------|
| 確認頁面 | ✅ 正常顯示 | OrderConfirmationActivity |
| 保存到服務器 | ✅ 正確序列化 | TempPaymentActivity |
| **訂單歷史** | ❌ **customizations=0** | OrderAdapter |
| 數據庫 | ❓ 待驗證 | order_item_customizations 表 |

---

## 🔧 已添加的診斷日誌

### 1. OrderHistoryActivity (新增日誌)

**位置**: Lines 58-99

**新增日誌點**:
```java
Log.d("OrderHistory", "🔄 API returned " + orderList.size() + " orders");
Log.d("OrderHistory", "  Order #" + order.getOid() + ":");
Log.d("OrderHistory", "    Items: " + order.getItems().size());
Log.d("OrderHistory", "      - " + item.getName() + " (customizations=" + custCount + ")");
Log.d("OrderHistory", "        * " + cust.getOptionName() + "=" + cust.getChoiceNames());
```

**作用**: 追蹤 API 返回的原始數據結構，檢查 customizations 字段是否在 JSON 中

### 2. OrderAdapter.onBindViewHolder (增強日誌)

**位置**: Lines 75-117

**新增日誌點**:
```java
Log.d("OrderAdapter", "    Customizations object: NOT NULL, size=" + customizations.size());
Log.d("OrderAdapter", "      ✅ Found cust: " + cust.getOptionName() + "=" + cust.getChoiceNames());
Log.d("OrderAdapter", "    ⚠️ Customizations list is EMPTY");
Log.d("OrderAdapter", "    ⚠️ Customizations object is NULL");
```

**作用**: 詳細檢查 customizations 對象的狀態（NULL vs EMPTY vs 有數據）

---

## 📋 診斷步驟

### 步驟 1: 重新編譯和運行
```bash
./gradlew clean build
./gradlew installDebug
```

### 步驟 2: 打開 Logcat 並過濾

過濾 1 - 檢查 API 返回的數據:
```
tag:OrderHistory
```

過濾 2 - 檢查 OrderAdapter 中的 customizations:
```
tag:OrderAdapter customizations
```

### 步驟 3: 打開訂單歷史頁面

**觀察 Logcat 並查找以下日誌**:

#### 預期看到的日誌序列:

```
1️⃣ 首先看 OrderHistory 日誌:
   D/OrderHistory: 🔄 API returned 1 orders
   D/OrderHistory:   Order #19:
   D/OrderHistory:     Items: 1
   D/OrderHistory:       - Grape Oolong Tea (customizations=???)
                         ⬆️ 這裡會告訴我們 API 返回的 customizations 數量

2️⃣ 然後看 OrderAdapter 日誌:
   D/OrderAdapter: === BINDING ORDER #19 at position 0 ===
   D/OrderAdapter:   Item: Grape Oolong Tea qty=2 ...
   D/OrderAdapter:     Customizations object: NOT NULL, size=???
                       ⬆️ 這會告訴我們 Gson 解析後的結果
```

---

## 🎯 可能的結果場景

### 場景 A: API 未返回 customizations
```
D/OrderHistory:       - Grape Oolong Tea (customizations=0)
D/OrderAdapter:     ⚠️ Customizations object is NULL
```
**結論**: ❌ `get_orders.php` 未返回自訂項數據  
**修復**: 檢查數據庫中 order_item_customizations 表是否有數據

### 場景 B: API 返回了，但 Gson 未解析
```
D/OrderHistory:       - Grape Oolong Tea (customizations=2)
                      ⬆️ 這裡有 2 個
D/OrderAdapter:     ⚠️ Customizations object is NULL
                    ⬆️ 但這裡變成 NULL
```
**結論**: ❌ Gson 解析失敗，可能是字段名稱不匹配  
**修復**: 檢查 OrderItem 模型的 @SerializedName 是否正確

### 場景 C: 一切正常
```
D/OrderHistory:       - Grape Oolong Tea (customizations=2)
                      * Sugar Level=Less Sweet
                      * Ice Level=No Ice
D/OrderAdapter:     Customizations object: NOT NULL, size=2
                    ✅ Found cust: Sugar Level=Less Sweet
```
**結論**: ✅ 數據流正常，自訂項應該能顯示

---

## 🔎 檢查清單

在運行診斷之前，請確認:

- [ ] 數據庫中有 order_item_customizations 表
- [ ] 表中有訂單 #19 的自訂項記錄
- [ ] OrderItem.java 有 `@SerializedName("customizations")`
- [ ] OrderItemCustomization.java 存在且正確定義
- [ ] get_orders.php 查詢包含 customizations 部分

---

## 📊 診斷命令

### 立即運行這個命令來查看完整流程:

```bash
# 清除 Logcat
adb logcat -c

# 實時監視診斷日誌
adb logcat tag:OrderHistory tag:OrderAdapter
```

### 或者分別查看:

```bash
# 只看 API 返回的數據結構
adb logcat tag:OrderHistory

# 只看 OrderAdapter 中的 customizations 檢查
adb logcat tag:OrderAdapter | grep customizations
```

---

## 📸 日誌示例輸出

### 如果 API 正確返回數據:

```
2025-12-19 15:46:38 D/OrderHistory: 🔄 API returned 1 orders
2025-12-19 15:46:38 D/OrderHistory:   Order #19:
2025-12-19 15:46:38 D/OrderHistory:     Items: 1
2025-12-19 15:46:38 D/OrderHistory:       - Grape Oolong Tea (customizations=2)
2025-12-19 15:46:38 D/OrderHistory:         * Sugar Level=Less Sweet
2025-12-19 15:46:38 D/OrderHistory:         * Ice Level=No Ice
2025-12-19 15:46:38 D/OrderAdapter: === BINDING ORDER #19 at position 0 ===
2025-12-19 15:46:38 D/OrderAdapter:   Item: Grape Oolong Tea qty=2 price=26.0 customizations=2
2025-12-19 15:46:38 D/OrderAdapter:     Customizations object: NOT NULL, size=2
2025-12-19 15:46:38 D/OrderAdapter:       ✅ Found cust: Sugar Level=Less Sweet
2025-12-19 15:46:38 D/OrderAdapter:       ✅ Found cust: Ice Level=No Ice
```

### 如果 API 未返回:

```
2025-12-19 15:46:38 D/OrderHistory: 🔄 API returned 1 orders
2025-12-19 15:46:38 D/OrderHistory:   Order #19:
2025-12-19 15:46:38 D/OrderHistory:     Items: 1
2025-12-19 15:46:38 D/OrderHistory:       - Grape Oolong Tea (customizations=0)
2025-12-19 15:46:38 D/OrderAdapter: === BINDING ORDER #19 at position 0 ===
2025-12-19 15:46:38 D/OrderAdapter:   Item: Grape Oolong Tea qty=2 price=26.0 customizations=0
2025-12-19 15:46:38 D/OrderAdapter:     ⚠️ Customizations object is NULL
```

---

## ⚡ 快速修復列表

### 如果是 API 問題

**檢查 get_orders.php**:
```php
// 查詢應該包含:
SELECT option_id, option_name, choice_names, text_value, additional_cost
FROM order_item_customizations
WHERE oid = ? AND item_id = ?
```

**驗證數據庫**:
```sql
-- 檢查是否有訂單 19 的自訂項
SELECT * FROM order_item_customizations WHERE oid = 19;

-- 應該看到 2 行:
-- option_id=5, option_name="Sugar Level", choice_names="Less Sweet"
-- option_id=6, option_name="Ice Level", choice_names="No Ice"
```

### 如果是 Gson 解析問題

**檢查 OrderItem.java**:
```java
@SerializedName("customizations")
private List<OrderItemCustomization> customizations;
```

**檢查 OrderItemCustomization.java**:
```java
@SerializedName("option_id")
private int optionId;

@SerializedName("option_name")
private String optionName;

@SerializedName("choice_names")
private String choiceNames;
```

---

## 🎯 下一步

1. **運行應用** → 執行診斷命令
2. **查看日誌** → 確定問題在哪一層
3. **根據結果** → 修復相應的組件
4. **驗證結果** → 重新運行確認修復

---

**準備好診斷了嗎？** 請運行應用並回報 Logcat 中看到的日誌！

