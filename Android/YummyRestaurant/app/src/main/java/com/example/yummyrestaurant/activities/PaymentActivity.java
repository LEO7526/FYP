package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.OrderApiService;
import com.example.yummyrestaurant.api.PaymentApiService;
import com.example.yummyrestaurant.api.PaymentIntentResponse;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.gson.Gson;
import com.stripe.android.PaymentConfiguration;
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

    private PaymentSheet paymentSheet;
    private String clientSecret;
    private ProgressBar loadingSpinner;
    private ImageView successIcon;
    private Button payButton;
    private TextView amountText;

    private String dishJson;

    private final List<Map<String, Object>> items = new ArrayList<>();          // backend payload
    private final List<Map<String, Object>> itemsForDisplay = new ArrayList<>(); // confirmation screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51S56QLC1wirzkW6GoFfOawzrgqNOL5i1DxFatxz2Mr5OAMISZ84QFFkn16763PXc3uDPjpqsQxJLpzfV2q74ke6U00P2dWN9PO"
        );

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        loadingSpinner = findViewById(R.id.loadingSpinner);
        successIcon = findViewById(R.id.successIcon);
        payButton = findViewById(R.id.payButton);
        amountText = findViewById(R.id.amountText);

        int totalAmount = CartManager.getTotalAmountInCents(); // in cents
        Log.d("PaymentActivity", "Cart items: " + CartManager.getCartItems().size());

        amountText.setText(String.format(Locale.getDefault(), "Total: HK$%.2f", totalAmount / 100.0));

        payButton.setOnClickListener(v -> {
            payButton.setEnabled(false);
            loadingSpinner.setVisibility(android.view.View.VISIBLE);
            fetchClientSecret();
        });
    }

    private void fetchClientSecret() {
        Map<String, Object> data = new HashMap<>();
        int totalAmount = CartManager.getTotalAmountInCents();
        data.put("amount", totalAmount);

        PaymentApiService service = RetrofitClient.getClient().create(PaymentApiService.class);
        Call<PaymentIntentResponse> call = service.createPaymentIntent(data);

        call.enqueue(new Callback<PaymentIntentResponse>() {
            @Override
            public void onResponse(Call<PaymentIntentResponse> call, Response<PaymentIntentResponse> response) {
                loadingSpinner.setVisibility(android.view.View.GONE);
                payButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    clientSecret = response.body().getClientSecret();
                    presentPaymentSheet();
                } else {
                    Toast.makeText(PaymentActivity.this, "Failed to get client secret", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentIntentResponse> call, Throwable t) {
                loadingSpinner.setVisibility(android.view.View.GONE);
                payButton.setEnabled(true);
                Toast.makeText(PaymentActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
                clientSecret,
                new PaymentSheet.Configuration.Builder("Yummy Restaurant").build()
        );
    }

    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            Log.i("PaymentActivity", "Payment completed successfully.");

            // Show success animation
            successIcon.setAlpha(0f);
            successIcon.setVisibility(android.view.View.VISIBLE);
            successIcon.animate().alpha(1f).setDuration(500).start();

            // Save order to backend
            String userId = RoleManager.getUserId();
            int amount = CartManager.getTotalAmountInCents();
            String paymentIntentId = clientSecret;
            saveOrderToBackend(userId, amount, paymentIntentId);

            new Handler().postDelayed(() -> {
                Log.i("PaymentActivity", "Navigating to OrderConfirmationActivity.");
                Intent intent = new Intent(PaymentActivity.this, OrderConfirmationActivity.class);

                // Pass order details
                intent.putExtra("customerId", RoleManager.getUserId());
                intent.putExtra("totalAmount", amount);
                intent.putExtra("itemCount", items.size());
                intent.putExtra("dishJson", dishJson);

                startActivity(intent);
                finish();
            }, 1500);

            // Clear cart
            CartManager.clearCart();
            Log.d("PaymentActivity", "Cart cleared after successful payment.");

        } else if (result instanceof PaymentSheetResult.Canceled) {
            Log.w("PaymentActivity", "Payment was canceled by the user.");
            Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            Throwable error = ((PaymentSheetResult.Failed) result).getError();
            Log.e("PaymentActivity", "Payment failed: " + error.getLocalizedMessage(), error);
            Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrderToBackend(String userId, int amount, String paymentIntentId) {
        Map<String, Object> orderData = new HashMap<>();

        boolean isStaff = RoleManager.isStaff();
        int customerId;

        if (isStaff) {
            customerId = 0; // Walk-in customer
            orderData.put("sid", RoleManager.getUserId()); // Staff ID
            orderData.put("table_number", RoleManager.getAssignedTable()); // Table number
        } else {
            customerId = Integer.parseInt(userId); // Regular customer
        }

        orderData.put("cid", customerId);
        orderData.put("ostatus", 1); // Order status
        orderData.put("table_number", "not chosen");
        orderData.put("sid", "not applicable");

        // ✅ Iterate over CartItem, not MenuItem
        for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
            CartItem cartItem = entry.getKey();
            MenuItem menuItem = cartItem.getMenuItem();
            int quantity = entry.getValue();

            // Backend payload
            Map<String, Object> item = new HashMap<>();
            item.put("item_id", menuItem.getId());
            item.put("qty", quantity);
            items.add(item);

            // Display payload
            Map<String, Object> displayItem = new HashMap<>();
            displayItem.put("item_id", menuItem.getId());
            displayItem.put("qty", quantity);
            displayItem.put("dish_name", menuItem.getName());
            displayItem.put("dish_price", menuItem.getPrice());

            // Include customization if present
            if (cartItem.getCustomization() != null) {
                displayItem.put("spice_level", cartItem.getCustomization().getSpiceLevel());
                displayItem.put("extra_notes", cartItem.getCustomization().getExtraNotes());
            }

            itemsForDisplay.add(displayItem);
        }

        orderData.put("items", items);

        dishJson = new Gson().toJson(itemsForDisplay);
        Log.d("PaymentActivity", "Order payload: " + new Gson().toJson(orderData));

        OrderApiService service = RetrofitClient.getClient().create(OrderApiService.class);
        Call<ResponseBody> call = service.saveOrder(orderData);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseText = response.body() != null ? response.body().string() : "";
                        Log.i("PaymentActivity", "Order saved successfully. Response: " + responseText);
                        Toast.makeText(PaymentActivity.this, "Order saved!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("PaymentActivity", "Failed to read response body", e);
                    }
                } else {
                    Log.w("PaymentActivity", "Order save failed. Response code: " + response.code());
                    Toast.makeText(PaymentActivity.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("PaymentActivity", "Order save error: " + t.getMessage(), t);
                Toast.makeText(PaymentActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Log.d("PaymentActivity", "Initiated backend save: customerId=" + customerId + ", items=" + items.size());
    }
}