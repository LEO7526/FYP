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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private int applyCoupons(List<Coupon> coupons) {
        Log.d(TAG, "applyCoupons called with " + coupons.size() + " coupons");

        int finalAmount = CartManager.getTotalAmountInCents();
        for (Coupon c : coupons) {
            int qty = c.getQuantity();
            if (!CouponValidator.isCouponValidForCart(c, qty)) continue;

            double discountValue = c.getDiscountValue();
            int discountAmount = c.getDiscountAmount(); // in cents

            for (int i = 0; i < qty; i++) {
                switch (c.getDiscountType().toLowerCase(Locale.ROOT)) {
                    case "cash":
                        // Prefer discountAmount if present, else discountValue * 100
                        int cashOff = discountAmount > 0 ? discountAmount : (int)Math.round(discountValue * 100);
                        finalAmount -= cashOff;
                        break;
                    case "percent":
                        finalAmount = (int)Math.round(finalAmount * (1 - discountValue / 100.0));
                        break;
                    case "free_item":
                        int cheapest = CartManager.getCheapestEligibleItemPrice(c);
                        finalAmount -= cheapest;
                        break;
                }
                finalAmount = Math.max(0, finalAmount);

                Log.d(TAG, "Applying coupon " + c.getCouponId() +
                        " type=" + c.getDiscountType() +
                        " discountValue=" + discountValue +
                        " discountAmount=" + discountAmount +
                        " finalAmount=" + finalAmount);
            }
        }
        return finalAmount;
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

        Log.d(TAG, "onActivityResult fired: requestCode=" + requestCode + " resultCode=" + resultCode);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            HashMap<Integer, Integer> couponQuantities =
                    (HashMap<Integer, Integer>) data.getSerializableExtra("couponQuantities");
            ArrayList<Coupon> selectedCoupons =
                    data.getParcelableArrayListExtra("selectedCoupons");

            Log.d(TAG, "Returned from MyCouponsActivity with:");
            Log.d(TAG, "couponQuantities=" + (couponQuantities != null ? new Gson().toJson(couponQuantities) : "null"));
            Log.d(TAG, "selectedCoupons=" + (selectedCoupons != null ? new Gson().toJson(selectedCoupons) : "null"));

            if ((couponQuantities == null || couponQuantities.isEmpty())
                    && (selectedCoupons == null || selectedCoupons.isEmpty())) {
                Log.d(TAG, "No coupons selected, proceeding without coupon");
                Intent payIntent = new Intent(this, TempPaymentActivity.class);
                payIntent.putExtra("totalAmount", CartManager.getTotalAmountInCents());
                startActivity(payIntent);
                return;
            }

            int finalAmount = applyCoupons(selectedCoupons != null ? selectedCoupons : new ArrayList<>());
            Log.d(TAG, "Final amount after applying "
                    + (selectedCoupons != null ? selectedCoupons.size() : 0)
                    + " coupons: " + finalAmount);

            Intent payIntent = new Intent(this, TempPaymentActivity.class);
            payIntent.putExtra("subtotalAmount", CartManager.getTotalAmountInCents());
            payIntent.putExtra("totalAmount", finalAmount);
            payIntent.putExtra("couponQuantities", couponQuantities);
            payIntent.putParcelableArrayListExtra("selectedCoupons", selectedCoupons);
            startActivity(payIntent);
        }
    }




}
