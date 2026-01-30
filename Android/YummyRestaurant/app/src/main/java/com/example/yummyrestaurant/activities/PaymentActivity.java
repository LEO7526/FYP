package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.OrderApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.gson.Gson;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends ThemeBaseActivity {

    private static final String TAG = "PaymentActivity";

    // ✅ 使用实际的 Stripe Publishable Key
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51S56Q5CEiSaWf7Oej0AHB17WDM62OAAM0EofpWf2TbvweOWZRD0Gm1tnC7i1epO4ACYBCnzRfLZaiSPCyVYMxCRk00nT1aG0qV";

    private ProgressBar loadingSpinner;
    private ImageView successIcon;
    private Button payButton;
    private TextView amountText;
    private RadioGroup paymentMethodGroup;
    private RadioButton rbCard, rbAlipayHK, rbCash;

    private String dishJson;
    private final List<Map<String, Object>> items = new ArrayList<>();
    private final List<Map<String, Object>> itemsForDisplay = new ArrayList<>();

    private Stripe stripe;
    private String clientSecret;
    private String paymentIntentId;
    private PaymentSheet paymentSheet;
    private String selectedPaymentMethod = "card"; // Default payment method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Log.d(TAG, "onCreate: PaymentActivity started");

        // Initialize Stripe with publishable key
        PaymentConfiguration.init(this, STRIPE_PUBLISHABLE_KEY);
        stripe = new Stripe(this, STRIPE_PUBLISHABLE_KEY);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        loadingSpinner = findViewById(R.id.loadingSpinner);
        successIcon = findViewById(R.id.successIcon);
        payButton = findViewById(R.id.payButton);
        amountText = findViewById(R.id.amountText);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        rbCard = findViewById(R.id.rbCard);
        rbAlipayHK = findViewById(R.id.rbAlipayHK);
        rbCash = findViewById(R.id.rbCash);
        
        // Hide alternative payment method - PaymentSheet only supports card payments
        rbAlipayHK.setVisibility(View.GONE);
        // Hide the radio group label if applicable
        View paymentMethodLabel = findViewById(R.id.paymentMethodLabel);
        if (paymentMethodLabel != null) {
            paymentMethodLabel.setVisibility(View.VISIBLE);
        }

        int totalAmount = CartManager.getTotalAmountInCents();
        Log.d(TAG, "onCreate: totalAmount=" + totalAmount);
        amountText.setText(String.format(Locale.getDefault(), "Total: HK$%.2f", totalAmount / 100.0));

        // ✅ Apply theme colors based on user role
        applyThemeColors();

        // Setup payment method selection - card or cash
        selectedPaymentMethod = "card"; // Default to card payment
        rbCard.setChecked(true);
        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCard) {
                selectedPaymentMethod = "card";
                Log.d(TAG, "Payment method: Card");
                Log.i(TAG, ">>> User payment method: CARD");
            } else if (checkedId == R.id.rbCash) {
                selectedPaymentMethod = "cash";
                Log.d(TAG, "Payment method: Cash at Front Desk");
                Log.i(TAG, ">>> User payment method: CASH");
            } else {
                selectedPaymentMethod = "card";
                Log.d(TAG, "Payment method: Card (default)");
            }
        });

        payButton.setOnClickListener(v -> {
            Log.d(TAG, "Pay button clicked with method: " + selectedPaymentMethod);
            Log.i(TAG, ">>> PAY BUTTON CLICKED | Method: " + selectedPaymentMethod);
            
            if ("cash".equals(selectedPaymentMethod)) {
                // Direct cash payment - no Stripe involved
                onCashPaymentSelected();
            } else {
                // Card payment - use Stripe
                payButton.setEnabled(false);
                loadingSpinner.setVisibility(View.VISIBLE);
                createPaymentIntent();
            }
        });
    }

    /**
     * Apply theme colors based on user role
     * Orange for customers, Blue for staff
     */
    private void applyThemeColors() {
        boolean isStaff = RoleManager.isStaff();
        int themeColor;
        String theme;

        if (isStaff) {
            // Blue theme for staff
            themeColor = android.graphics.Color.parseColor("#1976D2"); // Material Blue
            theme = "STAFF (Blue)";
            Log.d(TAG, "applyThemeColors: Applied BLUE theme for staff");
        } else {
            // Orange theme for customers
            themeColor = android.graphics.Color.parseColor("#FF6F00"); // Orange
            theme = "CUSTOMER (Orange)";
            Log.d(TAG, "applyThemeColors: Applied ORANGE theme for customers");
        }

        // Apply to Pay button
        payButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(themeColor));

        // Apply to amount text
        amountText.setTextColor(themeColor);

        // Apply to payment method label
        View paymentMethodLabel = findViewById(R.id.paymentMethodLabel);
        if (paymentMethodLabel instanceof TextView) {
            ((TextView) paymentMethodLabel).setTextColor(themeColor);
        }

        Log.i(TAG, ">>> Theme Applied: " + theme);
    }

    /**
     * Handle cash payment at front desk
     */
    private void onCashPaymentSelected() {
        Log.d(TAG, "onCashPaymentSelected: Processing cash payment");
        Log.i(TAG, ">>> CASH PAYMENT SELECTED");
        
        String userId = RoleManager.getUserId();
        int amount = CartManager.getTotalAmountInCents();
        
        Log.i(TAG, "Cash payment. userId=" + userId + ", amount=" + amount);
        Log.d(TAG, "onCashPaymentSelected: Saving order to backend");
        
        // Generate a fake payment intent ID for cash orders
        paymentIntentId = "cash_" + System.currentTimeMillis();
        
        // Save order with cash payment method and ostatus=2 (Done, pending payment)
        saveOrderToBackend(userId, amount, paymentIntentId);
        
        // Show success icon
        successIcon.setAlpha(0f);
        successIcon.setVisibility(View.VISIBLE);
        successIcon.animate().alpha(1f).setDuration(500).start();
        
        new Handler().postDelayed(() -> {
            Log.d(TAG, "Navigating to OrderConfirmationActivity");
            Log.i(TAG, ">>> REDIRECTING: Moving to OrderConfirmationActivity");
            Intent confirmIntent = new Intent(PaymentActivity.this, OrderConfirmationActivity.class);
            confirmIntent.putExtra("customerId", userId);
            confirmIntent.putExtra("totalAmount", amount);
            confirmIntent.putExtra("itemCount", items.size());
            confirmIntent.putExtra("dishJson", dishJson);
            confirmIntent.putExtra("paymentMethod", "cash");
            startActivity(confirmIntent);
            finish();
        }, 1500);
        
        CartManager.clearCart();
    }

    /**
     * Step 1: Create Payment Intent on backend (for card payments only)
     */
    private void createPaymentIntent() {
        Log.d(TAG, "createPaymentIntent: preparing request");
        Log.d(TAG, "createPaymentIntent: selectedPaymentMethod = " + selectedPaymentMethod);

        int totalAmount = CartManager.getTotalAmountInCents();
        String userId = RoleManager.getUserId();

        Map<String, Object> data = new HashMap<>();
        data.put("amount", totalAmount);
        data.put("cid", Integer.parseInt(userId));
        data.put("currency", "hkd");
        data.put("paymentMethod", selectedPaymentMethod);

        Log.d(TAG, "createPaymentIntent: request body = " + new Gson().toJson(data));
        Log.i(TAG, "createPaymentIntent: Sending request with paymentMethod=" + selectedPaymentMethod + ", amount=" + totalAmount);

        OrderApiService service = RetrofitClient.getClient(this).create(OrderApiService.class);
        Call<Map<String, Object>> call = service.createPaymentIntent(data);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d(TAG, "createPaymentIntent onResponse: success=" + response.isSuccessful());
                Log.d(TAG, "onResponse: HTTP Code = " + response.code());
                Log.d(TAG, "onResponse: isSuccessful = " + response.isSuccessful());
                Log.d(TAG, "onResponse: response message = " + response.message());
                Log.d(TAG, "onResponse: selectedPaymentMethod = " + selectedPaymentMethod);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();
                    
                    Log.d(TAG, "onResponse: response body keys = " + responseBody.keySet().toString());
                    
                    // Check if backend returned a fallback flag
                    Boolean fallback = (Boolean) responseBody.get("fallback");
                    String fallbackReason = (String) responseBody.get("fallbackReason");
                    
                    if (fallback != null && fallback) {
                        Log.w(TAG, ">>> FALLBACK DETECTED FROM SERVER: " + fallbackReason);
                        selectedPaymentMethod = "card";
                        rbCard.setChecked(true);
                    }
                    
                    clientSecret = (String) responseBody.get("clientSecret");
                    paymentIntentId = (String) responseBody.get("paymentIntentId");
                    
                    Log.d(TAG, "onResponse: clientSecret = " + (clientSecret != null ? clientSecret.substring(0, 20) + "..." : "null"));
                    Log.d(TAG, "onResponse: clientSecret length = " + (clientSecret != null ? clientSecret.length() : 0));
                    Log.d(TAG, "onResponse: paymentIntentId = " + paymentIntentId);
                    Log.d(TAG, "onResponse: final selectedPaymentMethod after fallback = " + selectedPaymentMethod);

                    if (clientSecret != null && !clientSecret.isEmpty()) {
                        Log.d(TAG, "Client secret received: " + clientSecret.substring(0, 20) + "...");
                        Log.i(TAG, ">>> SUCCESS: Payment Intent created successfully");
                        Log.i(TAG, ">>> About to present PaymentSheet with clientSecret: " + clientSecret.substring(0, 15) + "...");
                        Log.i(TAG, ">>> Using payment method: " + selectedPaymentMethod);
                        presentPaymentSheet();
                    } else {
                        Log.e(TAG, ">>> ERROR: clientSecret is null or empty");
                        Log.e(TAG, ">>> clientSecret value: " + (clientSecret != null ? clientSecret : "null"));
                        showError("Failed to retrieve client secret from backend");
                        resetPaymentButton();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Failed to create payment intent. Code=" + response.code() + ", error=" + errorBody);
                        Log.e(TAG, "Full error response: " + errorBody);
                        Log.e(TAG, "HTTP Headers: " + response.headers().toString());
                        
                        // If Alipay is not available, fall back to Card
                        if (response.code() == 400 && selectedPaymentMethod.equals("alipay_hk")) {
                            Log.w(TAG, ">>> ALIPAY ERROR: HTTP 400 received for Alipay, falling back to Card");
                            Log.w(TAG, ">>> Error details: " + errorBody);
                            Toast.makeText(PaymentActivity.this, "Alipay currently unavailable. Using Card instead.", Toast.LENGTH_LONG).show();
                            selectedPaymentMethod = "card";
                            rbCard.setChecked(true);
                            payButton.setEnabled(true);
                            loadingSpinner.setVisibility(View.GONE);
                            Log.w(TAG, ">>> RETRY: Attempting again with Card payment method");
                            return;
                        }
                        
                        showError("Payment setup failed. Please try again.");
                        resetPaymentButton();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading errorBody", e);
                        showError("Network error");
                        resetPaymentButton();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "createPaymentIntent onFailure: " + t.getMessage(), t);
                showError("Error: " + t.getMessage());
                resetPaymentButton();
            }
        });
    }

    /**
     * Step 2: Present Stripe Payment Sheet
     */
    private void presentPaymentSheet() {
        Log.d(TAG, "presentPaymentSheet: showing payment sheet");
        Log.d(TAG, "presentPaymentSheet: clientSecret = " + (clientSecret != null ? clientSecret.substring(0, 20) + "..." : "null"));
        
        // Pre-validation checks
        Log.d(TAG, "presentPaymentSheet: Pre-validation checks starting");
        
        if (clientSecret == null || clientSecret.isEmpty()) {
            Log.e(TAG, ">>> CRITICAL: clientSecret is null or empty!");
            showError("Failed to initialize payment. Please try again.");
            resetPaymentButton();
            return;
        }
        
        Log.d(TAG, "presentPaymentSheet: clientSecret validation passed");
        Log.d(TAG, "presentPaymentSheet: selectedPaymentMethod = " + selectedPaymentMethod);
        Log.d(TAG, "presentPaymentSheet: paymentIntentId = " + paymentIntentId);

        try {
            Log.d(TAG, "presentPaymentSheet: Creating PaymentSheet configuration");
            Log.d(TAG, "presentPaymentSheet: selectedPaymentMethod before config = " + selectedPaymentMethod);
            
            // Configuration for payment sheet with HK billing details for Alipay support
            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Yummy Restaurant")
                    .allowsDelayedPaymentMethods(true)
                    .defaultBillingDetails(
                            new PaymentSheet.BillingDetails.Builder()
                                    .address(new PaymentSheet.Address.Builder()
                                            .country("HK")
                                            .build())
                                    .build()
                    )
                    .build();

            Log.d(TAG, "presentPaymentSheet: Configuration created successfully");
            Log.i(TAG, ">>> ABOUT TO CALL PaymentSheet.presentWithPaymentIntent()");
            Log.i(TAG, ">>> METHOD: " + selectedPaymentMethod + " | SECRET: " + (clientSecret != null ? clientSecret.substring(0, 15) + "..." : "null"));
            
            paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
            
            Log.i(TAG, ">>> SUCCESS: PaymentSheet presented successfully");
        } catch (java.util.NoSuchElementException e) {
            Log.e(TAG, ">>> CRITICAL: NoSuchElementException caught!");
            Log.e(TAG, ">>> This means payment methods list is empty for: " + selectedPaymentMethod);
            Log.e(TAG, ">>> Error message: " + e.getMessage());
            Log.e(TAG, ">>> Full stack trace:", e);
            
            // Force fallback to Card when payment methods unavailable
            if (!selectedPaymentMethod.equals("card")) {
                Log.w(TAG, ">>> AUTO-FALLBACK: Switching from " + selectedPaymentMethod + " to card (method not supported by Stripe)");
                selectedPaymentMethod = "card";
                rbCard.setChecked(true);
                Toast.makeText(this, "Switched to Card payment (only option available)", Toast.LENGTH_LONG).show();
                
                // Retry with Card
                Log.i(TAG, ">>> RETRY: Requesting new PaymentIntent with Card method");
                payButton.setEnabled(true);
                loadingSpinner.setVisibility(View.GONE);
                createPaymentIntent();
            } else {
                Log.e(TAG, ">>> FATAL: NoSuchElementException occurred even with Card payment");
                Log.e(TAG, ">>> This might be a Stripe Dashboard configuration issue");
                showError("Payment service temporarily unavailable. Please try again later or contact support.");
                resetPaymentButton();
            }
        } catch (Exception e) {
            Log.e(TAG, ">>> FATAL: Unexpected exception in presentPaymentSheet");
            Log.e(TAG, "FATAL: Error presenting payment sheet: " + e.getMessage(), e);
            Log.e(TAG, "FATAL: Exception type: " + e.getClass().getName(), e);
            Log.e(TAG, "FATAL: Exception cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"), e);
            Log.e(TAG, "FATAL: Stack trace: " + android.util.Log.getStackTraceString(e));
            
            // Check if error message indicates payment method issue
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errorMsg.toLowerCase().contains("alipay") || errorMsg.toLowerCase().contains("payment method")) {
                Log.w(TAG, ">>> Error detected as payment method related");
                if (!selectedPaymentMethod.equals("card")) {
                    Log.w(TAG, ">>> AUTO-FALLBACK: Attempting Card payment due to error");
                    selectedPaymentMethod = "card";
                    rbCard.setChecked(true);
                    Toast.makeText(this, "Switched to Card payment", Toast.LENGTH_SHORT).show();
                    payButton.setEnabled(true);
                    loadingSpinner.setVisibility(View.GONE);
                    createPaymentIntent();
                    return;
                }
            }
            
            showError("Payment method unavailable. Please try Card payment.");
            
            // Fall back to Card
            if (selectedPaymentMethod.equals("alipay_hk")) {
                Log.w(TAG, "Falling back from Alipay to Card due to exception");
                selectedPaymentMethod = "card";
                rbCard.setChecked(true);
                Toast.makeText(this, "Switched to Card payment", Toast.LENGTH_SHORT).show();
            }
            resetPaymentButton();
        }
    }

    /**
     * Reset payment button state after error
     */
    private void resetPaymentButton() {
        payButton.setEnabled(true);
        loadingSpinner.setVisibility(View.GONE);
    }

    /**
     * Step 3: Handle Payment Sheet Result
     */
    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        Log.d(TAG, "onPaymentSheetResult called");
        Log.d(TAG, "onPaymentSheetResult: result type = " + (paymentSheetResult != null ? paymentSheetResult.getClass().getSimpleName() : "null"));
        
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, ">>> PAYMENT COMPLETED!");
            Log.i(TAG, ">>> Payment successful with method: " + selectedPaymentMethod);
            onPaymentSuccess();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, ">>> PAYMENT CANCELED");
            Log.w(TAG, ">>> User canceled payment with method: " + selectedPaymentMethod);
            showError("Payment cancelled");
            resetPaymentButton();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            Log.e(TAG, ">>> PAYMENT FAILED");
            Log.e(TAG, ">>> Failed error: " + failedResult.getError().getMessage());
            Log.e(TAG, ">>> Failed with method: " + selectedPaymentMethod);
            showError("Payment failed");
            resetPaymentButton();
        } else {
            Log.e(TAG, ">>> UNKNOWN: Unknown payment sheet result type");
            resetPaymentButton();
        }
    }

    /**
     * Step 4: On Payment Success
     */
    private void onPaymentSuccess() {
        Log.d(TAG, "onPaymentSuccess: payment completed");
        Log.i(TAG, ">>> TRANSACTION SUCCESS");
        Log.i(TAG, ">>> Payment method used: " + selectedPaymentMethod);
        Log.i(TAG, ">>> Payment intent ID: " + paymentIntentId);

        // Show success icon
        successIcon.setAlpha(0f);
        successIcon.setVisibility(View.VISIBLE);
        successIcon.animate().alpha(1f).setDuration(500).start();

        String userId = RoleManager.getUserId();
        int amount = CartManager.getTotalAmountInCents();

        Log.i(TAG, "Payment success. userId=" + userId + ", amount=" + amount + ", method=" + selectedPaymentMethod);
        Log.d(TAG, "onPaymentSuccess: Saving order to backend");
        saveOrderToBackend(userId, amount, paymentIntentId);

        new Handler().postDelayed(() -> {
            Log.d(TAG, "Navigating to OrderConfirmationActivity");
            Log.i(TAG, ">>> REDIRECTING: Moving to OrderConfirmationActivity");
            Intent confirmIntent = new Intent(PaymentActivity.this, OrderConfirmationActivity.class);
            confirmIntent.putExtra("customerId", userId);
            confirmIntent.putExtra("totalAmount", amount);
            confirmIntent.putExtra("itemCount", items.size());
            confirmIntent.putExtra("dishJson", dishJson);
            startActivity(confirmIntent);
            finish();
        }, 1500);

        CartManager.clearCart();
    }

    /**
     * Step 5: Save order to backend after successful payment
     */
    private void saveOrderToBackend(String userId, int amount, String paymentIntentId) {
        Log.d(TAG, "saveOrderToBackend: userId=" + userId + ", amount=" + amount + ", paymentIntentId=" + paymentIntentId);
        Log.d(TAG, "saveOrderToBackend: selectedPaymentMethod=" + selectedPaymentMethod);

        Map<String, Object> orderData = new HashMap<>();
        boolean isStaff = RoleManager.isStaff();
        int customerId;

        if (isStaff) {
            customerId = 0;
            orderData.put("sid", RoleManager.getUserId());
            orderData.put("table_number", RoleManager.getAssignedTable());
            Log.d(TAG, "saveOrderToBackend: staff order, sid=" + RoleManager.getUserId());
        } else {
            customerId = Integer.parseInt(userId);
            Log.d(TAG, "saveOrderToBackend: customer order, cid=" + customerId);
        }

        orderData.put("cid", customerId);
        
        // ✅ Set ostatus based on payment method
        // ostatus: 1=Pending, 2=Done (unpaid), 3=Paid, 4=Cancelled
        int ostatus = ("cash".equals(selectedPaymentMethod)) ? 2 : 3; // 2 for cash (pending payment), 3 for card (paid)
        orderData.put("ostatus", ostatus);
        Log.d(TAG, "saveOrderToBackend: ostatus=" + ostatus + " (payment_method=" + selectedPaymentMethod + ")");
        
        // ✅ Add order_type (dine_in or takeaway)
        String orderType = CartManager.getOrderType();
        Log.d(TAG, "saveOrderToBackend: order_type=" + orderType);
        orderData.put("order_type", orderType);
        
        // ✅ Add table_number only for dine_in orders
        if ("dine_in".equals(orderType)) {
            Integer tableNumber = CartManager.getTableNumber();
            if (tableNumber != null && tableNumber > 0) {
                orderData.put("table_number", tableNumber);
                Log.d(TAG, "saveOrderToBackend: dine_in order, table_number=" + tableNumber);
            } else {
                orderData.put("table_number", null);
                Log.d(TAG, "saveOrderToBackend: dine_in order but no valid table_number");
            }
        } else {
            orderData.put("table_number", null);
            Log.d(TAG, "saveOrderToBackend: takeaway order, table_number=null");
        }
        
        orderData.put("sid", "not applicable");
        orderData.put("payment_method", selectedPaymentMethod);
        
        // ✅ Only set payment_intent_id for card payments
        if ("card".equals(selectedPaymentMethod)) {
            orderData.put("payment_intent_id", paymentIntentId);
            Log.d(TAG, "saveOrderToBackend: card payment, payment_intent_id=" + paymentIntentId);
        } else if ("cash".equals(selectedPaymentMethod)) {
            orderData.put("payment_intent_id", "cash_" + System.currentTimeMillis());
            Log.d(TAG, "saveOrderToBackend: cash payment, generated pseudo payment_intent_id");
        }
        
        Log.d(TAG, "saveOrderToBackend: order data setup complete");

        // Build order items
        for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
            CartItem cartItem = entry.getKey();
            MenuItem menuItem = cartItem.getMenuItem();
            int quantity = entry.getValue();

            Log.d(TAG, "Adding item to order: " + menuItem.getName() + " (ID:" + menuItem.getId() + ") x" + quantity);

            Map<String, Object> item = new HashMap<>();
            item.put("item_id", menuItem.getId());
            item.put("qty", quantity);

            if (cartItem.getCustomization() != null) {
                Map<String, Object> customization = new HashMap<>();
                
                // Add customization details
                List<Map<String, Object>> customizationDetails = new ArrayList<>();
                if (cartItem.getCustomization().getCustomizationDetails() != null) {
                    for (com.example.yummyrestaurant.models.OrderItemCustomization detail : cartItem.getCustomization().getCustomizationDetails()) {
                        Map<String, Object> customizationDetail = new HashMap<>();
                        customizationDetail.put("option_id", detail.getOptionId());
                        customizationDetail.put("group_id", detail.getGroupId());
                        customizationDetail.put("selected_value_ids", detail.getSelectedValueIds());
                        customizationDetail.put("selected_values", detail.getSelectedChoices());
                        customizationDetail.put("text_value", detail.getTextValue());
                        customizationDetails.add(customizationDetail);
                    }
                }
                customization.put("customization_details", customizationDetails);
                customization.put("extra_notes", cartItem.getCustomization().getExtraNotes());
                item.put("customization", customization);
            }

            items.add(item);

            Map<String, Object> displayItem = new HashMap<>();
            displayItem.put("item_id", menuItem.getId());
            displayItem.put("qty", quantity);
            displayItem.put("dish_name", menuItem.getName());
            displayItem.put("dish_price", menuItem.getPrice());

            if (cartItem.getCustomization() != null) {
                displayItem.put("spice_level", cartItem.getCustomization().getSpiceLevel());
                displayItem.put("extra_notes", cartItem.getCustomization().getExtraNotes());
            }

            itemsForDisplay.add(displayItem);
        }

        orderData.put("items", items);
        dishJson = new Gson().toJson(itemsForDisplay);
        orderData.put("total_amount", amount);
        orderData.put("coupon_id", null);

        OrderApiService service = RetrofitClient.getClient(this).create(OrderApiService.class);
        Call<ResponseBody> call = service.saveOrderDirect(orderData);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "saveOrderToBackend onResponse: success=" + response.isSuccessful());
                if (response.isSuccessful()) {
                    try {
                        String responseText = response.body() != null ? response.body().string() : "";
                        Log.i(TAG, "Order saved successfully. Response: " + responseText);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read response body", e);
                    }
                } else {
                    Log.e(TAG, "Failed to save order. Code=" + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "saveOrderToBackend onFailure: " + t.getMessage(), t);
            }
        });
    }

    private void showError(String message) {
        loadingSpinner.setVisibility(View.GONE);
        payButton.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: " + message);
    }
}