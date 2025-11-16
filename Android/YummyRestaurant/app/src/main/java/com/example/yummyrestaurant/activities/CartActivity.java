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
import com.example.yummyrestaurant.utils.CouponValidator;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";

    private RecyclerView cartRecyclerView;
    private TextView totalCostText;
    private Button checkoutBtn;
    private double total;
    private Map<CartItem, Integer> cartItems;

    // Expose adapter for external refresh
    public static CartItemAdapter activeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Log.d(TAG, "onCreate: Initializing CartActivity UI");

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalCostText = findViewById(R.id.totalCostText);
        checkoutBtn = findViewById(R.id.checkoutBtn);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter and expose it
        activeAdapter = new CartItemAdapter(this, CartManager.getCartItems());
        cartRecyclerView.setAdapter(activeAdapter);

        updateCartUI();

        checkoutBtn.setOnClickListener(v -> {
            Log.d(TAG, "Checkout button clicked");
            if (cartItems == null || cartItems.isEmpty()) {
                Log.d(TAG, "Checkout aborted: cart is empty");
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "Launching MyCouponsActivity for coupon selection");
            Intent couponIntent = new Intent(CartActivity.this, MyCouponsActivity.class);
            couponIntent.putExtra("fromCart", true);

            try {
                int custId = Integer.parseInt(RoleManager.getUserId());
                couponIntent.putExtra("customer_id", custId);
                Log.d(TAG, "Passing customer_id=" + custId + " to MyCouponsActivity");
            } catch (Exception e) {
                Log.w(TAG, "No logged-in user, passing customer_id=0", e);
                couponIntent.putExtra("customer_id", 0);
            }

            // Collect and pass each MenuItem id
            if (cartItems != null && !cartItems.isEmpty()) {
                ArrayList<Integer> menuItemIds = new ArrayList<>();
                for (CartItem item : cartItems.keySet()) {
                    Integer id = item.getMenuItemId();
                    if (id != null) {
                        menuItemIds.add(id);
                    }
                }
                couponIntent.putIntegerArrayListExtra("menu_item_ids", menuItemIds);
                Log.d(TAG, "Passing menu item id=" + menuItemIds + " to MyCouponsActivity");

                // Pass order total (in cents)
                couponIntent.putExtra("order_total", CartManager.getTotalAmountInCents());
                Log.d(TAG, "Passing order total=" + CartManager.getTotalAmountInCents() + " to MyCouponsActivity");
            }

            startActivityForResult(couponIntent, 2001);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Refreshing cart UI");
        updateCartUI();
    }

    // Refresh cart UI from anywhere
    public static void refreshCartUI() {
        Log.d(TAG, "refreshCartUI: External request to refresh cart UI");
        if (activeAdapter != null) {
            activeAdapter.updateItems(CartManager.getCartItems());
        }
    }

    private void updateCartUI() {
        cartItems = CartManager.getCartItems();
        Log.d(TAG, "updateCartUI: items=" + cartItems.size());

        if (activeAdapter != null) {
            activeAdapter.updateItems(cartItems);
        }

        total = CartManager.getTotalCost();
        Log.d(TAG, "updateCartUI: total HK$ " + total);
        totalCostText.setText(String.format(Locale.getDefault(), "Total: HK$ %.2f", total));

        checkoutBtn.setEnabled(!cartItems.isEmpty());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            Coupon coupon = data.getParcelableExtra("selectedCoupon");
            if (coupon == null) {
                Log.d(TAG, "onActivityResult: No coupon returned");
                return;
            }

            Log.d(TAG, "onActivityResult: Coupon selected -> " + coupon.getTitle());

            // Validate coupon before applying
            if (!CouponValidator.isCouponValidForCart(coupon, 1)) {
                Log.d(TAG, "onActivityResult: Coupon invalid, aborting");
                Toast.makeText(this, "Coupon not valid for this cart", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalCents = CartManager.getTotalAmountInCents();
            int finalAmount = totalCents;

            switch (coupon.getDiscountType()) {
                case "cash":
                    finalAmount = Math.max(0, totalCents - (int) Math.round(coupon.getDiscountValue() * 100));
                    Log.d(TAG, "Applied cash discount: -" + coupon.getDiscountValue());
                    break;
                case "percent":
                    finalAmount = (int) Math.round(totalCents * (1 - coupon.getDiscountValue() / 100.0));
                    Log.d(TAG, "Applied percent discount: -" + coupon.getDiscountValue() + "%");
                    break;
                case "free_item":
                    // NEW CODE: trust CouponValidator
                    int cheapest = CartManager.getCheapestEligibleItemPrice(coupon);
                    if (cheapest > 0) {
                        finalAmount = Math.max(0, totalCents - cheapest);
                        Log.d(TAG, "Applied free_item discount: -" + cheapest + " cents");
                    } else {
                        Log.d(TAG, "No eligible items in cart for couponId=" + coupon.getCouponId());
                        Toast.makeText(this, "No eligible items in cart for this coupon", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
            }

            Log.d(TAG, "Final amount after coupon: " + finalAmount);
            Intent payIntent = new Intent(CartActivity.this, TempPaymentActivity.class);
            payIntent.putExtra("totalAmount", finalAmount);
            payIntent.putExtra("selectedCoupon", coupon);
            startActivity(payIntent);

        } else if (requestCode == 2001) {
            int totalCents = CartManager.getTotalAmountInCents();
            Log.d(TAG, "No coupon selected, proceeding with full amount: " + totalCents);
            Intent payIntent = new Intent(CartActivity.this, TempPaymentActivity.class);
            payIntent.putExtra("totalAmount", totalCents);
            startActivity(payIntent);
        }
    }
}
