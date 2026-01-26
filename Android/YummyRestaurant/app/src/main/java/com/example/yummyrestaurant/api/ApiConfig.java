package com.example.yummyrestaurant.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class ApiConfig {
    private static final String TAG = "ApiConfig";
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String KEY_API_ENV = "api_environment";
    private static final String KEY_ENV_AUTO_DETECTED = "api_env_auto_detected";

    // Define your environments here
    public static final String BASE_SIMULATOR_URL = "http://10.0.2.2/newFolder/Database/projectapi/";
    public static final String BASE_PHONE_URL     = "http://192.168.0.120/newFolder/Database/projectapi/";
    // 真實 IP: 192.168.0.120 (根據您的網絡自動調整)

    /**
     * 自動偵測是否為 Android 模擬器
     * @return true 如果是模擬器，false 如果是真實手機
     */
    public static boolean isEmulator() {
        // 檢查多個指標來判斷是否為模擬器
        boolean isEmulatorByBuild = 
                (Build.FINGERPRINT != null && Build.FINGERPRINT.contains("generic")) ||
                (Build.FINGERPRINT != null && Build.FINGERPRINT.contains("unknown")) ||
                (Build.DEVICE != null && (Build.DEVICE.contains("generic") || Build.DEVICE.contains("emulator"))) ||
                (Build.PRODUCT != null && Build.PRODUCT.contains("sdk")) ||
                (Build.MODEL != null && Build.MODEL.contains("Android SDK")) ||
                (Build.MANUFACTURER != null && Build.MANUFACTURER.contains("Genymotion"));
        
        Log.d(TAG, "Device Detection:");
        Log.d(TAG, "  Build.FINGERPRINT: " + Build.FINGERPRINT);
        Log.d(TAG, "  Build.DEVICE: " + Build.DEVICE);
        Log.d(TAG, "  Build.PRODUCT: " + Build.PRODUCT);
        Log.d(TAG, "  Build.MODEL: " + Build.MODEL);
        Log.d(TAG, "  Build.MANUFACTURER: " + Build.MANUFACTURER);
        Log.d(TAG, "  Is Emulator: " + isEmulatorByBuild);
        
        return isEmulatorByBuild;
    }

    /**
     * 自動偵測環境並保存到 SharedPreferences
     * @param context 應用上下文
     */
    public static void autoDetectEnvironment(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // 如果已經自動偵測過，不需要再檢測
        if (prefs.getBoolean(KEY_ENV_AUTO_DETECTED, false)) {
            Log.d(TAG, "Environment already auto-detected, skipping");
            return;
        }
        
        boolean isEmulator = isEmulator();
        String detectedEnv = isEmulator ? "Emulator" : "Phone";
        
        Log.d(TAG, "Auto-detected environment: " + detectedEnv);
        
        prefs.edit()
                .putString(KEY_API_ENV, detectedEnv)
                .putBoolean(KEY_ENV_AUTO_DETECTED, true)
                .apply();
    }

    /**
     * 手動設置環境（用於測試或覆蓋自動偵測）
     * @param context 應用上下文
     * @param env "Emulator" 或 "Phone"
     */
    public static void setApiEnv(Context context, String env) {
        Log.d(TAG, "Manually setting API environment to: " + env);
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_API_ENV, env)
                .putBoolean(KEY_ENV_AUTO_DETECTED, true)  // 標記為已手動設置
                .apply();
    }

    /**
     * 獲取保存的環境選擇
     * @param context 應用上下文
     * @return 保存的環境（"Emulator" 或 "Phone"）
     */
    public static String getApiEnv(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_API_ENV, isEmulator() ? "Emulator" : "Phone");
    }

    /**
     * 返回基於環境的實際 API 基礎 URL
     * @param context 應用上下文
     * @return 相應環境的 API URL
     */
    public static String getBaseUrl(Context context) {
        String env = getApiEnv(context);
        String baseUrl;
        
        if ("Phone".equals(env)) {
            baseUrl = BASE_PHONE_URL;
            Log.d(TAG, "Using PHONE environment: " + baseUrl);
        } else {
            baseUrl = BASE_SIMULATOR_URL;
            Log.d(TAG, "Using EMULATOR environment: " + baseUrl);
        }
        
        return baseUrl;
    }

    /**
     * 重置自動偵測標誌，強制重新偵測（用於調試）
     * @param context 應用上下文
     */
    public static void resetAutoDetection(Context context) {
        Log.d(TAG, "Resetting auto-detection flag");
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ENV_AUTO_DETECTED, false)
                .remove(KEY_API_ENV)
                .apply();
    }
}