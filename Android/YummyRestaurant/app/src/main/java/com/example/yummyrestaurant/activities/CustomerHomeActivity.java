package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.RoleManager;

public class CustomerHomeActivity extends AppCompatActivity {

    private TextView welcomeText;
    private Button browseProductsBtn, orderHistoryBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        // Initialize views
        welcomeText = findViewById(R.id.welcomeText);
        browseProductsBtn = findViewById(R.id.browseProductsBtn);
        orderHistoryBtn = findViewById(R.id.orderHistoryBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        // Set welcome message
        String username = RoleManager.getUserName();
        welcomeText.setText("Welcome, " + username + "!");

        // Set button actions
        browseProductsBtn.setOnClickListener(v -> {
            startActivity(new Intent(CustomerHomeActivity.this,MenuActivity.class));
        });

        orderHistoryBtn.setOnClickListener(v -> {
            startActivity(new Intent(CustomerHomeActivity.this, OrderHistoryActivity.class));
        });

        logoutBtn.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(CustomerHomeActivity.this)
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        Toast.makeText(CustomerHomeActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
                        RoleManager.clearUserData(); // Clear session
                        startActivity(new Intent(CustomerHomeActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
}