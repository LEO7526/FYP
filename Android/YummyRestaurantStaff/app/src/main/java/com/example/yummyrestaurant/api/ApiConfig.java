package com.example.yummyrestaurant.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.yummyrestaurant.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiConfig {
    private static final String TAG = "ApiConfig";
    private static final String PREFS_NAME = "api_config";
    private static final String PREF_KEY_DYNAMIC_BASE_URL = "dynamic_base_url";
    private static final String ADDRESS_ENDPOINT = "get_computer_address.php";

    private static volatile String runtimeBaseUrl = resolveBaseUrl();

    private ApiConfig() {
    }

    public interface InitCallback {
        void onComplete();
    }

    /**
     * Fetch dynamic address endpoint on app startup and update API base URL automatically.
     */
    public static void initializeBaseUrlOnStartup(Context context, InitCallback callback) {
        Context appContext = context.getApplicationContext();

        String cached = loadCachedBaseUrl(appContext);
        if (isValidHttpBaseUrl(cached)) {
            setRuntimeBaseUrl(cached, false);
        }

        List<String> candidateEndpoints = buildAddressEndpointCandidates();
        if (candidateEndpoints.isEmpty()) {
            postComplete(callback);
            return;
        }

        tryEndpointSequentially(appContext, candidateEndpoints, 0, callback);
    }

    /**
     * 返回基於環境的實際 API 基礎 URL
     * @param context 應用上下文
     * @return 相應環境的 API URL
     */
    public static String getBaseUrl(Context context) {
        Log.d(TAG, "Using API base URL: " + runtimeBaseUrl + " (isEmulator=" + isEmulator() + ")");
        return runtimeBaseUrl;
    }

    /**
     * Backward-compatible overload for call sites that do not have Context.
     */
    public static String getBaseUrl() {
        Log.d(TAG, "Using API base URL: " + runtimeBaseUrl + " (isEmulator=" + isEmulator() + ")");
        return runtimeBaseUrl;
    }

    private static String resolveBaseUrl() {
        if (!BuildConfig.DEBUG) {
            return BuildConfig.API_DEFAULT_BASE_URL;
        }
        return isEmulator() ? BuildConfig.API_SIMULATOR_BASE_URL : BuildConfig.API_PHONE_BASE_URL;
    }

    private static String buildAlternateProjectApiBase(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.isEmpty()) {
            return null;
        }
        if (endpointUrl.contains("/newFolder/Database/projectapi/")) {
            return normalizeBaseUrl(endpointUrl.replace("/newFolder/Database/projectapi/", "/projectapi/").replace(ADDRESS_ENDPOINT, ""));
        }
        if (endpointUrl.contains("/projectapi/")) {
            return normalizeBaseUrl(endpointUrl.replace("/projectapi/", "/newFolder/Database/projectapi/").replace(ADDRESS_ENDPOINT, ""));
        }
        return null;
    }

    private static List<String> buildAddressEndpointCandidates() {
        Set<String> endpoints = new LinkedHashSet<>();

        String currentBase = normalizeBaseUrl(runtimeBaseUrl);
        if (currentBase != null) {
            endpoints.add(currentBase + ADDRESS_ENDPOINT);
        }

        String phoneBase = normalizeBaseUrl(BuildConfig.API_PHONE_BASE_URL);
        if (phoneBase != null) {
            endpoints.add(phoneBase + ADDRESS_ENDPOINT);
        }

        String simulatorBase = normalizeBaseUrl(BuildConfig.API_SIMULATOR_BASE_URL);
        if (simulatorBase != null) {
            endpoints.add(simulatorBase + ADDRESS_ENDPOINT);
        }

        return new ArrayList<>(endpoints);
    }

    private static void tryEndpointSequentially(Context appContext, List<String> endpoints, int index, InitCallback callback) {
        if (index >= endpoints.size()) {
            Log.w(TAG, "Dynamic API discovery skipped: all endpoint candidates failed. Keep using " + runtimeBaseUrl);
            postComplete(callback);
            return;
        }

        String endpointUrl = endpoints.get(index);
        Log.d(TAG, "Trying dynamic API endpoint: " + endpointUrl);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(endpointUrl).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w(TAG, "Dynamic API endpoint failed: " + endpointUrl + " | " + e.getMessage());
                tryEndpointSequentially(appContext, endpoints, index + 1, callback);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "Dynamic API endpoint HTTP " + response.code() + ": " + endpointUrl);
                    if (response.code() == 404) {
                        String fallbackBase = buildAlternateProjectApiBase(endpointUrl);
                        if (isValidHttpBaseUrl(fallbackBase)) {
                            setRuntimeBaseUrl(fallbackBase, false);
                            saveCachedBaseUrl(appContext, fallbackBase);
                            RetrofitClient.reset();
                            Log.d(TAG, "Fallback API base URL from 404: " + fallbackBase);
                            response.close();
                            postComplete(callback);
                            return;
                        }
                    }
                    response.close();
                    tryEndpointSequentially(appContext, endpoints, index + 1, callback);
                    return;
                }

                try {
                    String body = response.body() != null ? response.body().string() : "";
                    JSONObject json = new JSONObject(body);
                    boolean success = json.optBoolean("success", false);
                    String dynamicBase = normalizeBaseUrl(json.optString("projectapi_base_url", ""));

                    if (success && isValidHttpBaseUrl(dynamicBase)) {
                        setRuntimeBaseUrl(dynamicBase, true);
                        saveCachedBaseUrl(appContext, dynamicBase);
                        RetrofitClient.reset();
                        Log.d(TAG, "Dynamic API base URL resolved: " + dynamicBase);
                        postComplete(callback);
                    } else {
                        Log.w(TAG, "Dynamic API response missing valid projectapi_base_url from: " + endpointUrl);
                        tryEndpointSequentially(appContext, endpoints, index + 1, callback);
                    }
                } catch (Exception ex) {
                    Log.w(TAG, "Failed to parse dynamic API response from: " + endpointUrl + " | " + ex.getMessage());
                    tryEndpointSequentially(appContext, endpoints, index + 1, callback);
                } finally {
                    response.close();
                }
            }
        });
    }

    private static void setRuntimeBaseUrl(String baseUrl, boolean fromNetwork) {
        String normalized = normalizeBaseUrl(baseUrl);
        if (!isValidHttpBaseUrl(normalized)) {
            return;
        }
        runtimeBaseUrl = normalized;
        Log.d(TAG, "API base URL updated (fromNetwork=" + fromNetwork + "): " + runtimeBaseUrl);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return null;
        }
        String trimmed = baseUrl.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    private static boolean isValidHttpBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return false;
        }
        return baseUrl.startsWith("http://") || baseUrl.startsWith("https://");
    }

    private static String loadCachedBaseUrl(Context appContext) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_KEY_DYNAMIC_BASE_URL, null);
    }

    private static void saveCachedBaseUrl(Context appContext, String baseUrl) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_KEY_DYNAMIC_BASE_URL, baseUrl).apply();
    }

    private static void postComplete(InitCallback callback) {
        if (callback == null) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(callback::onComplete);
    }

    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MODEL.contains("sdk_gphone")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic")
                || Build.DEVICE.contains("emulator")
                || Build.DEVICE.contains("emu")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk_gphone")
                || "google_sdk".equals(Build.PRODUCT);
    }
}