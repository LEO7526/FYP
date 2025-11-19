package com.example.yummyrestaurant.api;

import android.content.Context;
import android.content.SharedPreferences;

public class ApiConfig {
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String KEY_API_ENV = "api_environment";

    // Define your environments here
    public static final String BASE_SIMULATOR_URL = "http://10.0.2.2/newFolder/Database/projectapi/";
    public static final String BASE_PHONE_URL     = "http://192.168.0.120/newFolder/Database/projectapi/";

    // Save environment choice
    public static void setApiEnv(Context context, String env) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_API_ENV, env)
                .apply();
    }

    // Get saved environment choice
    public static String getApiEnv(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_API_ENV, "Emulator"); // default
    }

    // Return the actual base URL based on saved choice
    public static String getBaseUrl(Context context) {
        String env = getApiEnv(context);
        if ("Phone".equals(env)) {
            return BASE_PHONE_URL;
        } else {
            return BASE_SIMULATOR_URL;
        }
    }
}