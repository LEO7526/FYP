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
    private CartItemAdapter adapter;
    private TextView totalCostText;
    private Button checkoutBtn;
    private Map<CartItem, Integer> cartItems;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalCostText = findViewById(R.id.totalCostText);
        checkoutBtn = findViewById(R.id.checkoutBtn);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter once
        adapter = new CartItemAdapter(this, CartManager.getCartItems());
        cartRecyclerView.setAdapter(adapter);

        updateCartUI();

        checkoutBtn.setOnClickListener(v -> {
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Proceeding to payment...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
            intent.putExtra("totalAmount", CartManager.getTotalAmountInCents());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartUI();
    }

    private void updateCartUI() {
        cartItems = CartManager.getCartItems();
        adapter.updateItems(cartItems);

        total = CartManager.getTotalCost();
        totalCostText.setText(String.format(Locale.getDefault(), "Total: HK$ %.2f", total));

        checkoutBtn.setEnabled(!cartItems.isEmpty());
    }
}