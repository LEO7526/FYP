package com.example.yummyrestaurant.utils;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Utility class to check material availability for food items and packages
 * Prevents customers from ordering items when there aren't enough ingredients
 */
public class MaterialAvailabilityChecker {
    
    private static final String TAG = "MaterialChecker";
    private static final String API_BASE_URL = "http://10.0.2.2/newFolder/Database/projectapi/";
    
    /**
     * Interface for material availability callback
     */
    public interface MaterialCheckCallback {
        void onCheckComplete(boolean allAvailable, String message, JSONArray materialDetails);
        void onCheckError(String error);
    }
    
    /**
     * Check material availability for current cart items
     */
    public static void checkCartMaterialAvailability(Context context, MaterialCheckCallback callback) {
        Map<CartItem, Integer> cartItems = CartManager.getCartItems();
        
        if (cartItems.isEmpty()) {
            callback.onCheckComplete(true, "Cart is empty", new JSONArray());
            return;
        }
        
        try {
            JSONArray itemsArray = new JSONArray();
            
            for (Map.Entry<CartItem, Integer> entry : cartItems.entrySet()) {
                CartItem cartItem = entry.getKey();
                Integer quantity = entry.getValue();
                MenuItem menuItem = cartItem.getMenuItem();
                
                if (menuItem != null && quantity > 0) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("item_id", menuItem.getId());
                    itemObj.put("quantity", quantity);
                    itemObj.put("is_package", false); // Individual items for now
                    itemsArray.put(itemObj);
                }
            }
            
            checkMaterialAvailability(context, itemsArray, callback);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating cart check request", e);
            callback.onCheckError("Error preparing cart check: " + e.getMessage());
        }
    }
    
    /**
     * Check material availability for a single item before adding to cart
     */
    public static void checkItemMaterialAvailability(Context context, MenuItem item, int quantity, boolean isPackage, MaterialCheckCallback callback) {
        try {
            JSONArray itemsArray = new JSONArray();
            JSONObject itemObj = new JSONObject();
            itemObj.put("item_id", item.getId());
            itemObj.put("quantity", quantity);
            itemObj.put("is_package", isPackage);
            itemsArray.put(itemObj);
            
            checkMaterialAvailability(context, itemsArray, callback);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating item check request", e);
            callback.onCheckError("Error preparing item check: " + e.getMessage());
        }
    }
    
    /**
     * Check material availability for additional quantity of existing cart item
     */
    public static void checkAdditionalQuantity(Context context, CartItem cartItem, int additionalQty, MaterialCheckCallback callback) {
        // Get current cart quantities
        Map<CartItem, Integer> cartItems = CartManager.getCartItems();
        
        try {
            JSONArray itemsArray = new JSONArray();
            
            // Add all current cart items
            for (Map.Entry<CartItem, Integer> entry : cartItems.entrySet()) {
                CartItem existingItem = entry.getKey();
                Integer currentQty = entry.getValue();
                MenuItem menuItem = existingItem.getMenuItem();
                
                if (menuItem != null && currentQty > 0) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("item_id", menuItem.getId());
                    
                    // If this is the item we're adding to, include the additional quantity
                    if (existingItem.equals(cartItem)) {
                        itemObj.put("quantity", currentQty + additionalQty);
                    } else {
                        itemObj.put("quantity", currentQty);
                    }
                    
                    itemObj.put("is_package", false);
                    itemsArray.put(itemObj);
                }
            }
            
            // If the cart item doesn't exist in cart yet, add it as new
            if (!cartItems.containsKey(cartItem)) {
                MenuItem menuItem = cartItem.getMenuItem();
                if (menuItem != null) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("item_id", menuItem.getId());
                    itemObj.put("quantity", additionalQty);
                    itemObj.put("is_package", false);
                    itemsArray.put(itemObj);
                }
            }
            
            checkMaterialAvailability(context, itemsArray, callback);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating additional quantity check request", e);
            callback.onCheckError("Error preparing quantity check: " + e.getMessage());
        }
    }
    
    /**
     * Core method to check material availability via API
     */
    private static void checkMaterialAvailability(Context context, JSONArray itemsArray, MaterialCheckCallback callback) {
        String url = API_BASE_URL + "check_material_availability.php";
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("items", itemsArray);
            
            Log.d(TAG, "=== MATERIAL AVAILABILITY CHECK ===");
            Log.d(TAG, "URL: " + url);
            Log.d(TAG, "Request: " + requestBody.toString());
            Log.d(TAG, "Items to check: " + itemsArray.toString());
            
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        Log.d(TAG, "=== API RESPONSE RECEIVED ===");
                        Log.d(TAG, "Full response: " + response.toString());
                        
                        boolean success = response.optBoolean("success", false);
                        Log.d(TAG, "API success: " + success);
                        
                        if (success) {
                            boolean allAvailable = response.optBoolean("all_available", false);
                            String message = response.optString("message", "");
                            JSONArray materialCheck = response.optJSONArray("material_check");
                            
                            Log.d(TAG, "All materials available: " + allAvailable);
                            Log.d(TAG, "API message: " + message);
                            Log.d(TAG, "Material check details: " + (materialCheck != null ? materialCheck.toString() : "null"));
                            
                            callback.onCheckComplete(allAvailable, message, materialCheck != null ? materialCheck : new JSONArray());
                        } else {
                            String errorMessage = response.optString("message", "Unknown error");
                            Log.e(TAG, "API returned error: " + errorMessage);
                            callback.onCheckError("API Error: " + errorMessage);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing material check response", e);
                        callback.onCheckError("Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "=== API ERROR ===");
                    Log.e(TAG, "Network error occurred", error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response data: " + new String(error.networkResponse.data));
                    }
                    String errorMessage = "Network error";
                    if (error.networkResponse != null) {
                        errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
                    }
                    callback.onCheckError(errorMessage);
                }
            );
            
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating material check request", e);
            callback.onCheckError("Error creating request: " + e.getMessage());
        }
    }
    
    /**
     * Format insufficient materials message for user display
     */
    public static String formatInsufficientMaterialsMessage(JSONArray materialDetails) {
        StringBuilder message = new StringBuilder("Insufficient ingredients:\n");
        
        try {
            for (int i = 0; i < materialDetails.length(); i++) {
                JSONObject material = materialDetails.getJSONObject(i);
                boolean isSufficient = material.optBoolean("is_sufficient", true);
                
                if (!isSufficient) {
                    String materialName = material.optString("material_name", "Unknown");
                    double required = material.optDouble("required_quantity", 0);
                    double available = material.optDouble("available_quantity", 0);
                    
                    message.append("• ").append(materialName)
                           .append(": need ").append(String.format("%.1f", required))
                           .append(", have ").append(String.format("%.1f", available))
                           .append("\n");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error formatting insufficient materials message", e);
            return "Some ingredients are insufficient. Please contact staff.";
        }
        
        return message.toString().trim();
    }
}