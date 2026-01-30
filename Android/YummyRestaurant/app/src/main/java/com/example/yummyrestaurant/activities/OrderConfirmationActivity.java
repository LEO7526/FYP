package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;



import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderConfirmationActivity extends ThemeBaseActivity {

    private static final String TAG = "OrderConfirmationActivity";

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
        int subtotalAmount = intent.getIntExtra("subtotalAmount", 0);
        int totalAmount = intent.getIntExtra("totalAmount", 0);
        int itemCount = intent.getIntExtra("itemCount", 0);
        String dishJson = intent.getStringExtra("dishJson");
        ArrayList<Coupon> selectedCoupons = intent.getParcelableArrayListExtra("selectedCoupons");
        HashMap<Integer, Integer> couponQuantities =
                (HashMap<Integer, Integer>) intent.getSerializableExtra("couponQuantities");

        Log.d(TAG, "Received subtotalAmount=" + subtotalAmount +
                ", totalAmount=" + totalAmount +
                ", itemCount=" + itemCount +
                ", customerId=" + customerId);
        Log.d(TAG, "selectedCoupons=" + (selectedCoupons != null ? new Gson().toJson(selectedCoupons) : "null"));
        Log.d(TAG, "couponQuantities=" + (couponQuantities != null ? new Gson().toJson(couponQuantities) : "null"));
        Log.d(TAG, "dishJson received: " + dishJson);

        // Bind views
        TextView orderSummary = findViewById(R.id.orderSummary);
        TextView discountInfo = findViewById(R.id.discountInfo);
        TextView totalInfo = findViewById(R.id.totalInfo);
        TextView dishSummary = findViewById(R.id.dishSummary);
        TextView couponDetails = findViewById(R.id.couponDetails);

        double subtotal = subtotalAmount / 100.0;
        double finalTotal = totalAmount / 100.0;

        // ✅ 新增：計算真實的項目總數（所有 qty 的總和）
        int totalQty = 0;
        if (dishJson != null) {
            try {
                List<Map<String, Object>> dishes = new Gson().fromJson(
                        dishJson, new TypeToken<List<Map<String, Object>>>() {}.getType()
                );
                for (Map<String, Object> dish : dishes) {
                    int qty = ((Double) dish.get("qty")).intValue();
                    totalQty += qty;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse dishJson for qty calculation", e);
                totalQty = itemCount;
            }
        } else {
            totalQty = itemCount;
        }

        orderSummary.setText("Customer ID: " + customerId +
                "\nItems: " + totalQty +
                "\nSubtotal: HK$" + String.format(Locale.getDefault(), "%.2f", subtotal));

        // Coupon discount breakdown
        if (selectedCoupons != null && !selectedCoupons.isEmpty()) {
            StringBuilder discountLines = new StringBuilder();
            StringBuilder details = new StringBuilder();

            for (Coupon c : selectedCoupons) {
                String title = c.getTitle();
                String type = c.getDiscountType().toLowerCase(Locale.ROOT);
                int qty = couponQuantities != null ? couponQuantities.getOrDefault(c.getCouponId(), 1) : 1;

                Log.d(TAG, "Coupon applied: id=" + c.getCouponId() +
                        ", title=" + title +
                        ", type=" + type +
                        ", qty=" + qty +
                        ", discountValue=" + c.getDiscountValue());

                switch (type) {
                    case "percent":
                        discountLines.append("- ")
                                .append((int) c.getDiscountValue())
                                .append("% off ×").append(qty)
                                .append(" (").append(title).append(")\n");
                        details.append("Coupon: ").append(title)
                                .append("\nDiscount: ").append((int) c.getDiscountValue())
                                .append("% off ×").append(qty).append("\n\n");
                        break;
                    case "cash":
                        discountLines.append("- HK$")
                                .append(String.format(Locale.getDefault(), "%.2f", c.getDiscountValue()))
                                .append(" ×").append(qty)
                                .append(" (").append(title).append(")\n");
                        details.append("Coupon: ").append(title)
                                .append("\nDiscount: HK$")
                                .append(String.format(Locale.getDefault(), "%.2f", c.getDiscountValue()))
                                .append(" ×").append(qty).append("\n\n");
                        break;
                    case "free_item":
                        discountLines.append("- Free ").append(c.getItemCategory())
                                .append(" ×").append(qty)
                                .append(" (").append(title).append(")\n");
                        details.append("Coupon: ").append(title)
                                .append("\nDiscount: Free ").append(c.getItemCategory())
                                .append(" ×").append(qty).append("\n\n");
                        break;
                }
            }

            discountInfo.setText(discountLines.toString().trim());
            discountInfo.setVisibility(TextView.VISIBLE);

            couponDetails.setText(details.toString().trim());
            couponDetails.setVisibility(TextView.VISIBLE);
        } else {
            discountInfo.setVisibility(TextView.GONE);
            couponDetails.setVisibility(TextView.GONE);
        }

        totalInfo.setText("Total: HK$" + String.format(Locale.getDefault(), "%.2f", finalTotal));

        // Dish breakdown
        List<Map<String, Object>> dishes = new ArrayList<>();
        if (dishJson != null) {
            dishes = new Gson().fromJson(
                    dishJson, new TypeToken<List<Map<String, Object>>>() {}.getType()
            );

            StringBuilder summary = new StringBuilder();
            for (Map<String, Object> dish : dishes) {
                String name = (String) dish.get("dish_name");
                int qty = ((Double) dish.get("qty")).intValue();
                double price = (Double) dish.get("dish_price");
                double itemSubtotal = qty * price;

                summary.append("• ").append(name)
                        .append(" — Qty: ").append(qty)
                        .append(", Price: HK$").append(String.format(Locale.getDefault(), "%.2f", price))
                        .append(", Subtotal: HK$").append(String.format(Locale.getDefault(), "%.2f", itemSubtotal))
                        .append("\n");

                Log.d(TAG, "Dish: " + name + ", Qty: " + qty + ", Price: HK$" + price + ", Subtotal: HK$" + itemSubtotal);
                
                // ✅ 新增：顯示自訂項
                Log.d(TAG, "Checking customization_details in dish...");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> customDetails = (List<Map<String, Object>>) dish.get("customization_details");
                if (customDetails != null && !customDetails.isEmpty()) {
                    Log.d(TAG, "Found " + customDetails.size() + " customization details");
                    for (Map<String, Object> detail : customDetails) {
                        String optionName = (String) detail.get("option_name");
                        String choiceNames = (String) detail.get("choice_names");
                        String textValue = (String) detail.get("text_value");
                        
                        Log.d(TAG, "  Detail: option=" + optionName + ", choices=" + choiceNames + ", text=" + textValue);
                        
                        if (choiceNames != null && !choiceNames.isEmpty()) {
                            summary.append("    ├─ ").append(optionName).append(": ").append(choiceNames).append("\n");
                            Log.d(TAG, "  ✅ Added: " + optionName + " = " + choiceNames);
                        }
                        if (textValue != null && !textValue.isEmpty()) {
                            summary.append("    ├─ ").append(optionName).append(": ").append(textValue).append("\n");
                            Log.d(TAG, "  ✅ Added: " + optionName + " = " + textValue);
                        }
                    }
                } else {
                    Log.d(TAG, "No customization_details found or empty");
                }
                
                // ✅ 新增：顯示特殊要求（extra_notes）
                String extraNotes = (String) dish.get("extra_notes");
                if (extraNotes != null && !extraNotes.isEmpty()) {
                    summary.append("    └─ Special: ").append(extraNotes).append("\n");
                    Log.d(TAG, "  ✅ Added Special: " + extraNotes);
                }
            }
            dishSummary.setText(summary.toString());
        }
    }
}

