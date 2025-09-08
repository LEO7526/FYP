package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.QRScannerActivity;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    // UI Components
    private TextView welcomeTextView;
    private Button trackOrdersButton;
    private Button scanQRButton;
    private Button viewNotificationsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI elements
        welcomeTextView = findViewById(R.id.welcomeText);
        trackOrdersButton = findViewById(R.id.trackOrdersBtn);
        scanQRButton = findViewById(R.id.scanQRBtn);
        viewNotificationsButton = findViewById(R.id.viewNotificationsBtn);

        // Set welcome message
        String userName = "Hello, Employee!";
        welcomeTextView.setText(userName);

        // Set button click listeners
        trackOrdersButton.setOnClickListener(v -> {
            // Navigate to Order Tracking Activity
            startActivity(new Intent(this, OrderTrackingActivity.class));
        });

        scanQRButton.setOnClickListener(v -> {
            // Navigate to QRScanner Activity (QRScanner should extend AppCompatActivity)
            startActivity(new Intent(this, QRScannerActivity.class));
        });

        viewNotificationsButton.setOnClickListener(v -> {
            // Fetch unread notifications
            List<String> notifications = getUnreadNotifications();
            if (!notifications.isEmpty()) {
                // Display notification count or navigate to notifications page
                Toast.makeText(this, "You have " + notifications.size() + " unread notifications", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to fetch unread notifications
    private List<String> getUnreadNotifications() {
        // Example: Fetch notifications from Firebase or database
        // Here we simulate some placeholder data
        return new ArrayList<>(); // This should be replaced with actual logic
    }
}
