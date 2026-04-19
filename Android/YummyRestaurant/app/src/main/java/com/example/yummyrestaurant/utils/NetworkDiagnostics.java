package com.example.yummyrestaurant.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.yummyrestaurant.api.ApiConfig;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;

/**
 * Network connectivity and image loading diagnostics
 */
public class NetworkDiagnostics {
    private static final String TAG = "NetworkDiagnostics";

    /**
     * Check if device has active internet connection
     */
    public static boolean hasInternetConnection(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                Log.e(TAG, "ConnectivityManager is null");
                return false;
            }

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            
            String networkType = activeNetwork != null ? activeNetwork.getTypeName() : "None";
            Log.d(TAG, "Network Status - Connected: " + connected + ", Type: " + networkType);
            
            return connected;
        } catch (Exception e) {
            Log.e(TAG, "Error checking internet connection", e);
            return false;
        }
    }

    /**
     * Test connectivity to GitHub (where images are hosted)
     */
    public static boolean testGithubConnectivity() {
        return testUrlConnectivity("https://raw.githubusercontent.com");
    }

    /**
     * Test connectivity to specific URL
     */
    public static boolean testUrlConnectivity(String urlString) {
        try {
            Log.d(TAG, "Testing connectivity to: " + urlString);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("HEAD");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            boolean success = (responseCode >= 200 && responseCode < 400);
            Log.d(TAG, "URL test result: " + urlString + " -> HTTP " + responseCode + 
                    (success ? " ✓" : " ✗"));
            return success;
        } catch (IOException e) {
            Log.e(TAG, "Network test failed for " + urlString, e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error testing URL", e);
            return false;
        }
    }

    /**
     * Generate comprehensive diagnostics report
     */
    public static String generateDiagnosticsReport(Context context) {
        StringBuilder report = new StringBuilder();
        report.append("=== Network Diagnostics Report ===\n");
        report.append("Timestamp: ").append(System.currentTimeMillis()).append("\n");
        report.append("Device: ").append(android.os.Build.DEVICE).append("\n");
        report.append("Android: ").append(android.os.Build.VERSION.RELEASE).append("\n\n");

        // Internet connectivity
        report.append("Internet Connectivity:\n");
        boolean hasInternet = hasInternetConnection(context);
        report.append("  - Has Internet: ").append(hasInternet ? "YES" : "NO").append("\n");

        // GitHub connectivity
        report.append("\nGitHub Image Host:\n");
        boolean githubAccessible = testGithubConnectivity();
        report.append("  - Accessible: ").append(githubAccessible ? "YES" : "NO").append("\n");
        report.append("  - URL: https://raw.githubusercontent.com\n");

        // Image loading examples
        report.append("\nImage Loading Test:\n");
        String sampleImageUrl = buildSampleDishImageUrl();
        boolean dishImageAccessible = testUrlConnectivity(sampleImageUrl);
        report.append("  - Local dish images: ").append(dishImageAccessible ? "✓" : "✗").append("\n");
        report.append("  - Test URL: ").append(sampleImageUrl).append("\n");

        report.append("\nRecommendations:\n");
        if (!hasInternet) {
            report.append("  ⚠ ERROR: No internet connection detected\n");
            report.append("    - Enable WiFi or mobile data\n");
            report.append("    - Check airplane mode is OFF\n");
        }
        if (!githubAccessible) {
            report.append("  ⚠ ERROR: Cannot access GitHub\n");
            report.append("    - Check DNS resolution\n");
            report.append("    - Try VPN if GitHub is blocked\n");
            report.append("    - Check firewall settings\n");
        }
        if (hasInternet && githubAccessible) {
            report.append("  ✓ All network checks passed\n");
            report.append("    Images should load normally\n");
        }

        return report.toString();
    }

    /**
     * Log diagnostics to system log
     */
    public static void logDiagnostics(Context context) {
        String report = generateDiagnosticsReport(context);
        Log.i(TAG, report);
    }

    private static String buildSampleDishImageUrl() {
        String baseUrl = ApiConfig.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "http://10.0.2.2/newFolder/Image/dish/1.jpg";
        }

        String normalized = baseUrl.trim();
        String imageBase = normalized.replace("/Database/projectapi/", "/Image/");
        if (!imageBase.endsWith("/")) {
            imageBase += "/";
        }

        return imageBase + "dish/1.jpg";
    }
}
