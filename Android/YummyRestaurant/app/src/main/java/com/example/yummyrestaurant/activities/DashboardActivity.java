package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.QRScannerActivity;
import com.example.yummyrestaurant.utils.RoleManager;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private Button trackOrdersButton;
    private Button scanQRButton;
    private Button viewNotificationsButton;
    private Button checkInOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ✅ Initialize UI components
        welcomeTextView = findViewById(R.id.welcomeTextView);
        trackOrdersButton = findViewById(R.id.trackOrdersButton);
        scanQRButton = findViewById(R.id.scanQRButton);
        viewNotificationsButton = findViewById(R.id.viewNotificationsButton);
        checkInOutBtn = findViewById(R.id.checkInOutBtn);

        // ✅ Set welcome message
        String userName = "Hello, " + RoleManager.getUserName() + "!";
        welcomeTextView.setText(userName);

        // ✅ Set button click listeners
        trackOrdersButton.setOnClickListener(v ->
                startActivity(new Intent(this, OrderTrackingActivity.class))
        );

        scanQRButton.setOnClickListener(v ->
                startActivity(new Intent(this, QRScannerActivity.class))
        );

        viewNotificationsButton.setOnClickListener(v -> {
            List<String> notifications = getUnreadNotifications();
            String message = notifications.isEmpty()
                    ? "No new notifications"
                    : "You have " + notifications.size() + " unread notifications";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        checkInOutBtn.setOnClickListener(v ->
                startActivity(new Intent(this, CheckInAndOutActivity.class))
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;

        } else if (id == R.id.nav_logout) {
            showLogoutConfirmation();
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                    RoleManager.clearUserData(); // Clear session
                    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }



    // Simulated method to fetch unread notifications
    private List<String> getUnreadNotifications() {
        return new ArrayList<>(); // Replace with actual logic
    }
}
