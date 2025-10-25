package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.CartItemAdapter;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.utils.CartManager;

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
            couponIntent.putExtra("fromCart", true);   // 👈 add this flag
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
            int couponId = data.getIntExtra("selectedCouponId", 0);
            int discountAmount = data.getIntExtra("discountAmount", 0);
            String couponType = data.getStringExtra("couponType");   // 👈 add this
            String itemCategory = data.getStringExtra("itemCategory"); // 👈 for free item

            int totalCents = CartManager.getTotalAmountInCents();
            int finalAmount = totalCents;

            if ("cash".equals(couponType)) {
                // Fixed cash discount
                finalAmount = Math.max(0, totalCents - discountAmount);

            } else if ("percent".equals(couponType)) {
                // Percentage discount (discountAmount = percentage, e.g. 10 for 10%)
                finalAmount = (int) Math.round(totalCents * (1 - discountAmount / 100.0));

            } else if ("free_item".equals(couponType)) {
                // Free item (e.g. drink)
                if (CartManager.hasItemCategory(itemCategory)) {
                    int cheapestItemPrice = CartManager.getCheapestItemPrice(itemCategory);
                    finalAmount = Math.max(0, totalCents - cheapestItemPrice);
                } else {
                    Toast.makeText(this, "This coupon requires ordering a " + itemCategory, Toast.LENGTH_SHORT).show();
                    return; // don’t proceed to payment
                }
            }

            // Launch TempPaymentActivity with discounted total
            Intent payIntent = new Intent(CartActivity.this, TempPaymentActivity.class);
            payIntent.putExtra("totalAmount", finalAmount);
            payIntent.putExtra("selectedCouponId", couponId);
            payIntent.putExtra("discountAmount", discountAmount);
            payIntent.putExtra("couponType", couponType);
            payIntent.putExtra("itemCategory", itemCategory);
            startActivity(payIntent);

        } else if (requestCode == 2001) {
            // User backed out without selecting a coupon → just proceed to payment
            int totalCents = CartManager.getTotalAmountInCents();

            Intent payIntent = new Intent(CartActivity.this, TempPaymentActivity.class);
            payIntent.putExtra("totalAmount", totalCents);
            startActivity(payIntent);
        }
    }
}