# 自動環境偵測 - 使用說明

## ✨ 新功能概述

應用現在會**自動偵測**是否運行在 Android Studio 模擬器或真實手機上，並自動使用對應的 API 地址。

- ✅ **模擬器** → 自動使用 `http://10.0.2.2/...`
- ✅ **真實手機** → 自動使用 `http://192.168.0.120/...`

**無需手動設置！**

---

## 📱 工作原理

### 自動偵測邏輯

應用檢查以下裝置屬性：

```
Build.FINGERPRINT   - 檢查是否包含 "generic" 或 "unknown"
Build.DEVICE        - 檢查是否包含 "generic" 或 "emulator"
Build.PRODUCT       - 檢查是否包含 "sdk"
Build.MODEL         - 檢查是否包含 "Android SDK"
Build.MANUFACTURER  - 檢查是否包含 "Genymotion"
```

- 如果匹配任何模擬器特徵 → **使用模擬器配置**
- 否則 → **使用真實手機配置**

### 偵測流程

1. **應用啟動** → MainActivity.onCreate()
2. **自動調用** → `ApiConfig.autoDetectEnvironment(this)`
3. **檢測環境** → 檢查 Build 屬性
4. **保存結果** → SharedPreferences（只檢測一次）
5. **後續使用** → `ApiConfig.getBaseUrl()` 自動使用正確的 URL

---

## 🔧 修改的文件

### 1. **ApiConfig.java**（主要修改）

新增方法：

```java
// 自動偵測是否為模擬器
isEmulator() 
  ↓ 檢查 Build 屬性，返回 true/false

// 自動偵測並保存環境
autoDetectEnvironment(Context context)
  ↓ 自動檢測一次，保存到 SharedPreferences

// 獲取基礎 URL（自動使用正確的地址）
getBaseUrl(Context context)
  ↓ 根據保存的環境返回正確的 URL
```

新增常數：

```java
BASE_SIMULATOR_URL = "http://10.0.2.2/newFolder/Database/projectapi/"
BASE_PHONE_URL = "http://192.168.0.120/newFolder/Database/projectapi/"
```

### 2. **MainActivity.java**（添加初始化）

在 `onCreate()` 中添加：

```java
// 🚀 自動偵測環境（模擬器 vs 真實手機）
ApiConfig.autoDetectEnvironment(this);
```

---

## 🔍 偵測結果查看

應用會輸出詳細的日誌信息。在 Android Studio 的 Logcat 中查看：

```
D/ApiConfig: Device Detection:
D/ApiConfig:   Build.FINGERPRINT: generic/emulator/generic_x86/generic:12/S3E2.220310.004/...
D/ApiConfig:   Build.DEVICE: generic_x86
D/ApiConfig:   Build.PRODUCT: sdk_google_phone_x86
D/ApiConfig:   Build.MODEL: Android SDK built for x86
D/ApiConfig:   Build.MANUFACTURER: Google
D/ApiConfig:   Is Emulator: true
D/ApiConfig: Auto-detected environment: Emulator
D/ApiConfig: Using EMULATOR environment: http://10.0.2.2/newFolder/Database/projectapi/
```

或者真實手機：

```
D/ApiConfig: Device Detection:
D/ApiConfig:   Build.FINGERPRINT: xiaomi/redmi_note_8_pro/mtkbeawe:11/RQ3A.211001.001/...
D/ApiConfig:   Build.DEVICE: mtkbeawe
D/ApiConfig:   Build.PRODUCT: redmi_note_8_pro
D/ApiConfig:   Build.MODEL: Redmi Note 8 Pro
D/ApiConfig:   Build.MANUFACTURER: Xiaomi
D/ApiConfig:   Is Emulator: false
D/ApiConfig: Auto-detected environment: Phone
D/ApiConfig: Using PHONE environment: http://192.168.0.120/newFolder/Database/projectapi/
```

---

## 🎯 使用場景

### 場景 1：在模擬器中測試
1. 打開 Android Studio
2. 啟動 AVD（Android Virtual Device）
3. 運行應用
4. ✅ 自動使用 `10.0.2.2`（無需額外配置）

### 場景 2：在真實手機中測試
1. 連接真實手機到電腦
2. 確保手機連接到相同的 Wi-Fi（192.168.0.x）
3. 運行應用
4. ✅ 自動使用 `192.168.0.120`（無需額外配置）

### 場景 3：更改電腦 IP（需要時）
如果您的開發電腦 IP 不是 `192.168.0.120`，修改：

```java
// ApiConfig.java 第 16 行
public static final String BASE_PHONE_URL = "http://YOUR_PC_IP/newFolder/Database/projectapi/";
```

然後：
```java
// 重置自動偵測（強制重新檢測）
ApiConfig.resetAutoDetection(context);
```

---

## 🛠 進階用法

### 手動覆蓋自動偵測

```java
// 強制使用模擬器配置（即使在真實手機上）
ApiConfig.setApiEnv(context, "Emulator");

// 強制使用手機配置（即使在模擬器上）
ApiConfig.setApiEnv(context, "Phone");
```

### 重置自動偵測

```java
// 清除已保存的配置，下次啟動時重新偵測
ApiConfig.resetAutoDetection(context);
```

### 查詢當前環境

```java
// 獲取當前環境名稱
String env = ApiConfig.getApiEnv(context);
// 返回 "Emulator" 或 "Phone"

// 獲取當前 API 基礎 URL
String baseUrl = ApiConfig.getBaseUrl(context);
// 返回完整的 API URL
```

---

## 📋 檢查清單

部署前確認：

- [x] ApiConfig.java 已更新自動偵測邏輯
- [x] MainActivity.java 已添加 `ApiConfig.autoDetectEnvironment(this)`
- [x] BASE_PHONE_URL 設置為您的電腦 IP（192.168.0.120）
- [x] XAMPP Apache 已啟動
- [x] 真實手機連接到相同 Wi-Fi
- [ ] 編譯並運行應用
- [ ] 檢查 Logcat 確認自動偵測成功
- [ ] 測試 QR 碼掃描功能

---

## 🐛 故障排除

### 問題：仍然連接超時
**原因**：可能沒有重新編譯應用  
**解決**：
```
1. Clean Project (Build → Clean Project)
2. Rebuild Project (Build → Rebuild Project)
3. Re-run 應用
```

### 問題：自動偵測為模擬器，但實際是真實手機
**原因**：裝置信息被修改，或是定製的 ROM  
**解決**：
```java
// 手動設置為手機
ApiConfig.setApiEnv(context, "Phone");
```

### 問題：自動偵測為手機，但實際是模擬器
**原因**：罕見的模擬器配置  
**解決**：
```java
// 手動設置為模擬器
ApiConfig.setApiEnv(context, "Emulator");
```

### 問題：需要調試偵測邏輯
**方式**：查看 Logcat 日誌
```
adb logcat | grep ApiConfig
```

---

## 📊 技術細節

### 保存機制

偵測結果保存在 SharedPreferences 中：

```
鍵：api_environment
值："Emulator" 或 "Phone"

鍵：api_env_auto_detected
值：true（表示已自動偵測過）
```

### 自動偵測流程圖

```
應用啟動
    ↓
MainActivity.onCreate()
    ↓
ApiConfig.autoDetectEnvironment()
    ↓
檢查是否已保存過偵測結果？
    ├─ 是 → 使用保存的結果 → 完成
    └─ 否 → 執行偵測
           ↓
        檢查 Build 屬性
           ↓
        判斷是模擬器還是真實手機
           ↓
        保存結果到 SharedPreferences
           ↓
        完成
```

---

## ✅ 總結

現在您的應用會：

1. ✅ 在**模擬器上自動使用** `10.0.2.2`
2. ✅ 在**真實手機上自動使用** `192.168.0.120`
3. ✅ 無需手動配置或設置
4. ✅ 結果會被緩存，只檢測一次
5. ✅ 支持手動覆蓋和重置

**一次編譯，到處運行！** 🚀

---

**最後更新**：2026-01-27
