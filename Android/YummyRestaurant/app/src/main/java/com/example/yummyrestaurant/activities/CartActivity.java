package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.CartItemAdapter;
import com.example.yummyrestaurant.adapters.MenuItemAdapter;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private CartItemAdapter adapter;
    private TextView totalCostText;
    private Button checkoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalCostText = findViewById(R.id.totalCostText);
        checkoutBtn = findViewById(R.id.checkoutBtn);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Map<MenuItem, Integer> cartItems = CartManager.getCartItems();
        adapter = new CartItemAdapter(this, cartItems);
        cartRecyclerView.setAdapter(adapter);

        double total = CartManager.getTotalCost();
        totalCostText.setText(String.format(Locale.getDefault(), "Total: $ %.2f", total));

        checkoutBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Proceeding to payment...", Toast.LENGTH_SHORT).show();
            // TODO: Launch payment flow
        });
    }
}