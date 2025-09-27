package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerHomeActivity extends AppCompatActivity {

    private RecyclerView menuRecyclerView;
    private MenuItemAdapter adapter;
    private ProgressBar loadingSpinner;
    private EditText searchBar;
    private Spinner categorySpinner, spiceSpinner, tagSpinner;
    private String currentLanguage = "en";
    private static boolean login;

    private List<ImageView> functionIcons = new ArrayList<>();

    private Map<ImageView, String> iconBaseNames = new HashMap<>();

    public static boolean isLogin() {
        return login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        login = RoleManager.getUser() != null;



        initViews();
        setupRecyclerView();
        setupSearchBar();
        setupSpinners();
        setupNavigationDrawer();
        setupBottomFunctionBar();

        loadMenuItemsFromServer();


    }

    private void initViews() {
        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        searchBar = findViewById(R.id.searchBar);
        categorySpinner = findViewById(R.id.categorySpinner);
        spiceSpinner = findViewById(R.id.spiceSpinner);
        tagSpinner = findViewById(R.id.tagSpinner);
    }

    private void setupRecyclerView() {
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MenuItemAdapter(this, new ArrayList<>());
        menuRecyclerView.setAdapter(adapter);
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.searchByDishName(s.toString().trim());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSpinners() {
        String[] categories = {"All", "Appetizers", "Main Courses","Soup","Dessert"};

        String[] spiceLevels = {
                "All", "None", "Mild", "Medium", "Spicy", "Hot", "Numbing"
        };

        String[] tags = {
                "All", "vegetarian", "refreshing", "beef", "spicy", "chicken",
                "cold", "sour", "tofu", "numbing", "noodles", "pork",
                "streetfood", "stirfry", "classic", "eggplant", "sweet", "glutinous", "fish"
        };

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

    private void setupNavigationDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ImageView menuIcon = findViewById(R.id.menuIcon);
        ImageView cartIcon = findViewById(R.id.cartIcon);

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        cartIcon.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_logout) {
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

    private void setupBottomFunctionBar() {
        ImageView orderBellIcon = findViewById(R.id.orderBellIcon);
        ImageView couponIcon = findViewById(R.id.couponIcon);
        ImageView membershipIcon = findViewById(R.id.membershipIcon);
        ImageView orderRecordIcon = findViewById(R.id.orderRecordIcon);
        ImageView profileIcon = findViewById(R.id.profileIcon);

        functionIcons.add(orderBellIcon);
        functionIcons.add(couponIcon);
        functionIcons.add(membershipIcon);
        functionIcons.add(orderRecordIcon);
        functionIcons.add(profileIcon);

        iconBaseNames.put(orderBellIcon, "customer_main_page_function_item_background");
        iconBaseNames.put(couponIcon, "customer_main_page_function_item_background");
        iconBaseNames.put(membershipIcon, "customer_main_page_function_item_background_unique");
        iconBaseNames.put(orderRecordIcon, "customer_main_page_function_item_background");
        iconBaseNames.put(profileIcon, "customer_main_page_function_item_background");

        highlightIcon(orderBellIcon);

        orderBellIcon.setOnClickListener(v -> {
            highlightIcon(orderBellIcon);
        });

        couponIcon.setOnClickListener(v -> {
            highlightIcon(couponIcon);
            Toast.makeText(this, "Coupon activity clicked", Toast.LENGTH_SHORT).show();
        });

        membershipIcon.setOnClickListener(v -> {
            highlightIcon(membershipIcon);
            Toast.makeText(this, "Membership activity clicked", Toast.LENGTH_SHORT).show();
        });

        orderRecordIcon.setOnClickListener(v -> {
            highlightIcon(orderRecordIcon);
            if (login) {
                startActivity(new Intent(this, OrderHistoryActivity.class));
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        profileIcon.setOnClickListener(v -> {
            highlightIcon(profileIcon);
            if (login) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
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
                    adapter.setMenuItems(response.body().data);
                    applyFilters();
                } else {
                    Toast.makeText(CustomerHomeActivity.this, "Failed to load menu items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                menuRecyclerView.setVisibility(View.VISIBLE);
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

    private void highlightIcon(ImageView selectedIcon) {
        for (ImageView icon : functionIcons) {
            String baseName = iconBaseNames.get(icon);
            if (baseName == null) continue;

            int drawableId = (icon == selectedIcon)
                    ? getResources().getIdentifier(baseName + "_current", "drawable", getPackageName())
                    : getResources().getIdentifier(baseName, "drawable", getPackageName());

            if (drawableId != 0) {
                icon.setBackgroundResource(drawableId);
            }
        }
    }
}