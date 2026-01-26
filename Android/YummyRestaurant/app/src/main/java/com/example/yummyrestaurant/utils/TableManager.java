package com.example.yummyrestaurant.utils;

import android.util.Log;

import com.example.yummyrestaurant.api.RetrofitClient;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Utility class to manage table-related operations
 * Handles validation, caching, and API communication for seating chart and table verification
 */
public final class TableManager {
    private static final String TAG = "TableManager";
    
    private static Integer validatedTableId = null;
    private static Integer validatedTableCapacity = null;
    private static String validatedTableStatus = null;
    private static boolean isValidated = false;

    private TableManager() {
    }

    /**
     * Interface for table verification API
     */
    public interface TableVerificationApi {
        @POST("verify_table.php")
        Call<TableVerificationResponse> verifyTable(@Query("table_id") int tableId);
    }

    /**
     * Response model for table verification
     */
    public static class TableVerificationResponse {
        public boolean success;
        public boolean valid;
        public int table_id;
        public int capacity;
        public String status;
        public boolean available;
        public String message;
    }

    /**
     * Verify table by ID and store the validated information
     * @param context Application context for API configuration
     * @param tableId The table ID to verify
     * @param callback Callback with verification result
     */
    public static void verifyTable(android.content.Context context, int tableId, TableVerificationCallback callback) {
        TableVerificationApi api = RetrofitClient.getClient(context).create(TableVerificationApi.class);
        
        Call<TableVerificationResponse> call = api.verifyTable(tableId);
        call.enqueue(new Callback<TableVerificationResponse>() {
            @Override
            public void onResponse(Call<TableVerificationResponse> call, Response<TableVerificationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TableVerificationResponse verificationResponse = response.body();
                    
                    if (verificationResponse.success && verificationResponse.valid) {
                        // Store verified table information
                        validatedTableId = verificationResponse.table_id;
                        validatedTableCapacity = verificationResponse.capacity;
                        validatedTableStatus = verificationResponse.status;
                        isValidated = true;
                        
                        Log.d(TAG, "Table " + tableId + " verified successfully. Capacity: " + 
                                verificationResponse.capacity + ", Status: " + verificationResponse.status);
                        
                        if (callback != null) {
                            callback.onVerificationSuccess(verificationResponse);
                        }
                    } else {
                        Log.w(TAG, "Table " + tableId + " verification failed: " + verificationResponse.message);
                        isValidated = false;
                        if (callback != null) {
                            callback.onVerificationFailure(verificationResponse.message);
                        }
                    }
                } else {
                    String errorMsg = "Failed to verify table: " + (response.message() != null ? response.message() : "Unknown error");
                    Log.e(TAG, errorMsg);
                    isValidated = false;
                    if (callback != null) {
                        callback.onVerificationFailure(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(Call<TableVerificationResponse> call, Throwable t) {
                String errorMsg = "Network error during table verification: " + t.getMessage();
                Log.e(TAG, errorMsg);
                isValidated = false;
                if (callback != null) {
                    callback.onVerificationFailure(errorMsg);
                }
            }
        });
    }

    /**
     * Parse JSON string from QR code to extract table ID
     * Expected format: {"table_id": 5, "restaurant_id": 1}
     * @param qrCodeContent The content of the QR code
     * @return Extracted table ID, or -1 if parsing fails
     */
    public static int parseTableIdFromQRCode(String qrCodeContent) {
        try {
            if (qrCodeContent == null || qrCodeContent.trim().isEmpty()) {
                Log.w(TAG, "QR code content is empty");
                return -1;
            }
            
            // Try to parse as JSON
            if (qrCodeContent.startsWith("{") && qrCodeContent.endsWith("}")) {
                com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
                JsonObject jsonObject = parser.parse(qrCodeContent).getAsJsonObject();
                
                if (jsonObject.has("table_id")) {
                    int tableId = jsonObject.get("table_id").getAsInt();
                    Log.d(TAG, "Parsed table_id from JSON QR code: " + tableId);
                    return tableId;
                }
            } else {
                // Try to parse as plain integer (backward compatibility)
                try {
                    int tableId = Integer.parseInt(qrCodeContent.trim());
                    Log.d(TAG, "Parsed table_id from plain text QR code: " + tableId);
                    return tableId;
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Cannot parse table_id as plain integer: " + qrCodeContent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing QR code content: " + e.getMessage(), e);
        }
        
        return -1;
    }

    /**
     * Get the currently validated table ID
     * @return Table ID or null if not validated
     */
    public static Integer getValidatedTableId() {
        return isValidated ? validatedTableId : null;
    }

    /**
     * Get the currently validated table capacity
     * @return Table capacity or null if not validated
     */
    public static Integer getValidatedTableCapacity() {
        return isValidated ? validatedTableCapacity : null;
    }

    /**
     * Get the currently validated table status
     * @return Table status (e.g., "available", "occupied") or null if not validated
     */
    public static String getValidatedTableStatus() {
        return isValidated ? validatedTableStatus : null;
    }

    /**
     * Check if a table has been validated
     * @return true if table information is validated
     */
    public static boolean isTableValidated() {
        return isValidated;
    }

    /**
     * Clear validated table information
     */
    public static void clearValidation() {
        validatedTableId = null;
        validatedTableCapacity = null;
        validatedTableStatus = null;
        isValidated = false;
        Log.d(TAG, "Validated table information cleared");
    }

    /**
     * Callback interface for table verification results
     */
    public interface TableVerificationCallback {
        void onVerificationSuccess(TableVerificationResponse response);
        void onVerificationFailure(String errorMessage);
    }
}
