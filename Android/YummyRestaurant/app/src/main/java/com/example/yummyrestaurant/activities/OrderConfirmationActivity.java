package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
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
        int customerId = intent.getIntExtra("customerId", -1);
        int totalAmount = intent.getIntExtra("totalAmount", 0);
        int itemCount = intent.getIntExtra("itemCount", 0);
        String dishJson = intent.getStringExtra("dishJson");

        Log.d("OrderConfirmation", "Customer ID: " + customerId);
        Log.d("OrderConfirmation", "Total Amount: HK$" + (totalAmount / 100.0));
        Log.d("OrderConfirmation", "Item Count: " + itemCount);

        TextView orderSummary = findViewById(R.id.orderSummary);
        orderSummary.setText("Customer ID: " + customerId +
                "\nTotal: HK$" + (totalAmount / 100.0) +
                "\nItems: " + itemCount);

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

        TextView dishSummary = findViewById(R.id.dishSummary);
        StringBuilder summary = new StringBuilder();

        for (Map<String, Object> dish : dishes) {
            String name = (String) dish.get("dish_name");
            int qty = ((Double) dish.get("qty")).intValue();
            double price = (Double) dish.get("dish_price");
            double subtotal = qty * price;

            summary.append(name)
                    .append(" — Qty: ").append(qty)
                    .append(" — Price: HK$").append(price)
                    .append(" — Subtotal: HK$").append(String.format("%.2f", subtotal))
                    .append("\n");
        }

        dishSummary.setText(summary.toString());
    }
}