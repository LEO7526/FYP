package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderConfirmationActivity extends AppCompatActivity {

    private Button backToHomeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        backToHomeBtn = findViewById(R.id.backToHomeBtn);
        backToHomeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Retrieve order details
        Intent intent = getIntent();
        String customerId = RoleManager.getUserId();
        int totalAmount = intent.getIntExtra("totalAmount", 0);
        int itemCount = intent.getIntExtra("itemCount", 0);
        int discountAmount = intent.getIntExtra("discountAmount", 0);
        int couponId = intent.getIntExtra("couponId", 0);
        String dishJson = intent.getStringExtra("dishJson");

        // Subtotal = total + discount
        double subtotal = (totalAmount + discountAmount) / 100.0;
        double discount = discountAmount / 100.0;
        double total = totalAmount / 100.0;

        // Bind views
        TextView orderSummary = findViewById(R.id.orderSummary);
        TextView discountInfo = findViewById(R.id.discountInfo);
        TextView totalInfo = findViewById(R.id.totalInfo);
        TextView dishSummary = findViewById(R.id.dishSummary);

        // Fill in summary
        orderSummary.setText("Customer ID: " + customerId +
                "\nItems: " + itemCount +
                "\nSubtotal: HK$" + String.format("%.2f", subtotal));

        if (discountAmount > 0) {
            String discountText = "Discount: -HK$" + String.format("%.2f", discount);
            if (couponId != 0) {
                discountText += " (Coupon #" + couponId + ")";
            }
            discountInfo.setText(discountText);
            discountInfo.setVisibility(View.VISIBLE);   // always show when discount exists
        } else {
            discountInfo.setVisibility(View.GONE);      // hide completely if no discount
        }

        totalInfo.setText("Total: HK$" + String.format("%.2f", total));

        // Parse and display dish details
        List<Map<String, Object>> dishes = new ArrayList<>();
        if (dishJson != null) {
            dishes = new Gson().fromJson(
                    dishJson, new TypeToken<List<Map<String, Object>>>() {}.getType()
            );

            for (Map<String, Object> dish : dishes) {
                String name = (String) dish.get("dish_name");
                int qty = ((Double) dish.get("qty")).intValue();
                double price = (Double) dish.get("dish_price");
                Log.d("OrderConfirmation", "Dish: " + name + ", Qty: " + qty + ", Price: HK$" + price);
            }
        }

        StringBuilder summary = new StringBuilder();
        for (Map<String, Object> dish : dishes) {
            String name = (String) dish.get("dish_name");
            int qty = ((Double) dish.get("qty")).intValue();
            double price = (Double) dish.get("dish_price");
            double itemSubtotal = qty * price;

            summary.append(name)
                    .append(" — Qty: ").append(qty)
                    .append(" — Price: HK$").append(String.format("%.2f", price))
                    .append(" — Subtotal: HK$").append(String.format("%.2f", itemSubtotal))
                    .append("\n");
        }

        dishSummary.setText(summary.toString());
    }
}