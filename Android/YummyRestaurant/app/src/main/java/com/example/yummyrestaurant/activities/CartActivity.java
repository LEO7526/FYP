package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.CartItemAdapter;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private TextView totalCostText;
    private Button checkoutBtn;
    private double total;
    private Map<CartItem, Integer> cartItems;

    // 🔄 Expose adapter for external refresh
    public static CartItemAdapter activeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalCostText = findViewById(R.id.totalCostText);
        checkoutBtn = findViewById(R.id.checkoutBtn);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter and expose it
        activeAdapter = new CartItemAdapter(this, CartManager.getCartItems());
        cartRecyclerView.setAdapter(activeAdapter);

        updateCartUI();

        checkoutBtn.setOnClickListener(v -> {
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 1: go to MyCouponsActivity to let user view/select coupon
            Intent couponIntent = new Intent(CartActivity.this, MyCouponsActivity.class);
            couponIntent.putExtra("fromCart", true);
            startActivityForResult(couponIntent, 2001);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartUI();
    }

    // 🔁 Refresh cart UI from anywhere
    public static void refreshCartUI() {
        if (activeAdapter != null) {
            activeAdapter.updateItems(CartManager.getCartItems());
        }
    }

    private void updateCartUI() {
        cartItems = CartManager.getCartItems();
        if (activeAdapter != null) {
            activeAdapter.updateItems(cartItems);
        }

        total = CartManager.getTotalCost();
        totalCostText.setText(String.format(Locale.getDefault(), "Total: HK$ %.2f", total));

        checkoutBtn.setEnabled(!cartItems.isEmpty());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            Coupon coupon = data.getParcelableExtra("selectedCoupon");
            if (coupon == null) return;

            // ✅ Validate coupon before applying
            if (!isCouponValidForCart(coupon)) {
                return;
            }

            int totalCents = CartManager.getTotalAmountInCents();
            int finalAmount = totalCents;

            switch (coupon.getDiscountType()) {
                case "cash":
                    finalAmount = Math.max(0, totalCents - (int) Math.round(coupon.getDiscountValue() * 100));
                    break;
                case "percent":
                    finalAmount = (int) Math.round(totalCents * (1 - coupon.getDiscountValue() / 100.0));
                    break;
                case "free_item":
                    if (CartManager.hasItemCategory(coupon.getItemCategory())) {
                        int cheapest = CartManager.getCheapestItemPrice(coupon.getItemCategory());
                        finalAmount = Math.max(0, totalCents - cheapest);
                    } else {
                        Toast.makeText(this,
                                "This coupon requires ordering a " + coupon.getItemCategory(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
            }

            Intent payIntent = new Intent(CartActivity.this, TempPaymentActivity.class);
            payIntent.putExtra("totalAmount", finalAmount);
            payIntent.putExtra("selectedCoupon", coupon);
            startActivity(payIntent);

        } else if (requestCode == 2001) {
            int totalCents = CartManager.getTotalAmountInCents();
            Intent payIntent = new Intent(CartActivity.this, TempPaymentActivity.class);
            payIntent.putExtra("totalAmount", totalCents);
            startActivity(payIntent);
        }
    }

    private boolean isCouponValidForCart(Coupon coupon) {
        if (coupon == null) {
            Log.d("CouponDebug", "Coupon is null");
            return false;
        }

        Log.d("CouponDebug", "Validating coupon: " + coupon.getTitle() + " (ID=" + coupon.getCouponId() + ")");
        int totalCents = CartManager.getTotalAmountInCents();
        Log.d("CouponDebug", "Cart total (cents): " + totalCents);

        // 1. Minimum spend
        Double minSpend = coupon.getMinSpend();
        if (minSpend != null) {
            Log.d("CouponDebug", "Coupon minSpend=" + minSpend);
            if (totalCents < (int) Math.round(minSpend * 100)) {
                Log.d("CouponDebug", "Invalid: below min spend");
                return false;
            }
        }

        // 2. Applies to scope
        String appliesTo = coupon.getAppliesTo();
        Log.d("CouponDebug", "Coupon appliesTo=" + appliesTo);

        if ("item".equalsIgnoreCase(appliesTo)) {
            List<Integer> itemIds = coupon.getApplicableItems();
            if (itemIds != null && !itemIds.isEmpty()) {
                Log.d("CouponDebug", "Checking applicableItems=" + itemIds);
                if (!CartManager.hasAnyItem(itemIds)) {
                    Log.d("CouponDebug", "Invalid: no matching items in cart");
                    return false;
                }
            }

            String category = coupon.getItemCategory();
            if (category != null && !category.trim().isEmpty()) {
                Log.d("CouponDebug", "Checking itemCategory=" + category);
                if (!CartManager.hasItemCategory(category)) {
                    Log.d("CouponDebug", "Invalid: no matching category in cart");
                    return false;
                }
            }
        } else if ("category".equalsIgnoreCase(appliesTo)) {
            List<Integer> categoryIds = coupon.getApplicableCategories();
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Log.d("CouponDebug", "Checking applicableCategories=" + categoryIds);
                if (!CartManager.hasAnyCategory(categoryIds)) {
                    Log.d("CouponDebug", "Invalid: no matching categories in cart");
                    return false;
                }
            }
        }

        // 3. Order type
        String orderType = CartManager.getOrderType();
        Log.d("CouponDebug", "Order type=" + orderType);

        if ("dine_in".equals(orderType) && !coupon.isValidDineIn()) {
            Log.d("CouponDebug", "Invalid: not valid for dine-in");
            return false;
        }
        if ("takeaway".equals(orderType) && !coupon.isValidTakeaway()) {
            Log.d("CouponDebug", "Invalid: not valid for takeaway");
            return false;
        }
        if ("delivery".equals(orderType) && !coupon.isValidDelivery()) {
            Log.d("CouponDebug", "Invalid: not valid for delivery");
            return false;
        }

        // 4. Birthday-only
        if (coupon.isBirthdayOnly()) {
            Log.d("CouponDebug", "Coupon is birthday-only, checking RoleManager...");
            try {
                if (!RoleManager.isTodayUserBirthday()) {
                    Log.d("CouponDebug", "Invalid: not user's birthday");
                    return false;
                }
            } catch (Exception e) {
                Log.e("CouponDebug", "Error checking birthday", e);
                return false;
            }
        }

        // 5. Discount stacking
        if (!coupon.isCombineWithOtherDiscounts()) {
            Log.d("CouponDebug", "Coupon cannot combine with other discounts");
            if (CartManager.hasOtherDiscountsApplied()) {
                Log.d("CouponDebug", "Invalid: other discounts already applied");
                return false;
            }
        }

        Log.d("CouponDebug", "Coupon is valid ✅");
        return true;
    }
}