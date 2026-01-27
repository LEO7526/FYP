# Full-Screen Seating Chart Implementation Summary

**Date:** 2026-01-27  
**Project:** YummyRestaurant Booking System  
**Version:** 2.0 (Enhanced with Interactive Seating Chart)

---

## 概述 (Overview)

本實現將預訂系統從垂直列表顯示轉換為全屏互動式座位圖，允許用戶點擊座位圖上的餐桌進行選擇。整合了實時佔用狀態、座標信息和響應式設計。

---

## 📋 實現清單

### ✅ 1. 數據庫表結構更新
**文件:** `createProjectDB_5.3.sql`

**更改:**
- 在 `seatingChart` 表中新增兩個欄位:
  - `x_position DECIMAL(5,2)` - 餐桌X座標（佔屏幕寬度百分比）
  - `y_position DECIMAL(5,2)` - 餐桌Y座標（佔屏幕高度百分比）

**座標來源:** `seating_layout.json` 中的表格位置數據

**範例INSERT:**
```sql
INSERT INTO seatingChart (capacity, status, x_position, y_position) VALUES
(2, 0, 10, 10),  -- Table 1: 2-person table at position (10%, 10%)
(4, 0, 20, 25),  -- Table 2: 4-person table at position (20%, 25%)
(8, 0, 15, 55);  -- Table 3: 8-person table at position (15%, 55%)
```

---

### ✅ 2. 新PHP API端點
**文件:** `projectapi/get_available_tables_layout.php` (新建)

**功能:**
- 返回完整的座位圖數據，包括:
  - 所有餐桌的座標 (x, y)
  - 餐桌容量和狀態
  - 實時佔用狀態（來自 `table_orders` 表）
  - 預訂狀態（來自 `booking` 表）

**API調用:**
```
GET /projectapi/get_available_tables_layout.php?date=2024-01-15&time=18:30&pnum=4
```

**API響應格式:**
```json
{
  "success": true,
  "date": "2024-01-15",
  "time": "18:30",
  "guest_count": 4,
  "required_capacity": 4,
  "layout": {
    "width_percent": 100,
    "height_percent": 100,
    "cell_width": 10,
    "cell_height": 15
  },
  "tables": [
    {
      "id": 1,
      "capacity": 2,
      "status": "available",
      "x": 10,
      "y": 10,
      "is_available": true,
      "suitable_for_booking": false
    },
    {
      "id": 2,
      "capacity": 4,
      "status": "occupied",
      "x": 20,
      "y": 25,
      "is_available": false,
      "suitable_for_booking": false
    }
  ],
  "available_tables": [
    // Only tables suitable for the guest count
  ],
  "total_tables": 50,
  "total_available": 35,
  "summary": {
    "available": 35,
    "occupied": 10,
    "reserved": 5
  }
}
```

**實時狀態邏輯:**
- 檢查 `table_orders` 表中狀態不為 'paid' 或 'cancelled' 的記錄 → **佔用狀態**
- 檢查 `booking` 表中在指定時間範圍內（±2小時）的記錄 → **預訂狀態**
- 其他情況 → **可用**

---

### ✅ 3. 更新Table模型類
**文件:** `models/Table.java`

**新增屬性:**
```java
private float x;                      // X座標（百分比）
private float y;                      // Y座標（百分比）
private String status;                // 狀態：available, occupied, reserved
private boolean is_available;         // 是否可用
private boolean suitable_for_booking; // 是否適合該用餐人數
```

**構造函數:**
```java
// 基本構造函數（向後兼容）
public Table(int tid, int capacity)

// 完整構造函數（新座位圖支持）
public Table(int tid, int capacity, float x, float y, String status, 
             boolean is_available, boolean suitable_for_booking)
```

**新增方法:**
- `getX()`, `getY()` - 獲取座標
- `getStatus()`, `setStatus()` - 獲取/設置狀態
- `isAvailable()`, `setAvailable()` - 可用性檢查
- `isSuitableForBooking()`, `setSuitableForBooking()` - 人數適合性檢查

---

### ✅ 4. 新自訂座位圖視圖組件
**文件:** `views/SeatingChartView.java` (新建)

**功能:**
- Canvas基礎的自訂View組件
- 渲染所有餐桌及其座標
- 觸摸交互支持
- 響應式設計

**主要特性:**
1. **響應式設計**
   - 根據屏幕尺寸自動調整餐桌大小
   - 最小大小: 40dp，最大大小: 60dp
   - 支持任何屏幕寬度

2. **顏色編碼**
   - 可用 (Available): 綠色 (#4CAF50)
   - 佔用 (Occupied): 紅色 (#F44336)
   - 預訂 (Reserved): 橙色 (#FF9800)
   - 已選擇 (Selected): 藍色 (#2196F3)

3. **視覺反饋**
   - 點擊可用餐桌時高亮顯示
   - 不可用餐桌點擊時提示訊息
   - 圓角矩形設計，邊框清晰

4. **觸摸交互**
   - 點擊餐桌時檢測
   - 回調接口 `OnTableSelectedListener`
   - 支持選擇/取消選擇

**使用示例:**
```java
// 在XML中
<com.example.yummyrestaurant.views.SeatingChartView
    android:id="@+id/seatingChartView"
    android:layout_width="match_parent"
    android:layout_height="300dp" />

// 在Java中
SeatingChartView chartView = findViewById(R.id.seatingChartView);
chartView.setTables(tableList);
chartView.setOnTableSelectedListener(new SeatingChartView.OnTableSelectedListener() {
    @Override
    public void onTableSelected(Table table) {
        // Handle table selection
    }
    
    @Override
    public void onTableUnavailable(int tableId) {
        // Handle unavailable table click
    }
});
```

---

### ✅ 5. 修改ConfirmBookingActivity
**文件:** `activities/ConfirmBookingActivity.java`

**主要改變:**
1. 移除 RecyclerView 和 TableAdapter
2. 新增 SeatingChartView 引用
3. 實現 `SeatingChartView.OnTableSelectedListener` 接口
4. 更新JSON解析以支持新的座位圖數據格式
5. 增強的表格數據驗證和錯誤處理

**新增方法:**
- `setupSeatingChart()` - 初始化座位圖
- `onTableSelected(Table table)` - 點擊事件回調
- `onTableUnavailable(int tableId)` - 不可用表格回調

**修改的JSON解析:**
支持新的API響應格式，包括座標和狀態信息
```java
// 解析新格式
float x = tableObject.optDouble("x");
float y = tableObject.optDouble("y");
String status = tableObject.optString("status");
boolean isAvailable = tableObject.optBoolean("is_available");
boolean suitableForBooking = tableObject.optBoolean("suitable_for_booking");
```

---

### ✅ 6. 修改BookingActivity
**文件:** `activities/BookingActivity.java`

**主要改變:**
1. 將API調用從 `get_available_tables.php` 改為 `get_available_tables_layout.php`
2. 改進的JSON響應解析
3. 增強的錯誤處理和超時設置
4. 添加詳細的日誌記錄

**API調用修改:**
```java
// 舊版本
String urlString = String.format("%sget_available_tables.php?date=%s&time=%s&pnum=%s",
        baseUrl, date, time, pnum);

// 新版本
String urlString = String.format("%sget_available_tables_layout.php?date=%s&time=%s&pnum=%s",
        baseUrl, date, time, pnum);
```

**響應處理:**
```java
JSONObject jsonResponse = new JSONObject(apiResponse);
if (jsonResponse.optBoolean("success")) {
    String tablesJsonArray = jsonResponse.optJSONArray("tables").toString();
    // ... 傳遞給ConfirmBookingActivity
}
```

---

### ✅ 7. 更新XML佈局
**文件:** `res/layout/activity_confirm_booking.xml`

**主要改變:**
1. 移除RecyclerView
2. 新增SeatingChartView組件
3. 添加顏色圖例（可用/佔用/預訂）
4. 改進的UI/UX設計
5. 響應式佈局權重分配

**新佈局結構:**
```xml
LinearLayout (主容器)
├── Title (標題)
├── Subtitle (副標題)
├── Legend (顏色圖例)
│   ├── Available (綠色)
│   ├── Occupied (紅色)
│   └── Reserved (橙色)
├── SeatingChartView (座位圖)
├── User Details Section
│   ├── EditText: Name
│   ├── EditText: Phone
│   ├── EditText: Purpose
│   └── EditText: Remark
└── Button: Confirm
```

---

## 🎨 用戶體驗流程

### 預訂流程 (Updated)

1. **BookingActivity (第一步)**
   - 用戶選擇日期、時間、用餐人數
   - 點擊「Find Available Tables」
   - 系統調用 `get_available_tables_layout.php`
   - 返回完整的座位圖數據

2. **ConfirmBookingActivity (第二步)**
   - 顯示全屏互動式座位圖
   - 用戶點擊座位圖上的可用餐桌
   - 餐桌被選中並高亮
   - 用戶填寫預訂詳細信息
   - 點擊「Confirm Booking」提交

3. **提交預訂**
   - 驗證選擇的餐桌和用戶信息
   - 調用 `create_booking.php` API
   - 成功返回後關閉Activity

---

## 🔄 數據流

```
用戶輸入 (日期、時間、人數)
    ↓
BookingActivity
    ↓
get_available_tables_layout.php API
    ↓ 查詢數據庫
┌─────────────────────────┐
│ seatingChart:           │
│ - tid                   │
│ - capacity              │
│ - x_position            │
│ - y_position            │
│ - status                │
└─────────────────────────┘
┌─────────────────────────┐
│ booking:                │
│ - tid (預訂)            │
│ - bdate                 │
│ - btime                 │
└─────────────────────────┘
┌─────────────────────────┐
│ table_orders (實時):    │
│ - table_number          │
│ - status                │
└─────────────────────────┘
    ↓ 返回JSON (包含座標和狀態)
ConfirmBookingActivity
    ↓
SeatingChartView (渲染座位圖)
    ↓ 用戶點擊選擇
Table Selection
    ↓ 用戶確認預訂
create_booking.php API
    ↓
✅ 預訂完成
```

---

## 📱 響應式設計詳情

### 屏幕適應性
- **最小屏幕寬度:** 320dp (舊手機)
- **最大餐桌大小:** 60dp (平板設備)
- **最小餐桌大小:** 40dp (手機設備)
- **邊距:** 屏幕寬度的5%

### 座標計算
```
實際像素座標 = 邊距 + (可用寬度 × 百分比座標 / 100)
例如：
實際X = marginLeft + (availableWidth × 10 / 100)  // 10% 的可用寬度
```

### 不同設備表現
- **手機 (320dp):** 餐桌 ~40dp，間距優化
- **平板 (600dp+):** 餐桌 ~60dp，更清晰可點擊

---

## 🔐 數據安全和驗證

### API端點安全
- 參數驗證（日期、時間、人數格式）
- 數據庫準備語句防止SQL注入
- 適當的HTTP狀態碼返回

### 表格狀態驗證
- 只允許選擇可用餐桌
- 檢查餐桌容量是否符合人數要求
- 實時檢查 `table_orders` 中的佔用狀況

---

## 📊 測試清單

- [ ] 數據庫：驗證 `seatingChart` 表座標數據正確
- [ ] API：測試 `get_available_tables_layout.php` 返回正確格式
- [ ] UI：驗證座位圖在不同屏幕尺寸上正確渲染
- [ ] 交互：測試點擊座位圖選擇/取消選擇餐桌
- [ ] 狀態：驗證實時佔用狀態正確反映在UI中
- [ ] 預訂：完整的預訂流程從開始到完成
- [ ] 錯誤處理：測試各種錯誤情況（無可用表格等）

---

## 🚀 部署說明

1. **更新數據庫:** 執行 `createProjectDB_5.3.sql` 腳本
2. **上傳新PHP文件:** `get_available_tables_layout.php`
3. **更新Android應用:**
   - 添加 `SeatingChartView.java`
   - 更新 `Table.java`
   - 修改 `BookingActivity.java` 和 `ConfirmBookingActivity.java`
   - 更新 `activity_confirm_booking.xml`
4. **測試:** 進行全面的功能測試

---

## 📝 版本歷史

- **v1.0 (原始):** RecyclerView 垂直列表顯示
- **v2.0 (當前):** Canvas-based 互動式座位圖，含座標和實時狀態

---

## 🔗 相關文件引用

- 座位圖配置: `projectapi/seating_layout.json`
- 現有API: `projectapi/get_seating_chart.php` (可選集成)
- 驗證API: `projectapi/verify_table.php`
- 創建預訂: `projectapi/create_booking.php`

