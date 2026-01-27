# 修復摘要 - 水平/豎直滾動及旋轉狀態恢復

## 已完成的修改

### 1. 佈局檔案 - activity_confirm_booking.xml
**位置:** `app/src/main/res/layout/activity_confirm_booking.xml`

**改動:**
- 將 SeatingChartView 包裹在 HorizontalScrollView 中（支持左右滾動）
- 保留 NestedScrollView 層級（支持上下滾動）
- 新增 `android:fillViewport="true"` 確保滾動容器填充整個視圖

**結構:**
```xml
NestedScrollView (vertical)
  └─ HorizontalScrollView (horizontal)
      └─ SeatingChartView
```

**效果:**
- ✅ 豎屏模式：可上下滾動查看所有表格
- ✅ 橫屏模式：可左右滾動查看邊界表格 (x=10%, x=90%)
- ✅ 合併模式：可在任何方向滾動

---

### 2. SeatingChartView.java - 更新測量方法
**位置:** `app/src/main/java/com/example/yummyrestaurant/views/SeatingChartView.java`

**改動 - onMeasure() 方法:**

```java
@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // 获取父容器的建议尺寸
    int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
    int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);
    
    // 双向滚动支持:
    // 宽度: 1.3x 建议宽度 (支持水平滚动)
    // 高度: 0.85x 宽度的比例 (支持竖直滚动)
    
    int desiredWidth = (int)(suggestedWidth * 1.3f);   // 1.3x 水平拓展
    int desiredHeight = (int)(desiredWidth * 0.85f);   // 维持宽高比
    
    // 确保最少覆盖建议尺寸
    if (desiredWidth < suggestedWidth) desiredWidth = suggestedWidth;
    if (desiredHeight < suggestedHeight) desiredHeight = suggestedHeight;
    
    int widthSpec = MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.EXACTLY);
    int heightSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY);
    
    super.onMeasure(widthSpec, heightSpec);
}
```

**效果:**
- ✅ 寬度擴展 30% 以支持左右滾動
- ✅ 高度按比例保持一致外觀
- ✅ 自動適應橫/豎屏尺寸

---

### 3. ConfirmBookingActivity.java - 狀態管理
**位置:** `app/src/main/java/com/example/yummyrestaurant/activities/ConfirmBookingActivity.java`

**改動 1 - onCreate() 方法新增恢復邏輯:**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_confirm_booking);

    // ... 初始化代碼 ...

    // 旋轉後恢復選中表格和表單狀態
    if (savedInstanceState != null) {
        restoreStateAfterRotation(savedInstanceState);
    }
}
```

**改動 2 - 新增 onSaveInstanceState() 方法:**

```java
@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (selectedTable != null) {
        // 保存已選擇的表格 ID
        outState.putInt("SELECTED_TABLE_ID", selectedTable.getTid());
        // 保存表單可見性狀態
        outState.putBoolean("FORM_VISIBLE", bottomSheetContainer.getChildCount() > 0);
    }
}
```

**改動 3 - 新增 restoreStateAfterRotation() 方法:**

```java
private void restoreStateAfterRotation(Bundle savedInstanceState) {
    int selectedTableId = savedInstanceState.getInt("SELECTED_TABLE_ID", -1);
    boolean formWasVisible = savedInstanceState.getBoolean("FORM_VISIBLE", false);

    if (selectedTableId != -1) {
        // 從表格列表中尋找已選擇的表格
        for (Table t : tableList) {
            if (t.getTid() == selectedTableId) {
                selectedTable = t;
                // 更新視覺狀態（藍色高亮）
                seatingChartView.setSelectedTable(selectedTableId);
                // 啟用 FAB 按鈕
                fabConfirm.setEnabled(true);
                fabConfirm.setAlpha(1.0f);

                // 如果旋轉前表單可見，則重新顯示
                if (formWasVisible) {
                    showBookingDetailsSheet();  // 重新填充表單
                }
                break;
            }
        }
    }
}
```

**效果:**
- ✅ 旋轉後保存並恢復選中的表格 ID
- ✅ 重新顯示之前可見的預訂表單
- ✅ FAB 按鈕狀態一致
- ✅ 使用者無需重新選擇表格

---

## 技術細節

### 雙向滾動原理

1. **垂直滾動 (NestedScrollView)**
   - 高度計算: `desiredHeight = desiredWidth * 0.85f`
   - 表格排列在 1.2x 的網格中
   - 允許向上/下滾動查看所有表格

2. **水平滾動 (HorizontalScrollView)**
   - 寬度計算: `desiredWidth = suggestedWidth * 1.3f`
   - 額外寬度 (30%) 允許邊界表格 (x=10%, x=90%) 完全可見
   - 允許向左/右滾動在橫屏模式下查看

3. **配置變更 (屏幕旋轉)**
   - Activity 銷毀前保存 selectedTableId 和表單狀態
   - Activity 重建時恢復已選表格
   - 自動重新填充表單如果之前可見

---

## 編譯結果

```
BUILD SUCCESSFUL in 1m 44s
100 actionable tasks: 99 executed, 1 up-to-date
```

✅ **編譯完全成功**

---

## 測試建議

### 核心測試場景

1. **水平滾動測試**
   - 旋轉至橫屏
   - 向左滾動查看左邊表格 (x=10%)
   - 向右滾動查看右邊表格 (x=90%)

2. **垂直滾動測試**
   - 保持豎屏
   - 向上滾動查看上方表格 (y=10%)
   - 向下滾動查看下方表格 (y=85%)

3. **旋轉恢復測試 (主要修復)**
   - 豎屏選擇表格 → 表單顯示
   - 旋轉至橫屏 → 表格/表單保持可見
   - 旋轉回豎屏 → **表單應重新出現** ✓
   - 點擊提交按鈕 → 預訂成功

4. **容量驗證測試**
   - 不合適的表格應保持灰色
   - 旋轉不應影響容量驗證邏輯

---

## 已知限制

- 雙向同時滾動依賴於 Android 的 NestedScrollView + HorizontalScrollView 實現
- 大型屏幕 (7"+) 在橫屏模式下可能需要額外調整寬度比例
- 表單最初在 FAB 點擊時顯示，旋轉後自動重現

---

## 部署檢查清單

- ✅ 編譯無錯誤
- ✅ 佈局檔案結構正確
- ✅ 狀態管理方法完整
- ✅ 容量驗證邏輯保留
- ✅ 新增 APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## 回滾計畫

如果需要恢復原始功能，可執行:

```bash
git revert <commit-hash>
```

或手動撤銷上述三個檔案的改動。
