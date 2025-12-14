package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.android.material.navigation.NavigationView;

public class CustomerHomeActivity extends BaseCustomerActivity {

    private ImageView imageView;
    private LinearLayout dotsContainer;
    private ImageButton btnPrev, btnNext;
    private DrawerLayout drawerLayout;

    private int[] images = {
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3
    };

    private int index = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean paused = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!paused) {
                showImage(index);
                index = (index + 1) % images.length;
            }
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        setupBottomFunctionBar();
        setupNavigationDrawer();

        imageView = findViewById(R.id.myImageView);
        dotsContainer = findViewById(R.id.dotsContainer);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        setupDots();
        handler.post(runnable);

        // Press to pause, release to advance immediately
        imageView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    paused = true;
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    paused = false;
                    index = (index + 1) % images.length;
                    showImage(index);
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 3000);
                    return true;
            }
            return false;
        });

        // Manual navigation
        btnPrev.setOnClickListener(v -> {
            // Stop current loop
            handler.removeCallbacks(runnable);

            // Move to previous image
            index = (index - 1 + images.length) % images.length;
            showImage(index);

            // Resume auto-slideshow
            paused = false;
            handler.postDelayed(runnable, 3000);
        });

        btnNext.setOnClickListener(v -> {
            // Stop current loop
            handler.removeCallbacks(runnable);

            // Move to next image
            index = (index + 1) % images.length;
            showImage(index);

            // Resume auto-slideshow
            paused = false;
            handler.postDelayed(runnable, 3000);
        });
    }

    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ImageView menuIcon = findViewById(R.id.menuIcon);

        // Open drawer when menu icon is clicked
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            boolean isLoggedIn = RoleManager.getUserId() != null && !RoleManager.getUserId().isEmpty();

            // Allow nav_settings even if not logged in
            if (!isLoggedIn && id != R.id.nav_settings && id != R.id.nav_home) {
                Toast.makeText(this, "Please log in to access this feature.", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }

            if (id == R.id.nav_home) {
                // Already on home, just close drawer
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_booking) {
                startActivity(new Intent(this, BookingActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_logout) {
                if (!isLoggedIn) {
                    Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                new AlertDialog.Builder(this)
                        .setTitle("Confirm Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            RoleManager.clearUserData();
                            startActivity(new Intent(this, BrowseMenuActivity.class));
                            finish();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        });
    }

    private void setupDots() {
        dotsContainer.removeAllViews();
        for (int i = 0; i < images.length; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_unselected);
            dotsContainer.addView(dot);
        }
        updateDots(0);
    }

    private void showImage(int position) {
        imageView.setImageResource(images[position]);
        updateDots(position);
    }

    private void updateDots(int selectedIndex) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            dot.setBackgroundResource(i == selectedIndex
                    ? R.drawable.dot_selected
                    : R.drawable.dot_unselected);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}