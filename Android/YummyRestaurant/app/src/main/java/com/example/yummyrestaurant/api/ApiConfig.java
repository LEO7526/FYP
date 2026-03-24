package com.example.yummyrestaurant.api;

import android.content.Context;
import android.util.Log;

import com.example.yummyrestaurant.BuildConfig;

public class ApiConfig {
    private static final String TAG = "ApiConfig";

    // Use one base URL per build type (debug/release) instead of runtime environment toggles.
    public static final String BASE_URL = BuildConfig.API_DEFAULT_BASE_URL;

    /**
     * 返回基於環境的實際 API 基礎 URL
     * @param context 應用上下文
     * @return 相應環境的 API URL
     */
    public static String getBaseUrl(Context context) {
        Log.d(TAG, "Using API base URL: " + BASE_URL);
        return BASE_URL;
    }
}