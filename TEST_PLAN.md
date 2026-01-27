# 測試計畫 - 滾動和旋轉功能修復

## 已實現的更改

### 1. 佈局改進 (activity_confirm_booking.xml)
- ✅ 新增 HorizontalScrollView 層級支持左右滾動
- ✅ 保留 NestedScrollView 支持上下滾動
- ✅ 雙向滾動容器結構完成

### 2. SeatingChartView 改進 (SeatingChartView.java)
- ✅ 更新 onMeasure() 方法支援雙向滾動
- ✅ 寬度設定為 1.3x（支援左右滾動）
- ✅ 高度計算保持寬度的 0.85 比例（支援上下滾動）
- ✅ 確保最小寬度 = 螢幕寬度，最小高度 = 螢幕高度

### 3. 狀態管理 (ConfirmBookingActivity.java)
- ✅ 新增 onSaveInstanceState() 方法保存選中表格 ID
- ✅ 新增 onSaveInstanceState() 保存表單可見性狀態
- ✅ 新增 restoreStateAfterRotation() 方法恢復旋轉後的狀態
- ✅ onCreate() 檢查 savedInstanceState 並恢復狀態

## 測試場景

### 場景 1: 水平滾動 (Landscape Mode)
**步驟:**
1. 啟動應用程序
2. 導航到預訂頁面
3. 旋轉設備至橫屏
4. 嘗試向左/右滾動查看邊緣表格

**預期結果:**
- ✅ 可以向左/右滾動
- ✅ 邊界表格 (x=10, x=90) 可見
- ✅ 表格顯示正確，未被裁切

### 場景 2: 垂直滾動 (Portrait Mode)
**步驟:**
1. 啟動應用程序
2. 導航到預訂頁面（豎屏）
3. 嘗試向上/下滾動

**預期結果:**
- ✅ 可以向上/下滾動查看所有表格
- ✅ 表格佈局完整
- ✅ 狀態欄始終可見

### 場景 3: 旋轉恢復 - 無表格選中
**步驟:**
1. 啟動應用程序
2. 導航到預訂頁面（豎屏）
3. 旋轉至橫屏
4. 再旋轉回豎屏

**預期結果:**
- ✅ 佈局正確適應旋轉
- ✅ 無表格選中時，FAB 保持禁用狀態
- ✅ 表單保持隱藏

### 場景 4: 旋轉恢復 - 已選中表格 (主要修復)
**步驟:**
1. 啟動應用程序
2. 導航到預訂頁面（豎屏）
3. 點擊選擇任意表格
4. FAB 啟用，表單顯示
5. 旋轉至橫屏
6. 驗證選中表格和表單狀態
7. 旋轉回豎屏

**預期結果:**
- ✅ 橫屏時選中表格保持選中狀態（藍色高亮）
- ✅ 橫屏時表單保持可見
- ✅ 返回豎屏時表單重新出現（**主要修復**)
- ✅ FAB 始終啟用
- ✅ 可以提交預訂

### 場景 5: 雙向滾動組合測試
**步驟:**
1. 啟動應用程序
2. 導航到預訂頁面（豎屏）
3. 向下滾動查看所有表格
4. 旋轉至橫屏
5. 向右滾動查看邊界表格
6. 在邊界表格上（如右上角）進行點擊
7. 驗證表單顯示
8. 旋轉回豎屏
9. 驗證表單仍可見

**預期結果:**
- ✅ 所有滾動方向都正常運作
- ✅ 邊界表格可點擊
- ✅ 旋轉後狀態完全恢復

### 場景 6: 容量驗證保持工作
**步驟:**
1. 啟動應用程序
2. 導航到預訂頁面，選擇人數（如 6 人）
3. 嘗試點擊不適合容量的表格（應為灰色）
4. 旋轉設備
5. 驗證仍無法選中不合適的表格

**預期結果:**
- ✅ 不合適的表格仍呈灰色
- ✅ 無法選中容量不足的表格
- ✅ 旋轉不影響容量驗證邏輯

## 潛在問題排查

### 如果水平滾動不工作:
- 檢查 HorizontalScrollView 是否正確包裝 NestedScrollView
- 驗證 SeatingChartView 的 onMeasure() 寬度計算
- 檢查 fillViewport 屬性設定

### 如果表單旋轉後仍不出現:
- 驗證 onSaveInstanceState() 是否被調用
- 檢查 restoreStateAfterRotation() 的邏輯
- 確認 selectedTable 物件不為 null
- 檢查 showBookingDetailsSheet() 方法是否被正確調用

### 如果旋轉時表格選中狀態丟失:
- 驗證 setSelectedTable() 方法是否被調用
- 檢查 SeatingChartView.selectedTableId 變數
- 確認表格列表 (tableList) 未被清空

## 編譯和部署

編譯命令:
```bash
gradlew.bat clean build
```

APK 位置: `app/build/outputs/apk/debug/app-debug.apk`

安裝命令:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 驗收標準

✅ 所有 6 個測試場景通過
✅ 沒有崩潰或異常
✅ 水平和垂直滾動流暢
✅ 旋轉後表單完整恢復
✅ FAB 和表格狀態一致
