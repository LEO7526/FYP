# 菜品自訂選項系統實施指南

## 📋 概述

本文檔說明如何在 YummyRestaurant Android 應用中實現菜品自訂選項系統，包括：
- 菜品自訂選項驗證
- 自訂選項正確保存到購物車
- 訂單中自訂選項的持久化
- 數據庫和 API 端點支持

---

## 🔧 系統組件

### 1. Android 模型類

#### `CustomizationOption.java`
- 代表菜品的自訂選項定義
- 例如：辛辣度、邊菜選擇、額外配件
- 支持類型：single_choice, multi_choice, quantity, text_note

#### `OrderItemCustomization.java`
- 代表訂單項目的實際自訂選擇
- 儲存客戶的選擇（例如："Hot" spice level）
- 包含額外費用計算

#### `Customization.java` (增強版)
- 包含辛辣度、備註和詳細自訂列表
- 包含驗證方法 `validateCustomizations()`
- 包含成本計算 `getTotalAdditionalCost()`

### 2. Android 驗證工具

#### `CustomizationValidator.java`
- 驗證所有必填自訂項是否已選擇
- 檢查多選限制
- 驗證文字輸入長度

### 3. Android Activities

#### `CustomizeDishActivity_v2.java`
- 改進版本，支持完整驗證
- 驗證必填項：辛辣度、特殊要求
- 將自訂詳情保存到 `Customization` 對象
- 添加到購物車前進行驗證

---

## 💾 數據庫表結構

### 新增表（在 `customization_tables_4.3.sql` 中）

```sql
-- 自訂選項定義表
item_customization_options
  - option_id (PK)
  - item_id (FK)
  - option_name (例如："Spice Level")
  - option_type (single_choice, multi_choice, quantity, text_note)
  - is_required (TINYINT)
  - max_selections (INT)

-- 自訂選項的具體選擇
customization_option_choices
  - choice_id (PK)
  - option_id (FK)
  - choice_name (例如："Hot")
  - additional_cost (DECIMAL)

-- 訂單自訂詳情（訂單提交後儲存）
order_item_customizations
  - customization_id (PK)
  - oid (FK to orders)
  - option_id (FK)
  - option_name
  - choice_ids (JSON)
  - choice_names (JSON)
  - text_value (文字備註)
  - additional_cost
```

---

## 🔄 流程：從菜品到訂單

### 1️⃣ 菜品詳情頁面 (DishDetailActivity)
```
用戶點擊 "Customize" 按鈕
    ↓
啟動 CustomizeDishActivity
```

### 2️⃣ 自訂頁面 (CustomizeDishActivity_v2)
```
顯示自訂選項：
  - 必選：辛辣度（Mild, Medium, Hot, Numbing）
  - 可選：特殊備註（文字輸入）

用戶做選擇
  ↓
按「Save」按鈕
  ↓
驗證：
  - 辛辣度已選擇？✓
  - 備註不超過 500 字？✓
  ↓
建立 OrderItemCustomization 對象
  ↓
加入購物車
  ↓
返回購物車
```

### 3️⃣ 購物車 (CartActivity)
```
顯示購物車項目
  - 菜名 + 數量
  - 自訂選項（例如："(Hot) • Notes: No msg"）
  - 小計（包括額外費用）
```

### 4️⃣ 訂單提交 (PaymentActivity/CheckoutActivity)
```
收集所有購物車項目
  ↓
呼叫 saveOrderDirect.php
  ↓
JSON 格式：
{
  "cid": 123,
  "items": [
    {
      "item_id": 6,
      "qty": 2,
      "customizations": [
        {
          "option_id": 1,
          "option_name": "Spice Level",
          "choice_ids": null,
          "choice_names": ["Hot"],
          "text_value": "Extra chili on side",
          "additional_cost": 0.50
        }
      ]
    }
  ],
  "total_amount": 5000,
  "coupon_id": null
}
```

### 5️⃣ 後端保存 (saveOrderDirect_v2.php)
```
1. 插入訂單主記錄 (orders 表)
2. 為每個項目插入 (order_items 表)
3. 為每個自訂選項調用 saveItemCustomizations()：
   - 插入 order_item_customizations 表
   - 儲存自訂選擇詳情
4. 提交事務
5. 返回 order_id
```

---

## 📦 JSON 請求格式

### 項目自訂對象結構

```json
{
  "option_id": 1,
  "option_name": "Spice Level",
  "option_type": "single_choice",
  "is_required": true,
  "choice_ids": [2, 3],
  "choice_names": ["Hot", "Extra Chili"],
  "text_value": "特殊要求的文字備註",
  "additional_cost": 5.50
}
```

### 完整訂單請求

```json
{
  "cid": 101,
  "ostatus": 1,
  "total_amount": 12500,
  "coupon_id": null,
  "items": [
    {
      "item_id": 6,
      "qty": 2,
      "customizations": [
        {
          "option_id": 1,
          "option_name": "Spice Level",
          "choice_ids": null,
          "choice_names": ["Hot"],
          "text_value": "側面加辣椒",
          "additional_cost": 0.0
        }
      ]
    }
  ]
}
```

---

## ✅ 實施檢查清單

### 數據庫
- [ ] 運行 `customization_tables_4.3.sql` 創建表
- [ ] 驗證表結構正確
- [ ] 添加示例自訂選項數據

### Android 代碼
- [ ] 創建/更新 `CustomizationOption.java`
- [ ] 創建 `OrderItemCustomization.java`
- [ ] 更新 `Customization.java`
- [ ] 創建 `CustomizationValidator.java`
- [ ] 更新 `CustomizeDishActivity.java`（或使用 v2 版本）
- [ ] 在 `CartActivity` 中顯示自訂詳情
- [ ] 在訂單確認頁面顯示自訂選項

### API 端點
- [ ] 部署 `saveOrderDirect_v2.php`（或備份舊版本）
- [ ] 在 Android 中更新 API 調用
- [ ] 測試自訂選項的保存

### 測試
- [ ] 測試選擇必填項
- [ ] 測試驗證錯誤提示
- [ ] 測試自訂選項加入購物車
- [ ] 測試訂單提交和數據庫保存
- [ ] 驗證 order_item_customizations 表中的數據

---

## 🔧 配置示例

### 為菜品添加自訂選項

```sql
-- 為麻婆豆腐 (item_id=6) 添加辛辣度選項
INSERT INTO item_customization_options 
(item_id, option_name, option_type, is_required, max_selections)
VALUES (6, 'Spice Level', 'single_choice', 1, 1);

-- 取得新建的 option_id
SET @option_id = LAST_INSERT_ID();

-- 添加選擇項
INSERT INTO customization_option_choices 
(option_id, choice_name, additional_cost, display_order)
VALUES 
(@option_id, 'Mild', 0, 1),
(@option_id, 'Medium', 0, 2),
(@option_id, 'Hot', 0, 3),
(@option_id, 'Numbing', 0, 4);
```

---

## 🐛 常見問題和排查

| 問題 | 原因 | 解決方案 |
|------|------|--------|
| 自訂選項未出現在購物車 | CartAdapter 未更新 | 更新 CartItemAdapter 顯示自訂詳情 |
| 訂單保存失敗 | 缺少 order_item_customizations 表 | 運行 customization_tables_4.3.sql |
| 驗證未觸發 | validateCustomizations() 未被調用 | 確保在 saveBtn.onClick 中調用 |
| 自訂費用未計算 | getTotalAdditionalCost() 未實現 | 在 Customization 類中實現成本計算 |

---

## 📱 Android 集成示例

### CartActivity 中顯示自訂

```java
String customizationText = "";
if (cartItem.getCustomization() != null) {
    Customization custom = cartItem.getCustomization();
    
    // 顯示辛辣度
    if (custom.getSpiceLevel() != null && !custom.getSpiceLevel().isEmpty()) {
        customizationText += "(" + custom.getSpiceLevel() + ") ";
    }
    
    // 顯示備註
    if (custom.getExtraNotes() != null && !custom.getExtraNotes().isEmpty()) {
        customizationText += "• " + custom.getExtraNotes();
    }
}

tvCustomization.setText(customizationText);
```

### 提交訂單時收集自訂

```java
private JSONArray buildItemsWithCustomizations() {
    JSONArray items = new JSONArray();
    
    for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
        CartItem cartItem = entry.getKey();
        Integer qty = entry.getValue();
        
        JSONObject item = new JSONObject();
        item.put("item_id", cartItem.getMenuItemId());
        item.put("qty", qty);
        
        // 添加自訂選項
        if (cartItem.getCustomization() != null) {
            JSONArray customizations = buildCustomizationArray(cartItem.getCustomization());
            item.put("customizations", customizations);
        }
        
        items.put(item);
    }
    
    return items;
}
```

---

## 📞 支援

如有問題，請檢查：
1. 數據庫表是否正確創建
2. Android 模型是否序列化正確
3. JSON 格式是否符合 PHP 期望
4. 日誌消息（Logcat）是否有錯誤

---

**最後更新**: 2025年11月19日
**版本**: 1.0（YummyRestaurant 4.3+）
