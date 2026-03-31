package com.example.yummyrestaurant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * LabelManager - Handles fetching and caching of translatable labels from database
 * 
 * Instead of hardcoding labels in strings.xml, this manager fetches them from the backend API
 * and caches them locally for offline access.
 * 
 * Supported labels:
 * - Spice levels (不辣, 微辣, 中辣, 辣, 麻辣)
 * - Menu tags (素食, 海鮮, 辣, etc.)
 * - Customization groups (辣度, 甜度, 冰量, 奶量, 配料)
 * - Customization option values (微辣, 中辣, 多糖, 少冰, etc.)
 * 
 * Usage:
 * LabelManager manager = new LabelManager(context);
 * manager.fetchLabels("zh-TW", new LabelManager.LabelCallback() {
 *     @Override
 *     public void onSuccess() {
 *         String spiceLabel = manager.getSpiceLevelName(1);
 *         List<Map<String, Object>> tags = manager.getAllTags();
 *     }
 *     
 *     @Override
 *     public void onError(String error) {
 *         Log.e("LabelManager", error);
 *     }
 * });
 */
public class LabelManager {
    private static final String TAG = "LabelManager";
    private static final String PREF_NAME = "label_manager";
    private static final String CACHE_KEY_SPICE = "cache_spice_levels";
    private static final String CACHE_KEY_TAGS = "cache_tags";
    private static final String CACHE_KEY_CUST_GROUPS = "cache_customization_groups";
    private static final String CACHE_KEY_LANGUAGE = "cache_language";
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours
    
    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private String apiBaseUrl = "http://your-server.com/FYP%20CCK/api/"; // Replace with your server
    
    // In-memory caches
    private List<Map<String, Object>> spiceLevels = new ArrayList<>();
    private List<Map<String, Object>> tags = new ArrayList<>();
    private List<Map<String, Object>> customizationGroups = new ArrayList<>();
    
    public interface LabelCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public LabelManager(Context context, String baseUrl) {
        this.context = context;
        this.apiBaseUrl = baseUrl;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Fetch all labels from backend API and cache them
     */
    public void fetchLabels(String language, LabelCallback callback) {
        // Check if cache is still valid
        if (isCacheValid(language)) {
            loadFromCache();
            callback.onSuccess();
            return;
        }
        
        // Fetch from API
        new Thread(() -> {
            try {
                String url = apiBaseUrl + "labels.php?lang=" + language + "&action=all";
                
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                
                Response response = client.newCall(request).execute();
                
                if (!response.isSuccessful()) {
                    // Try loading from cache on failure
                    if (hasCacheData()) {
                        loadFromCache();
                        callback.onSuccess();
                        return;
                    }
                    callback.onError("API returned: " + response.code());
                    return;
                }
                
                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonResponse.get("success").getAsBoolean()) {
                    JsonObject data = jsonResponse.getAsJsonObject("data");
                    
                    // Parse spice levels
                    spiceLevels = parseJsonArray(data.getAsJsonArray("spiceLevels"));
                    
                    // Parse tags
                    tags = parseJsonArray(data.getAsJsonArray("tags"));
                    
                    // Parse customization groups
                    parseCustomizationGroups(data.getAsJsonArray("customizationGroups"));
                    
                    // Cache the results
                    saveToCache(language);
                    
                    callback.onSuccess();
                } else {
                    callback.onError("API returned error");
                }
                
            } catch (IOException e) {
                Log.e(TAG, "Error fetching labels", e);
                if (hasCacheData()) {
                    loadFromCache();
                    callback.onSuccess();
                } else {
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Get spice level name by ID
     */
    public String getSpiceLevelName(int spiceId) {
        for (Map<String, Object> spice : spiceLevels) {
            if ((int) spice.get("id") == spiceId) {
                return (String) spice.get("name");
            }
        }
        return "Unknown";
    }
    
    /**
     * Get all spice levels
     */
    public List<Map<String, Object>> getAllSpiceLevels() {
        return new ArrayList<>(spiceLevels);
    }
    
    /**
     * Get tag name by ID
     */
    public String getTagName(int tagId) {
        for (Map<String, Object> tag : tags) {
            if ((int) tag.get("id") == tagId) {
                return (String) tag.get("name");
            }
        }
        return "Unknown";
    }
    
    /**
     * Get all tags
     */
    public List<Map<String, Object>> getAllTags() {
        return new ArrayList<>(tags);
    }
    
    /**
     * Get customization group label by ID
     */
    public String getCustomizationGroupLabel(int groupId) {
        for (Map<String, Object> group : customizationGroups) {
            if ((int) group.get("id") == groupId) {
                return (String) group.get("label");
            }
        }
        return "Unknown";
    }
    
    /**
     * Get customization option value name by ID
     */
    public String getCustomizationValueName(int valueId) {
        for (Map<String, Object> group : customizationGroups) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> values = (List<Map<String, Object>>) group.get("values");
            for (Map<String, Object> value : values) {
                if ((int) value.get("id") == valueId) {
                    return (String) value.get("name");
                }
            }
        }
        return "Unknown";
    }
    
    /**
     * Get all customization groups with their values
     */
    public List<Map<String, Object>> getAllCustomizationGroups() {
        return new ArrayList<>(customizationGroups);
    }
    
    /**
     * Get customization group with specific ID
     */
    public Map<String, Object> getCustomizationGroup(int groupId) {
        for (Map<String, Object> group : customizationGroups) {
            if ((int) group.get("id") == groupId) {
                return new HashMap<>(group);
            }
        }
        return null;
    }
    
    // ============ Private helper methods ============
    

    private void parseCustomizationGroups(JsonArray jsonArray) {
        customizationGroups = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            Map<String, Object> group = gson.fromJson(jsonArray.get(i), Map.class);
            // Values are already in the group as a list
            customizationGroups.add(group);
        }
    }
    
    private void saveToCache(String language) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CACHE_KEY_SPICE, gson.toJson(spiceLevels));
        editor.putString(CACHE_KEY_TAGS, gson.toJson(tags));
        editor.putString(CACHE_KEY_CUST_GROUPS, gson.toJson(customizationGroups));
        editor.putString(CACHE_KEY_LANGUAGE, language);
        editor.putLong("cache_time", System.currentTimeMillis());
        editor.apply();
    }
    
    private void loadFromCache() {
        try {
            String spiceJson = sharedPreferences.getString(CACHE_KEY_SPICE, "[]");
            String tagsJson = sharedPreferences.getString(CACHE_KEY_TAGS, "[]");
            String custGroupsJson = sharedPreferences.getString(CACHE_KEY_CUST_GROUPS, "[]");
            
            spiceLevels = gson.fromJson(spiceJson, List.class);
            tags = gson.fromJson(tagsJson, List.class);
            customizationGroups = gson.fromJson(custGroupsJson, List.class);
        } catch (Exception e) {
            Log.e(TAG, "Error loading from cache", e);
        }
    }
    
    private boolean isCacheValid(String language) {
        String cachedLanguage = sharedPreferences.getString(CACHE_KEY_LANGUAGE, "");
        if (!cachedLanguage.equals(language)) {
            return false;
        }
        
        long cacheTime = sharedPreferences.getLong("cache_time", 0);
        return System.currentTimeMillis() - cacheTime < CACHE_DURATION;
    }
    
    private boolean hasCacheData() {
        return !sharedPreferences.getString(CACHE_KEY_SPICE, "").isEmpty();
    }
    
    /**
     * Clear all cached labels
     */
    public void clearCache() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(CACHE_KEY_SPICE);
        editor.remove(CACHE_KEY_TAGS);
        editor.remove(CACHE_KEY_CUST_GROUPS);
        editor.remove(CACHE_KEY_LANGUAGE);
        editor.remove("cache_time");
        editor.apply();
        
        spiceLevels.clear();
        tags.clear();
        customizationGroups.clear();
    }
}
