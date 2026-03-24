package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AlertDialog;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.MenuItemAdapter;
import com.example.yummyrestaurant.adapters.PackagesAdapter;
import com.example.yummyrestaurant.api.MenuApi;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.MenuResponse;
import com.example.yummyrestaurant.models.PackagesResponse;
import com.example.yummyrestaurant.models.SetMenu;
import com.example.yummyrestaurant.utils.BadgeManager;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.android.material.navigation.NavigationView;
import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseMenuActivity extends BaseCustomerActivity implements PackagesAdapter.OnPackageClickListener {

    private RecyclerView menuRecyclerView;
    private MenuItemAdapter adapter;
    private PackagesAdapter packagesAdapter;
    private List<SetMenu> packages = new ArrayList<>();
    private LottieAnimationView loadingSpinner;
    private EditText searchBar;
    private String currentLanguage = "en";
    private static boolean login;
    private String selectedCategory = "All Dishes"; // default

    private List<ImageView> functionIcons = new ArrayList<>();
    private Map<ImageView, String> iconBaseNames = new HashMap<>();

    // New: cart badge views
    private ImageView cartIcon;
    private static TextView cartBadge;
    private android.view.View orderTypeHintOverlay;

    // Tab management
    private Button btnMenuTab, btnPackageTab;
    private View tabUnderline;
    private String currentTab = "menu"; // "menu" or "package"

    // Category management
    private LinearLayout menuCategoryList, packageCategoryList;
    private RecyclerView packageRecyclerView;
    private LinearLayout menuContentContainer, packageContentContainer;

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

        setupBottomFunctionBar();

        login = RoleManager.getUser() != null;

        initViews();
        setupRecyclerView();
        setupSearchBar();
        setupTabButtons();
        setupNavigationDrawer();
        setupOrderTypeButtons();

        updateCartBadge();

        // Load menu immediately (always visible)
        loadMenuItemsFromServer();
    }

    private void initViews() {
        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        searchBar = findViewById(R.id.searchBar);

        // cart views (ensure your layout uses the FrameLayout with cartBadge)
        cartIcon = findViewById(R.id.cartIcon);
        cartBadge = findViewById(R.id.cartBadge);
        BadgeManager.registerBadgeView(cartBadge);

        orderTypeHintOverlay = findViewById(R.id.orderTypeHintOverlay);

        // Tab buttons
        btnMenuTab = findViewById(R.id.btnMenuTab);
        btnPackageTab = findViewById(R.id.btnPackageTab);
        tabUnderline = findViewById(R.id.tabUnderline);

        // Category lists
        menuCategoryList = findViewById(R.id.menuCategoryList);
        packageCategoryList = findViewById(R.id.packageCategoryList);

        // Content containers
        menuContentContainer = findViewById(R.id.menuContentContainer);
        packageContentContainer = findViewById(R.id.packageContentContainer);

        // Package RecyclerView
        packageRecyclerView = findViewById(R.id.packageRecyclerView);
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

    private void setupTabButtons() {
        btnMenuTab.setOnClickListener(v -> switchTab("menu"));
        btnPackageTab.setOnClickListener(v -> switchTab("package"));

        // Initialize underline position under Menu tab
        btnMenuTab.post(() -> animateTabUnderline(btnMenuTab));
    }

    private void switchTab(String tab) {
        currentTab = tab;

        // Update button styles
        if ("menu".equals(tab)) {
            btnMenuTab.setTextColor(getResources().getColor(R.color.colorPrimary));
            btnMenuTab.setTypeface(null, android.graphics.Typeface.BOLD);
            btnPackageTab.setTextColor(getResources().getColor(R.color.gray_text));
            btnPackageTab.setTypeface(null, android.graphics.Typeface.NORMAL);

            // Animate underline to menu tab
            animateTabUnderline(btnMenuTab);

            // Show menu content
            showMenuContent();
        } else {
            btnPackageTab.setTextColor(getResources().getColor(R.color.colorPrimary));
            btnPackageTab.setTypeface(null, android.graphics.Typeface.BOLD);
            btnMenuTab.setTextColor(getResources().getColor(R.color.gray_text));
            btnMenuTab.setTypeface(null, android.graphics.Typeface.NORMAL);

            // Animate underline to package tab
            animateTabUnderline(btnPackageTab);

            // Show package content
            showPackageContent();
        }
    }

    private void animateTabUnderline(Button targetButton) {
        targetButton.post(() -> {
            int targetLeft = targetButton.getLeft();
            int targetWidth = targetButton.getWidth();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tabUnderline.getLayoutParams();
            if (params == null) {
                params = new FrameLayout.LayoutParams(targetWidth, 6);
                params.leftMargin = targetLeft;
                params.gravity = android.view.Gravity.BOTTOM;
                tabUnderline.setLayoutParams(params);
                return;
            }
            int startLeft = params.leftMargin;
            int startWidth = params.width;
            final FrameLayout.LayoutParams animParams = params;
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(220);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.addUpdateListener(va -> {
                float t = (float) va.getAnimatedValue();
                animParams.leftMargin = Math.round(startLeft + (targetLeft - startLeft) * t);
                animParams.width = Math.round(startWidth + (targetWidth - startWidth) * t);
                animParams.gravity = android.view.Gravity.BOTTOM;
                tabUnderline.setLayoutParams(animParams);
            });
            anim.start();
        });
    }

    private void showMenuContent() {
        // Show menu section, hide package section
        menuContentContainer.setVisibility(View.VISIBLE);
        packageContentContainer.setVisibility(View.GONE);
        searchBar.setVisibility(View.VISIBLE);

        // Setup menu categories
        populateMenuCategories();
        adapter.showAllItems();
    }

    private void showPackageContent() {
        // Show package section, hide menu section
        menuContentContainer.setVisibility(View.GONE);
        packageContentContainer.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.GONE);

        // Setup package categories
        populatePackageCategories();
        loadPackagesFromServer();
    }

    private void populateMenuCategories() {
        menuCategoryList.removeAllViews();

        // Get unique categories from menu items
        String[] categories = {"All Dishes", "Appetizers", "Soup", "Main Courses", "Dessert", "Drink"};

        for (String category : categories) {
            Button btn = new Button(this);
            btn.setText(category);
            btn.setTextSize(12);
            btn.setBackgroundColor(getResources().getColor(R.color.white));
            btn.setTextColor(getResources().getColor(R.color.purple_700));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 4, 0, 4);
            btn.setLayoutParams(params);
            btn.setPadding(8, 8, 8, 8);

            final String categoryName = category;
            btn.setOnClickListener(v -> {
                selectedCategory = categoryName;
                updateMenuCategoryHighlight(categoryName);
                adapter.setSelectedCategory(categoryName);
                
                // Show all items and scroll to the category
                if (categoryName.equals("All Dishes")) {
                    adapter.showAllItems();
                    smoothScrollToPosition(menuRecyclerView, 0);
                } else {
                    adapter.showAllItems();
                    int position = adapter.getPositionForCategory(categoryName);
                    smoothScrollToPosition(menuRecyclerView, position);
                }
            });

            menuCategoryList.addView(btn);
        }

        // Highlight the first category
        updateMenuCategoryHighlight("All Dishes");
    }

    private void populatePackageCategories() {
        packageCategoryList.removeAllViews();

        // Get unique package types from packages
        Set<String> types = new HashSet<>();
        for (SetMenu pkg : packages) {
            types.add(pkg.getName());
        }

        for (String type : types) {
            Button btn = new Button(this);
            btn.setText(type);
            btn.setTextSize(12);
            btn.setBackgroundColor(getResources().getColor(R.color.white));
            btn.setTextColor(getResources().getColor(R.color.purple_700));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 4, 0, 4);
            btn.setLayoutParams(params);
            btn.setPadding(8, 8, 8, 8);

            final String packageName = type;
            btn.setOnClickListener(v -> {
                // Scroll to package of this type
                updatePackageCategoryHighlight(packageName);
                packagesAdapter.setSelectedPackageName(packageName);
                int position = packagesAdapter.getPositionForPackageName(packageName);
                smoothScrollToPosition(packageRecyclerView, position);
            });

            packageCategoryList.addView(btn);
        }
    }

    private void updateMenuCategoryHighlight(String selectedCategory) {
        for (int i = 0; i < menuCategoryList.getChildCount(); i++) {
            View child = menuCategoryList.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                if (btn.getText().toString().equals(selectedCategory)) {
                    btn.setBackgroundColor(getResources().getColor(R.color.purple_200));
                    btn.setTextColor(getResources().getColor(R.color.purple_700));
                } else {
                    btn.setBackgroundColor(getResources().getColor(R.color.white));
                    btn.setTextColor(getResources().getColor(R.color.gray_text));
                }
            }
        }
    }

    private void updatePackageCategoryHighlight(String selectedType) {
        for (int i = 0; i < packageCategoryList.getChildCount(); i++) {
            View child = packageCategoryList.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                if (btn.getText().toString().equals(selectedType)) {
                    btn.setBackgroundColor(getResources().getColor(R.color.purple_200));
                    btn.setTextColor(getResources().getColor(R.color.purple_700));
                } else {
                    btn.setBackgroundColor(getResources().getColor(R.color.white));
                    btn.setTextColor(getResources().getColor(R.color.gray_text));
                }
            }
        }
    }

    // Custom smooth scroll with 0.5 second animation
    private void smoothScrollToPosition(RecyclerView recyclerView, int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            LinearSmoothScroller smoothScroller = new LinearSmoothScroller(this) {
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }

                @Override
                protected int calculateTimeForScrolling(int dx) {
                    // Fixed 500ms (0.5 seconds) animation duration
                    return 500;
                }
            };
            smoothScroller.setTargetPosition(position);
            layoutManager.startSmoothScroll(smoothScroller);
        }
    }

    private void loadPackagesFromServer() {
        Log.d("BrowseMenu", "Loading packages");
        loadingSpinner.setVisibility(View.VISIBLE);
        packageRecyclerView.setVisibility(View.GONE);

        MenuApi menuApi = RetrofitClient.getClient(this).create(MenuApi.class);
        Call<PackagesResponse> call = menuApi.getPackages();

        call.enqueue(new Callback<PackagesResponse>() {
            @Override
            public void onResponse(Call<PackagesResponse> call, Response<PackagesResponse> response) {
                loadingSpinner.setVisibility(View.GONE);
                packageRecyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    packages = response.body().getData();
                    packagesAdapter = new PackagesAdapter(packages, BrowseMenuActivity.this);
                    packageRecyclerView.setLayoutManager(new LinearLayoutManager(BrowseMenuActivity.this));
                    packageRecyclerView.setAdapter(packagesAdapter);
                    populatePackageCategories();
                } else {
                    Toast.makeText(BrowseMenuActivity.this, "Failed to load packages", Toast.LENGTH_SHORT).show();
                }

                updateCartBadge();
            }

            @Override
            public void onFailure(Call<PackagesResponse> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                packageRecyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(BrowseMenuActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateCartBadge();
            }
        });
    }

    @Override
    public void onPackageClick(SetMenu setMenu) {
        // Check if order type is selected
        if (!CartManager.isOrderTypeSelected()) {
            Toast.makeText(this, "Please select Dine In or Takeaway first", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, BuildSetMenuActivity.class);
        intent.putExtra("package_id", setMenu.getId());
        startActivity(intent);
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

            boolean isLoggedIn = RoleManager.getUserId() != null && !RoleManager.getUserId().isEmpty();

            // ✅ Allow nav_settings even if not logged in
            if (!isLoggedIn && id != R.id.nav_logout && id != R.id.nav_settings) {
                Toast.makeText(this, "Please log in to access this feature.", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_booking) {
                startActivity(new Intent(this, BookingActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_my_bookings) {
                startActivity(new Intent(this, MyBookingsActivity.class));
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
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, CustomerHomeActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            return false;
        });
    }



    private void loadMenuItemsFromServer() {
        Log.d("MenuActivity", "Loading menu items for language: " + currentLanguage);
        loadingSpinner.setVisibility(View.VISIBLE);
        menuRecyclerView.setVisibility(View.GONE);

        MenuApi menuApi = RetrofitClient.getClient(this).create(MenuApi.class);
        Call<MenuResponse> call = menuApi.getMenuItems(currentLanguage);

        call.enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                loadingSpinner.setVisibility(View.GONE);
                menuRecyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    adapter.setMenuItems(response.body().data);
                    populateMenuCategories();
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

    /**
     * Setup order type selection buttons
     */
    private void setupOrderTypeButtons() {
        Button btnDineIn = findViewById(R.id.btnDineIn);
        Button btnTakeaway = findViewById(R.id.btnTakeaway);

        btnDineIn.setOnClickListener(v -> {
            // Launch QR Scanner for table selection
            Toast.makeText(BrowseMenuActivity.this, "Scanning QR code...", Toast.LENGTH_SHORT).show();
            Intent qrScanIntent = new Intent(BrowseMenuActivity.this, 
                    com.example.yummyrestaurant.utils.QRScannerActivity.class);
            startActivity(qrScanIntent);
            // Note: The QRScannerActivity will set order type and table number, 
            // then return to this activity
        });

        btnTakeaway.setOnClickListener(v -> {
            CartManager.setOrderType("takeaway");
            updateOrderTypeButtons("takeaway");
            // Menu already loaded, just update UI state
            adapter.notifyDataSetChanged();
            updateOverlayVisibility();
        });

        // Update UI if already selected
        String selectedType = CartManager.getOrderType();
        if (selectedType != null) {
            updateOrderTypeButtons(selectedType);
            menuRecyclerView.setVisibility(View.VISIBLE);
        }
        
        // Show overlay if no order type selected yet
        updateOverlayVisibility();
    }

    /**
     * Update order type button visual state
     */
    private void updateOrderTypeButtons(String selectedType) {
        Button btnDineIn = findViewById(R.id.btnDineIn);
        Button btnTakeaway = findViewById(R.id.btnTakeaway);

        if ("dine_in".equals(selectedType)) {
            btnDineIn.setBackgroundColor(getResources().getColor(R.color.purple_700));
            btnDineIn.setTextColor(getResources().getColor(R.color.white));
            btnTakeaway.setBackgroundColor(getResources().getColor(R.color.white));
            btnTakeaway.setTextColor(getResources().getColor(R.color.purple_700));
        } else {
            btnTakeaway.setBackgroundColor(getResources().getColor(R.color.purple_700));
            btnTakeaway.setTextColor(getResources().getColor(R.color.white));
            btnDineIn.setBackgroundColor(getResources().getColor(R.color.white));
            btnDineIn.setTextColor(getResources().getColor(R.color.purple_700));
        }
    }
    
    /**
     * Update overlay visibility based on order type selection
     */
    private void updateOverlayVisibility() {
        if (orderTypeHintOverlay != null) {
            if (CartManager.isOrderTypeSelected()) {
                orderTypeHintOverlay.setVisibility(View.GONE);
            } else {
                orderTypeHintOverlay.setVisibility(View.VISIBLE);
            }
        }
    }
}

