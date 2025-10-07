package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuResponse;
import com.example.yummyrestaurant.utils.BadgeManager;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseMenuActivity extends BaseCustomerActivity {

    private RecyclerView menuRecyclerView;
    private MenuItemAdapter adapter;
    private ProgressBar loadingSpinner;
    private EditText searchBar;
    private String currentLanguage = "en";
    private static boolean login;
    private Button btnAll, btnAppetizers, btnMainCourses, btnSoup, btnDessert, btnDrink;
    private String selectedCategory = "All Dishes"; // default

    private List<ImageView> functionIcons = new ArrayList<>();
    private Map<ImageView, String> iconBaseNames = new HashMap<>();

    // New: cart badge views
    private ImageView cartIcon;
    private static TextView cartBadge;

    public static boolean isLogin() {
        return login;
    }

    public static void setLogin(boolean login) {
        BrowseMenuActivity.login = login;
    }

    public static boolean getLogin(){
        return login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_menu);

        setupBottomFunctionBar ();

        login = RoleManager.getUser() != null;

        initViews();
        setupRecyclerView();
        setupSearchBar();
        setupCategoryButtons();
        setupNavigationDrawer();
        setupBottomFunctionBar();

        // update cart badge on start
        updateCartBadge();

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
        btnDrink = findViewById(R.id.btnDrink);

        // cart views (ensure your layout uses the FrameLayout with cartBadge)
        cartIcon = findViewById(R.id.cartIcon);
        cartBadge = findViewById(R.id.cartBadge);
        BadgeManager.registerBadgeView(cartBadge);
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
            } else if (id == R.id.btnDrink) {
                selectedCategory = "Drink";
            }
            applyFilters();
        };

        btnAll.setOnClickListener(listener);
        btnAppetizers.setOnClickListener(listener);
        btnMainCourses.setOnClickListener(listener);
        btnSoup.setOnClickListener(listener);
        btnDessert.setOnClickListener(listener);
        btnDrink.setOnClickListener(listener); // new
    }

    private void setupNavigationDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ImageView menuIcon = findViewById(R.id.menuIcon);

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        if (cartIcon != null) {
            cartIcon.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Check login status
            boolean isLoggedIn = RoleManager.getUserId() != null && !RoleManager.getUserId().isEmpty();

            // Block access to all items except logout if not logged in
            if (!isLoggedIn && id != R.id.nav_logout) {
                Toast.makeText(this, "Please log in to access this feature.", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
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
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, CustomerHomeActivity.class));
                return true;
            }

            return false;
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
                    Toast.makeText(BrowseMenuActivity.this, "Failed to load menu items", Toast.LENGTH_SHORT).show();
                }

                // Ensure badge is current after data load
                updateCartBadge();
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                menuRecyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(BrowseMenuActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Ensure badge is current on failure too
                updateCartBadge();
            }
        });
    }

    private void applyFilters() {
        adapter.filter(selectedCategory);
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // update the stored Intent so onResume() sees the latest extras
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge(); // refresh badge every time this screen becomes visible
    }

    // New: update cart badge from CartManager
    public static void updateCartBadge() {
        int totalCount = 0;
        try {
            Map<CartItem, Integer> cart = CartManager.getCartItems();
            if (cart != null && !cart.isEmpty()) {
                for (Integer qty : cart.values()) {
                    if (qty != null) totalCount += qty;
                }
            }
        } catch (Exception e) {
            totalCount = 0;
        }

        if (cartBadge == null) return;

        if (totalCount > 0) {
            String text = totalCount > 99 ? "99+" : String.valueOf(totalCount);
            cartBadge.setText(text);
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }
}
