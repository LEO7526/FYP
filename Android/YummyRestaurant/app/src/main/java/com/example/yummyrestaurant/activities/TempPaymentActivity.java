package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
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

public class TempPaymentActivity extends AppCompatActivity {

    private static final String TAG = "TempPaymentActivity";

    private ProgressBar loadingSpinner;
    private ImageView successIcon;
    private Button confirmButton;
    private TextView amountText;

    private String dishJson;
    private final List<Map<String, Object>> items = new ArrayList<>();
    private final List<Map<String, Object>> itemsForDisplay = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_payment);

        loadingSpinner = findViewById(R.id.loadingSpinner);
        successIcon = findViewById(R.id.successIcon);
        confirmButton = findViewById(R.id.confirmButton1);
        amountText = findViewById(R.id.amountText);

        int totalAmount = CartManager.getTotalAmountInCents();
        amountText.setText(String.format(Locale.getDefault(), "Total: HK$%.2f", totalAmount / 100.0));

        confirmButton.setOnClickListener(v -> {
            confirmButton.setEnabled(false);
            loadingSpinner.setVisibility(View.VISIBLE);
            saveOrderDirectly();
        });
    }

    /**
     * Build payload and call backend endpoint to save order directly into orders and order_items.
     * Expected server behavior: create a new row in orders and rows in order_items; return success and order id.
     */
    private void saveOrderDirectly() {
        Log.d(TAG, "saveOrderDirectly: preparing payload");

        boolean isStaff = RoleManager.isStaff();
        String userId = RoleManager.getUserId();
        int customerId = isStaff ? 0 : Integer.parseInt(userId);
        int totalAmount = CartManager.getTotalAmountInCents();

        // Build order header map
        Map<String, Object> orderHeader = new HashMap<>();
        orderHeader.put("cid", customerId);              // customer id (0 for walk-in/staff)
        orderHeader.put("ostatus", 1);                   // default order status
        orderHeader.put("odate", System.currentTimeMillis()); // optional: timestamp, adapt to backend format
        orderHeader.put("orderRef", "temp_order_" + System.currentTimeMillis()); // unique ref
        if (isStaff) {
            orderHeader.put("sid", Integer.parseInt(RoleManager.getUserId()));
            orderHeader.put("table_number", RoleManager.getAssignedTable());
        } else {
            orderHeader.put("sid", null);
            orderHeader.put("table_number", "not chosen");
        }

        // Build order_items array
        items.clear();
        itemsForDisplay.clear();
        for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
            CartItem cartItem = entry.getKey();
            MenuItem menuItem = cartItem.getMenuItem();
            int qty = entry.getValue();

            Map<String, Object> item = new HashMap<>();
            item.put("item_id", menuItem.getId());
            item.put("qty", qty);
            items.add(item);

            Map<String, Object> display = new HashMap<>();
            display.put("item_id", menuItem.getId());
            display.put("qty", qty);
            display.put("dish_name", menuItem.getName());
            display.put("dish_price", menuItem.getPrice());
            if (cartItem.getCustomization() != null) {
                display.put("spice_level", cartItem.getCustomization().getSpiceLevel());
                display.put("extra_notes", cartItem.getCustomization().getExtraNotes());
            }
            itemsForDisplay.add(display);
        }

        orderHeader.put("items", items);
        orderHeader.put("total_amount", totalAmount); // cents
        dishJson = new Gson().toJson(itemsForDisplay);

        // Call the backend via Retrofit. Make sure OrderApiService has an endpoint like saveOrderDirect(Map)
        OrderApiService service = RetrofitClient.getClient().create(OrderApiService.class);
        Call<ResponseBody> call = service.saveOrderDirect(orderHeader);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingSpinner.setVisibility(View.GONE);
                confirmButton.setEnabled(true);

                if (response.isSuccessful()) {
                    try {
                        String body = response.body() != null ? response.body().string() : "";
                        Log.i(TAG, "Order saved directly. Server response: " + body);
                        successIcon.setVisibility(View.VISIBLE);
                        Toast.makeText(TempPaymentActivity.this, "Order saved!", Toast.LENGTH_SHORT).show();

                        // Navigate to confirmation (pass useful details)
                        Intent intent = new Intent(TempPaymentActivity.this, OrderConfirmationActivity.class);
                        intent.putExtra("customerId", String.valueOf(customerId));
                        intent.putExtra("totalAmount", totalAmount);
                        intent.putExtra("itemCount", items.size());
                        intent.putExtra("dishJson", dishJson);
                        startActivity(intent);
                        finish();

                        CartManager.clearCart();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read response body", e);
                        Toast.makeText(TempPaymentActivity.this, "Order saved but failed to read response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "Failed to save order. Code=" + response.code() + ", error=" + err);
                        Toast.makeText(TempPaymentActivity.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading errorBody", e);
                        Toast.makeText(TempPaymentActivity.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                confirmButton.setEnabled(true);
                Log.e(TAG, "saveOrderDirectly onFailure: " + t.getMessage(), t);
                Toast.makeText(TempPaymentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}