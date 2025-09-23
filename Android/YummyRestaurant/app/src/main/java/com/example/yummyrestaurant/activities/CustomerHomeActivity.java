package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.MenuItemAdapter;
import com.example.yummyrestaurant.api.MenuApi;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.models.MenuResponse;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerHomeActivity extends AppCompatActivity {

    private TextView welcomeText;
    private RecyclerView menuRecyclerView;
    private MenuItemAdapter adapter;
    private ProgressBar loadingSpinner;
    private EditText searchBar;
    private Spinner categorySpinner, spiceSpinner, tagSpinner;
    private String currentLanguage = "en"; // Default language

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        // Initialize views
        welcomeText = findViewById(R.id.welcomeText);
        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        searchBar = findViewById(R.id.searchBar);
        categorySpinner = findViewById(R.id.categorySpinner);
        spiceSpinner = findViewById(R.id.spiceSpinner);
        tagSpinner = findViewById(R.id.tagSpinner);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ImageView menuIcon = findViewById(R.id.menuIcon);

        ImageView cartIcon = findViewById(R.id.cartIcon);

        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        // Set welcome message
        String username = RoleManager.getUserName();
        welcomeText.setText("Welcome, " + username + "!");

        // Set up RecyclerView
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MenuItemAdapter(this, new ArrayList<>());
        menuRecyclerView.setAdapter(adapter);

        // Search functionality for dish name only
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.searchByDishName(s.toString().trim());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Spinner setup
        setupSpinners();

        // Load menu items
        loadMenuItemsFromServer();


        // Navigation drawer logic
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_order_history) {
                startActivity(new Intent(this, OrderHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            }else if (id == R.id.nav_logout) {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            RoleManager.clearUserData();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
            }

            return false;
        });
    }


    private void setupSpinners() {
        String[] categories = {"All", "Appetizers", "Main Courses"};
        String[] spiceLevels = {"All", "Mild", "Numbing"};
        String[] tags = {"All", "vegetarian", "refreshing", "beef", "spicy"};

        categorySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories));
        spiceSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spiceLevels));
        tagSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tags));

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        categorySpinner.setOnItemSelectedListener(filterListener);
        spiceSpinner.setOnItemSelectedListener(filterListener);
        tagSpinner.setOnItemSelectedListener(filterListener);
    }

    private void loadMenuItemsFromServer() {
        Log.d("MenuActivity", "Loading menu items for language: " + currentLanguage);
        loadingSpinner.setVisibility(View.VISIBLE);
        menuRecyclerView.setVisibility(View.GONE);

        MenuApi menuApi = RetrofitClient.getClient().create(MenuApi.class);
        Call<MenuResponse> call = menuApi.getMenuItems(currentLanguage);

        call.enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                loadingSpinner.setVisibility(View.GONE);
                menuRecyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Log.d("RawResponse", new Gson().toJson(response.body()));
                    adapter.setMenuItems(response.body().data);
                    applyFilters();

                    for (MenuItem item : response.body().data) {
                        Log.d("MenuItem", "ID: " + item.getId() +
                                ", Name: " + item.getName() +
                                ", Image URL: " + item.getImage_url() +
                                ", Description: " + item.getDescription() +
                                ", Price: " + item.getPrice());
                    }

                    if (!response.body().data.isEmpty()) {
                        MenuItem first = response.body().data.get(0);
                        Log.d("Debug123", "Raw name: " + first.getImage_url());
                    }

                } else {
                    Log.e("MenuActivity", "Response failed or empty");
                    Toast.makeText(CustomerHomeActivity.this, "Failed to load menu items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                menuRecyclerView.setVisibility(View.VISIBLE);
                Log.e("MenuActivity", "API call failed: " + t.getMessage());
                Toast.makeText(CustomerHomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String selectedSpice = spiceSpinner.getSelectedItem().toString();
        String selectedTag = tagSpinner.getSelectedItem().toString();
        adapter.filter(selectedCategory, selectedSpice, selectedTag);
    }
}