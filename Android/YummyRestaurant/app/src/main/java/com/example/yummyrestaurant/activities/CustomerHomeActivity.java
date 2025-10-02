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
    private String currentLanguage = "en";
    private static boolean login;
    private Button btnAll, btnAppetizers, btnMainCourses, btnSoup, btnDessert;
    private String selectedCategory = "All Dishes"; // default


    private List<ImageView> functionIcons = new ArrayList<>();
    private Map<ImageView, String> iconBaseNames = new HashMap<>();

    public static boolean isLogin() {
        return login;
    }

    public static void setLogin(boolean login) {
        CustomerHomeActivity.login = login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        login = RoleManager.getUser() != null;

        initViews();
        setupRecyclerView();
        setupSearchBar();
        setupCategoryButtons();
        setupNavigationDrawer();
        setupBottomFunctionBar();

        loadMenuItemsFromServer();
    }

    private void initViews() {
        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        searchBar = findViewById(R.id.searchBar);
        btnAll = findViewById(R.id.btnAll);
        btnAppetizers = findViewById(R.id.btnAppetizers);
        btnMainCourses = findViewById(R.id.btnMainCourses);
        btnSoup = findViewById(R.id.btnSoup);
        btnDessert = findViewById(R.id.btnDessert);
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

    private void setupCategoryButtons() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.btnAll) {
                selectedCategory = "All Dishes";
            } else if (id == R.id.btnAppetizers) {
                selectedCategory = "Appetizers";
            } else if (id == R.id.btnMainCourses) {
                selectedCategory = "Main Courses";
            } else if (id == R.id.btnSoup) {
                selectedCategory = "Soup";
            } else if (id == R.id.btnDessert) {
                selectedCategory = "Dessert";
            }
            applyFilters();
        };

        btnAll.setOnClickListener(listener);
        btnAppetizers.setOnClickListener(listener);
        btnMainCourses.setOnClickListener(listener);
        btnSoup.setOnClickListener(listener);
        btnDessert.setOnClickListener(listener);
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

        //default
        highlightIcon(orderBellIcon);

        orderBellIcon.setOnClickListener(v -> {
            highlightIcon(orderBellIcon);
            Toast.makeText(this, "You're already on the Home page", Toast.LENGTH_SHORT).show();
        });

        couponIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, CouponActivity.class);
            intent.putExtra("selectedIcon", R.id.couponIcon); // tell Home which icon to highlight
            startActivity(intent);
        });

        membershipIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, MembershipActivity.class);
            intent.putExtra("selectedIcon", R.id.membershipIcon); // tell Home which icon to highlight
            startActivity(intent);
        });

        orderRecordIcon.setOnClickListener(v -> {

            if (login) {
                Intent intent = new Intent(this, OrderHistoryActivity.class);
                intent.putExtra("selectedIcon", R.id.orderRecordIcon); // tell Home which icon to highlight
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        profileIcon.setOnClickListener(v -> {

            if (login) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("selectedIcon", R.id.profileIcon); // tell Home which icon to highlight
                startActivity(intent);
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
        adapter.filter(selectedCategory);
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

    @Override
    protected void onResume() {
        super.onResume();

        // Read which icon should be highlighted (default = Home/OrderBell)
        int selectedIconId = getIntent().getIntExtra("selectedIcon", R.id.orderBellIcon);

        ImageView selectedIcon = findViewById(selectedIconId);
        if (selectedIcon != null) {
            highlightIcon(selectedIcon);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // update the stored Intent so onResume() sees the latest extras
    }
}