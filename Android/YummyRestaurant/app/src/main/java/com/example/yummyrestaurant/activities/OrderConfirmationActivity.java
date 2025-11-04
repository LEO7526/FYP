package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.Coupon;
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
        String dishJson = intent.getStringExtra("dishJson");

        // Full Coupon object passed from TempPaymentActivity
        Coupon selectedCoupon = intent.getParcelableExtra("selectedCoupon");

        // Bind views
        TextView orderSummary = findViewById(R.id.orderSummary);
        TextView discountInfo = findViewById(R.id.discountInfo);
        TextView totalInfo = findViewById(R.id.totalInfo);
        TextView dishSummary = findViewById(R.id.dishSummary);
        TextView couponDetails = findViewById(R.id.couponDetails); // 👈 add this TextView in layout

        // Calculate subtotal/discount/total
        int discountAmount = 0;
        if (selectedCoupon != null) {
            discountAmount = selectedCoupon.getDiscountAmount();
        }
        double subtotal = (totalAmount + discountAmount) / 100.0;
        double discount = discountAmount / 100.0;
        double total = totalAmount / 100.0;

        // Fill in summary
        orderSummary.setText("Customer ID: " + customerId +
                "\nItems: " + itemCount +
                "\nSubtotal: HK$" + String.format("%.2f", subtotal));

        if (selectedCoupon != null && discountAmount > 0) {
            // Show discount line
            String discountText = "Discount: -HK$" + String.format("%.2f", discount);
            discountText += " (" + selectedCoupon.getTitle() + ")";
            discountInfo.setText(discountText);
            discountInfo.setVisibility(View.VISIBLE);

            // Show concise coupon details
            StringBuilder details = new StringBuilder();
            details.append("Coupon: ").append(selectedCoupon.getTitle()).append("\n");
            if ("percent".equals(selectedCoupon.getDiscountType())) {
                details.append("Discount: ").append((int) selectedCoupon.getDiscountValue()).append("% off");
            } else if ("cash".equals(selectedCoupon.getDiscountType())) {
                details.append("Discount: HK$")
                        .append(String.format("%.2f", selectedCoupon.getDiscountValue()));
            } else if ("free_item".equals(selectedCoupon.getDiscountType())) {
                details.append("Discount: Free ").append(selectedCoupon.getItemCategory());
            }
            couponDetails.setText(details.toString());
            couponDetails.setVisibility(View.VISIBLE);

        } else {
            discountInfo.setVisibility(View.GONE);
            couponDetails.setVisibility(View.GONE);
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
                    .append(" • Qty: ").append(qty)
                    .append(" • Price: HK$").append(String.format("%.2f", price))
                    .append(" • Subtotal: HK$").append(String.format("%.2f", itemSubtotal))
                    .append("\n");
        }

        dishSummary.setText(summary.toString());
    }
}