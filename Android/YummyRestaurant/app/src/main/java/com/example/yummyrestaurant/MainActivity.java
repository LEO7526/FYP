package com.example.yummyrestaurant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.yummyrestaurant.activities.CustomerHomeActivity;
import com.example.yummyrestaurant.activities.DashboardActivity;
import com.example.yummyrestaurant.activities.LoginActivity;
import com.example.yummyrestaurant.activities.StaffOrdersActivity;
import com.example.yummyrestaurant.api.ApiConfig;
import com.example.yummyrestaurant.utils.RoleManager;

public class MainActivity extends AppCompatActivity {

    // Request code for notification permission
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize RoleManager
        RoleManager.init(this);

        // 🚀 自動偵測環境（模擬器 vs 真實手機）
        ApiConfig.autoDetectEnvironment(this);

        // Check and request POST_NOTIFICATIONS permission (required for Android 13 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                // Permission already granted, proceed with login logic
                handleUserLogin();
            }
        } else {
            // For Android versions below 13, proceed with login logic directly
            handleUserLogin();
        }
    }

    private void handleUserLogin() {
        // Initialize RoleManager first
        RoleManager.init(this);
        
        // Retrieve user role from RoleManager (assumes it's saved during login)
        String userRole = RoleManager.getUserRole();
        String userName = RoleManager.getUserName();
        String userId = RoleManager.getUserId();
        
        android.util.Log.d("MainActivity", "=== LOGIN CHECK ===");
        android.util.Log.d("MainActivity", "User Role: " + userRole);
        android.util.Log.d("MainActivity", "User Name: " + userName);
        android.util.Log.d("MainActivity", "User ID: " + userId);
        android.util.Log.d("MainActivity", "Is Staff: " + RoleManager.isStaff());

        if (userRole != null) {
            android.util.Log.d("MainActivity", "User is logged in, checking role...");
            if ("staff".equals(userRole)) {
                android.util.Log.d("MainActivity", "🎯 STAFF DETECTED! Navigating to StaffOrdersActivity");
                // If user is staff, navigate to StaffOrdersActivity
                startActivity(new Intent(MainActivity.this, StaffOrdersActivity.class));
            } else {
                android.util.Log.d("MainActivity", "Customer detected, navigating to CustomerHomeActivity");
                // If user is not staff, navigate to CustomerHomeActivity
                startActivity(new Intent(MainActivity.this, CustomerHomeActivity.class));
            }
            // Close MainActivity to prevent returning to it
            finish();
        } else {
            android.util.Log.d("MainActivity", "No user logged in, defaulting to customer interface");
            // If user is not logged in, assume it is a customer
            startActivity(new Intent(this, CustomerHomeActivity.class));
            // Close MainActivity to prevent returning to it
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with login logic
                handleUserLogin();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Notification permission was denied", Toast.LENGTH_SHORT).show();
                // Proceed with login logic even if permission was denied
                handleUserLogin();
            }
        }
    }
}