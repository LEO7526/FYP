package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.net.Uri;
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
import com.example.yummyrestaurant.api.PaymentUrlResponse;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.gson.Gson;

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

    private ProgressBar loadingSpinner;
    private ImageView successIcon;
    private Button payButton;
    private TextView amountText;

    private String dishJson;
    private final List<Map<String, Object>> items = new ArrayList<>();
    private final List<Map<String, Object>> itemsForDisplay = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        loadingSpinner = findViewById(R.id.loadingSpinner);
        successIcon = findViewById(R.id.successIcon);
        payButton = findViewById(R.id.payButton);
        amountText = findViewById(R.id.amountText);

        int totalAmount = CartManager.getTotalAmountInCents();
        amountText.setText(String.format(Locale.getDefault(), "Total: HK$%.2f", totalAmount / 100.0));

        payButton.setOnClickListener(v -> {
            payButton.setEnabled(false);
            loadingSpinner.setVisibility(android.view.View.VISIBLE);
            fetchPayDollarUrl();
        });
    }

    private void fetchPayDollarUrl() {
        Map<String, Object> data = new HashMap<>();
        int totalAmount = CartManager.getTotalAmountInCents();
        data.put("amount", totalAmount);
        data.put("currency", "344"); // HKD
        data.put("payType", "ALIPAY"); // or FPS, OCTOPUS, etc.

        PaymentApiService service = RetrofitClient.getClient().create(PaymentApiService.class);
        Call<PaymentUrlResponse> call = service.getPayDollarUrl(data);

        call.enqueue(new Callback<PaymentUrlResponse>() {
            @Override
            public void onResponse(Call<PaymentUrlResponse> call, Response<PaymentUrlResponse> response) {
                loadingSpinner.setVisibility(android.view.View.GONE);
                payButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String paymentUrl = response.body().getPaymentUrl();
                    redirectToPayDollar(paymentUrl);
                } else {
                    Toast.makeText(PaymentActivity.this, "Failed to get payment URL", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentUrlResponse> call, Throwable t) {
                loadingSpinner.setVisibility(android.view.View.GONE);
                payButton.setEnabled(true);
                Toast.makeText(PaymentActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToPayDollar(String paymentUrl) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
        startActivity(browserIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Uri data = intent.getData();
        if (data != null && data.getQueryParameter("status") != null) {
            String status = data.getQueryParameter("status");
            if ("success".equals(status)) {
                successIcon.setAlpha(0f);
                successIcon.setVisibility(android.view.View.VISIBLE);
                successIcon.animate().alpha(1f).setDuration(500).start();

                String userId = RoleManager.getUserId();
                int amount = CartManager.getTotalAmountInCents();
                saveOrderToBackend(userId, amount, "PayDollar");

                new Handler().postDelayed(() -> {
                    Intent confirmIntent = new Intent(PaymentActivity.this, OrderConfirmationActivity.class);
                    confirmIntent.putExtra("customerId", userId);
                    confirmIntent.putExtra("totalAmount", amount);
                    confirmIntent.putExtra("itemCount", items.size());
                    confirmIntent.putExtra("dishJson", dishJson);
                    startActivity(confirmIntent);
                    finish();
                }, 1500);

                CartManager.clearCart();
            } else {
                Toast.makeText(this, "Payment failed or canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveOrderToBackend(String userId, int amount, String paymentIntentId) {
        Map<String, Object> orderData = new HashMap<>();
        boolean isStaff = RoleManager.isStaff();
        int customerId;

        if (isStaff) {
            customerId = 0;
            orderData.put("sid", RoleManager.getUserId());
            orderData.put("table_number", RoleManager.getAssignedTable());
        } else {
            customerId = Integer.parseInt(userId);
        }

        orderData.put("cid", customerId);
        orderData.put("ostatus", 1);
        orderData.put("table_number", "not chosen");
        orderData.put("sid", "not applicable");

        for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
            CartItem cartItem = entry.getKey();
            MenuItem menuItem = cartItem.getMenuItem();
            int quantity = entry.getValue();

            Map<String, Object> item = new HashMap<>();
            item.put("item_id", menuItem.getId());
            item.put("qty", quantity);
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
                    Toast.makeText(PaymentActivity.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}