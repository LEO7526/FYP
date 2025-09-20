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
import com.example.yummyrestaurant.api.PaymentApiService;
import com.example.yummyrestaurant.api.PaymentIntentResponse;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.RoleManager;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.yummyrestaurant.utils.CartManager;

public class PaymentActivity extends AppCompatActivity {

    private PaymentSheet paymentSheet;
    private String clientSecret;
    private ProgressBar loadingSpinner;
    private ImageView successIcon;
    private Button payButton;
    private TextView amountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        PaymentConfiguration.init(getApplicationContext(), "pk_test_51S56QLC1wirzkW6GoFfOawzrgqNOL5i1DxFatxz2Mr5OAMISZ84QFFkn16763PXc3uDPjpqsQxJLpzfV2q74ke6U00P2dWN9PO");

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        loadingSpinner = findViewById(R.id.loadingSpinner);
        successIcon = findViewById(R.id.successIcon);
        payButton = findViewById(R.id.payButton);
        amountText = findViewById(R.id.amountText);

        int totalAmount = CartManager.getTotalAmountInCents(); // in cents
        amountText.setText("Total: HK$" + (totalAmount / 100.0));

        payButton.setOnClickListener(v -> {
            payButton.setEnabled(false);
            loadingSpinner.setVisibility(View.VISIBLE);
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
                loadingSpinner.setVisibility(View.GONE);
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
                loadingSpinner.setVisibility(View.GONE);
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
            successIcon.setVisibility(View.VISIBLE);
            successIcon.animate().alpha(1f).setDuration(500).start();
            Log.d("PaymentActivity", "Success animation triggered.");



            // Save order to backend
            String userId = RoleManager.getUserId();
            int amount = CartManager.getTotalAmountInCents(); // Use the new method
            String paymentIntentId = clientSecret; // Or extract actual ID if needed
            saveOrderToBackend(userId, amount, paymentIntentId);

            // Clear cart
            CartManager.clearCart();
            Log.d("PaymentActivity", "Cart cleared after successful payment.");

            // Delay and navigate to confirmation screen
            new Handler().postDelayed(() -> {
                Log.i("PaymentActivity", "Navigating to OrderConfirmationActivity.");
                Intent intent = new Intent(PaymentActivity.this, OrderConfirmationActivity.class);
                startActivity(intent);
                finish();
            }, 1500);
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
        orderData.put("cid", RoleManager.getUserId());
        orderData.put("ocost", CartManager.getTotalCost());

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        orderData.put("odeliverdate", timestamp);
        orderData.put("ostatus", 1);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<MenuItem, Integer> entry : CartManager.getCartItems().entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("pid", entry.getKey().getId());
            item.put("oqty", entry.getValue());
            item.put("item_cost", entry.getKey().getPrice());
            items.add(item);

            // Log each item detail
            Log.d("PaymentActivity", "Item added: pid=" + item.get("pid") +
                    ", qty=" + item.get("oqty") +
                    ", cost=" + item.get("item_cost"));
        }

        orderData.put("items", items);

        // Log the full payload
        Log.d("PaymentActivity", "Order payload: " + new com.google.gson.Gson().toJson(orderData));

        OrderApiService service = RetrofitClient.getClient().create(OrderApiService.class);
        Call<ResponseBody> call = service.saveOrder(orderData);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseText = response.body().string(); // Read the actual response
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

        Log.d("PaymentActivity", "Initiated backend save: userId=" + userId + ", amount=" + amount + ", paymentIntentId=" + paymentIntentId);
    }


}