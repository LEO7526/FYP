package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.QRScannerActivity;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView welcomeTextView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ✅ Initialize UI components
        welcomeTextView = findViewById(R.id.welcomeTextView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // ✅ Set welcome message
        String userName = "Hello, " + RoleManager.getUserName() + "!";
        welcomeTextView.setText(userName);

        // ✅ Set navigation item listener
        navigationView.setNavigationItemSelectedListener(this);

        // ✅ Set up toolbar and drawer toggle
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✅ Access header view and update user info
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.navHeaderName);
        TextView navHeaderEmail = headerView.findViewById(R.id.navHeaderEmail);

        // ✅ Get user data from RoleManager (or your user session manager)
        String name = RoleManager.getUserName(); // e.g., "Ching"
        String email = RoleManager.getUserEmail(); // e.g., "ching@example.com"

        // ✅ Set dynamic values
        navHeaderName.setText("Welcome, " + name);
        navHeaderEmail.setText(email);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        ImageView cartIcon = findViewById(R.id.cartIcon);
        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CartActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_track_orders) {
            startActivity(new Intent(this, OrderTrackingActivity.class));
        } else if (id == R.id.nav_scan_qr) {
            startActivity(new Intent(this, QRScannerActivity.class));

        }  else if (id == R.id.nav_browse_table) {
            startActivity(new Intent(this, TableOverviewActivity.class));

        }else if (id == R.id.nav_browse_menu) {
            startActivity(new Intent(this, MenuActivity.class));

        }else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));

        }

        else if (id == R.id.nav_view_notifications) {
            List<String> notifications = getUnreadNotifications();
            String message = notifications.isEmpty()
                    ? "No new notifications"
                    : "You have " + notifications.size() + " unread notifications";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_check_in_out) {
            startActivity(new Intent(this, CheckInAndOutActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            showLogoutConfirmation();
        } else {
            return false;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                    RoleManager.clearUserData();
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