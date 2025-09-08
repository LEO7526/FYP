package com.example.yummyrestaurant;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.activities.DashboardActivity;
import com.example.yummyrestaurant.activities.LoginActivity;
import com.example.yummyrestaurant.activities.ProductListActivity;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Use the callback to handle the role asynchronously
            RoleManager.getUserRole(userId, role -> {
                if ("staff".equals(role)) {
                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, ProductListActivity.class));
                }
                finish(); // Optional: close MainActivity after redirect
            });

        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Optional: close MainActivity after redirect
        }
    }
}