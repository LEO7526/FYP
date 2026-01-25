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

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.OrderApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.gson.Gson;
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

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    // ✅ 使用实际的 Stripe Publishable Key
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51S56Q5CEiSaWf7Oej0AHB17WDM62OAAM0EofpWf2TbvweOWZRD0Gm1tnC7i1epO4ACYBCnzRfLZaiSPCyVYMxCRk00nT1aG0qV";

    private ProgressBar loadingSpinner;
    private ImageView successIcon;
    private Button payButton;
    private TextView amountText;

    private String dishJson;
    private final List<Map<String, Object>> items = new ArrayList<>();
    private final List<Map<String, Object>> itemsForDisplay = new ArrayList<>();

    private Stripe stripe;
    private String clientSecret;
    private String paymentIntentId;
    private PaymentSheet paymentSheet;

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

        int totalAmount = CartManager.getTotalAmountInCents();
        Log.d(TAG, "onCreate: totalAmount=" + totalAmount);
        amountText.setText(String.format(Locale.getDefault(), "Total: HK$%.2f", totalAmount / 100.0));

        payButton.setOnClickListener(v -> {
            Log.d(TAG, "Pay button clicked");
            payButton.setEnabled(false);
            loadingSpinner.setVisibility(View.VISIBLE);
            createPaymentIntent();
        });
    }

    /**
     * Step 1: Create Payment Intent on backend
     */
    private void createPaymentIntent() {
        Log.d(TAG, "createPaymentIntent: preparing request");

        int totalAmount = CartManager.getTotalAmountInCents();
        String userId = RoleManager.getUserId();

        Map<String, Object> data = new HashMap<>();
        data.put("amount", totalAmount);
        data.put("cid", Integer.parseInt(userId));
        data.put("currency", "hkd");

        Log.d(TAG, "createPaymentIntent: request body = " + new Gson().toJson(data));

        OrderApiService service = RetrofitClient.getClient(this).create(OrderApiService.class);
        Call<Map<String, Object>> call = service.createPaymentIntent(data);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d(TAG, "createPaymentIntent onResponse: success=" + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();
                    clientSecret = (String) responseBody.get("clientSecret");
                    paymentIntentId = (String) responseBody.get("paymentIntentId");

                    if (clientSecret != null) {
                        Log.d(TAG, "Client secret received: " + clientSecret.substring(0, 20) + "...");
                        presentPaymentSheet();
                    } else {
                        showError("Failed to retrieve client secret from backend");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Failed to create payment intent. Code=" + response.code() + ", error=" + errorBody);
                        showError("Error: " + response.code());
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading errorBody", e);
                        showError("Network error");
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "createPaymentIntent onFailure: " + t.getMessage(), t);
                showError("Error: " + t.getMessage());
            }
        });
    }

    /**
     * Step 2: Present Stripe Payment Sheet
     */
    private void presentPaymentSheet() {
        Log.d(TAG, "presentPaymentSheet: showing payment sheet");

        // Configuration without customer (no saved payment methods needed)
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Yummy Restaurant")
                .allowsDelayedPaymentMethods(true)
                .build();

        paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
    }

    /**
     * Step 3: Handle Payment Sheet Result
     */
    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Payment successful!");
            onPaymentSuccess();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Payment canceled");
            showError("Payment cancelled");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e(TAG, "Payment failed");
            showError("Payment failed");
        }
    }

    /**
     * Step 4: On Payment Success
     */
    private void onPaymentSuccess() {
        Log.d(TAG, "onPaymentSuccess: payment completed");

        // Show success icon
        successIcon.setAlpha(0f);
        successIcon.setVisibility(View.VISIBLE);
        successIcon.animate().alpha(1f).setDuration(500).start();

        String userId = RoleManager.getUserId();
        int amount = CartManager.getTotalAmountInCents();

        Log.i(TAG, "Payment success. userId=" + userId + ", amount=" + amount);
        saveOrderToBackend(userId, amount, paymentIntentId);

        new Handler().postDelayed(() -> {
            Log.d(TAG, "Navigating to OrderConfirmationActivity");
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
        Log.d(TAG, "saveOrderToBackend: userId=" + userId + ", amount=" + amount);

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
        }

        orderData.put("cid", customerId);
        orderData.put("ostatus", 1);
        orderData.put("table_number", "not chosen");
        orderData.put("sid", "not applicable");
        orderData.put("payment_method", "stripe");
        orderData.put("payment_intent_id", paymentIntentId);

        // Build order items
        for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
            CartItem cartItem = entry.getKey();
            MenuItem menuItem = cartItem.getMenuItem();
            int quantity = entry.getValue();

            Log.d(TAG, "Adding item to order: " + menuItem.getName() + " x" + quantity);

            Map<String, Object> item = new HashMap<>();
            item.put("item_id", menuItem.getId());
            item.put("qty", quantity);

            if (cartItem.getCustomization() != null) {
                List<Map<String, Object>> customizations = new ArrayList<>();
                // Add customizations here
                item.put("customizations", customizations);
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