package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.StaffOrdersAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.models.StaffOrder;
import com.example.yummyrestaurant.utils.SessionManager;
import com.example.yummyrestaurant.inventory.InventoryActivity;
import com.example.yummyrestaurant.inventory.HistoryActivity;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Looper;

public class StaffOrdersActivity extends AppCompatActivity {

    private SessionManager session;
    private RecyclerView recyclerView;
    private StaffOrdersAdapter adapter;

    // 資料列表
    private List<StaffOrder> allOrderList;      // 總表
    private List<StaffOrder> displayOrderList;  // 顯示表

    private TabLayout tabLayout;
    
    // Navigation Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    
    // Auto-refresh mechanism
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_orders);
        
        android.util.Log.d("KitchenOrders", "StaffOrdersActivity onCreate started");

        session = new SessionManager(this);

        // 初始化 List
        allOrderList = new ArrayList<>();
        displayOrderList = new ArrayList<>();
        
        android.util.Log.d("KitchenOrders", "Order lists initialized");

        // 1. 設定 RecyclerView
        recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StaffOrdersAdapter(this, displayOrderList);
        recyclerView.setAdapter(adapter);
        
        android.util.Log.d("KitchenOrders", "RecyclerView and adapter set up");

        // 2. 設定 TabLayout
        tabLayout = findViewById(R.id.orderTabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                android.util.Log.d("KitchenOrders", "Tab selected: position " + position);
                // Tab 0 = Making (ostatus=1), Tab 1 = Delivered (ostatus=2)
                filterOrders(position);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 3. 載入資料
        fetchOrders();

        // 4. 設定自動刷新機制
        setupAutoRefresh();

        // 5. 設定導航抽屜
        setupNavigationDrawer();

        Toast.makeText(this, "Logged in as: " + session.getStaffName(), Toast.LENGTH_SHORT).show();
    }
    
    private void setupAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("KitchenOrders", "Auto-refresh: Fetching orders");
                fetchOrders();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        android.util.Log.d("KitchenOrders", "Auto-refresh mechanism set up with " + REFRESH_INTERVAL + "ms interval");
    }
    
    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        
        // 設定漢堡選單按鈕
        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu);
        if (hamburgerMenu != null) {
            hamburgerMenu.setOnClickListener(v -> {
                Toast.makeText(this, "Opening drawer...", Toast.LENGTH_SHORT).show();
                drawerLayout.openDrawer(GravityCompat.START);
            });
        }
        
        // 設定導航選單項目點擊
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        }
        
        // 設定標題中的員工資訊
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView staffName = headerView.findViewById(R.id.staff_name);
            TextView staffRole = headerView.findViewById(R.id.staff_role);
            
            if (staffName != null) {
                staffName.setText(session.getStaffName());
            }
            if (staffRole != null) {
                staffRole.setText("🍳 Kitchen Staff");
            }
        }
    }

    // 從 API 抓取訂單 (支援新的 Order Type)
    private void fetchOrders() {
        android.util.Log.d("KitchenOrders", "Fetching orders from: " + ApiConstants.getOrders());
        
        StringRequest request = new StringRequest(Request.Method.GET, ApiConstants.getOrders(),
                response -> {
                    android.util.Log.d("KitchenOrders", "API Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            allOrderList.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                // 這裡使用了新的 Order 建構子
                                StaffOrder order = new StaffOrder(
                                        obj.getInt("oid"),
                                        obj.getString("table_number"), // 現在是 String (可能包含 "Takeaway")
                                        obj.getString("odate"),
                                        obj.getInt("ostatus"),
                                        obj.getString("summary"),
                                        obj.optString("type", "dine_in") // 新增 type
                                );
                                
                                // Debug logging
                                android.util.Log.d("KitchenOrders", "Loaded order - OID: " + order.getOid() + ", Status: " + order.getStatus() + ", Summary: " + order.getSummary());
                                
                                allOrderList.add(order);
                            }
                            
                            android.util.Log.d("KitchenOrders", "Total orders loaded: " + allOrderList.size());

                            // 更新 Tab 數字
                            updateTabCounts();

                            // 刷新顯示
                            int currentTabPosition = tabLayout.getSelectedTabPosition();
                            filterOrders(currentTabPosition);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Data Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    android.util.Log.e("KitchenOrders", "Network error: " + error.toString());
                    if (error.getMessage() != null) {
                        android.util.Log.e("KitchenOrders", "Error message: " + error.getMessage());
                    }
                    Toast.makeText(this, "Network Error: " + (error.getMessage() != null ? error.getMessage() : "Unknown network error"), Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    // 篩選訂單
    private void filterOrders(int tabPosition) {
        android.util.Log.d("KitchenOrders", "Filtering orders for tab position: " + tabPosition);
        android.util.Log.d("KitchenOrders", "Total orders to filter: " + allOrderList.size());
        
        displayOrderList.clear();
        int filteredCount = 0;
        
        for (StaffOrder order : allOrderList) {
            android.util.Log.d("KitchenOrders", "Checking order OID: " + order.getOid() + ", Status: " + order.getStatus());
            
            // Tab 0 (Making) 顯示 ostatus = 1 - orders ready to start making
            // Tab 1 (Delivered) 顯示 ostatus = 2 - completed orders
            if (tabPosition == 0 && order.getStatus() == 1) {
                displayOrderList.add(order);
                filteredCount++;
                android.util.Log.d("KitchenOrders", "Added to Making tab - OID: " + order.getOid());
            } else if (tabPosition == 1 && order.getStatus() == 2) {
                displayOrderList.add(order);
                filteredCount++;
                android.util.Log.d("KitchenOrders", "Added to Delivered tab - OID: " + order.getOid());
            }
        }
        
        android.util.Log.d("KitchenOrders", "Filtered orders count: " + filteredCount + " for tab: " + tabPosition);
        adapter.notifyDataSetChanged();
    }

    // 更新 Tab 數字
    private void updateTabCounts() {
        android.util.Log.d("KitchenOrders", "Updating tab counts for " + allOrderList.size() + " orders");
        
        int countNew = 0;
        int countMaking = 0;
        int countDelivered = 0;

        for (StaffOrder order : allOrderList) {
            android.util.Log.d("KitchenOrders", "Counting order OID: " + order.getOid() + ", Status: " + order.getStatus());
            
            // New 和 Making 都計算 ostatus = 1 的訂單
            if (order.getStatus() == 1) {
                countNew++;
                countMaking++;
                android.util.Log.d("KitchenOrders", "Counted for New/Making tabs");
            }
            // Delivered 計算 ostatus = 2 的訂單
            else if (order.getStatus() == 2) {
                countDelivered++;
                android.util.Log.d("KitchenOrders", "Counted for Delivered tab");
            }
        }
        
        android.util.Log.d("KitchenOrders", "Final counts - New: " + countNew + ", Making: " + countMaking + ", Delivered: " + countDelivered);

        if (tabLayout.getTabAt(0) != null) tabLayout.getTabAt(0).setText("New (" + countNew + ")");
        if (tabLayout.getTabAt(1) != null) tabLayout.getTabAt(1).setText("Making (" + countMaking + ")");
        if (tabLayout.getTabAt(2) != null) tabLayout.getTabAt(2).setText("Delivered (" + countDelivered + ")");
        
        android.util.Log.d("KitchenOrders", "Tab titles updated");
    }

    // 選單邏輯
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_orders) {
            // 已在當前頁面，關閉抽屜即可
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_tables) {
            startActivity(new Intent(StaffOrdersActivity.this, TableSelectionActivity.class));
            return true;
        } else if (id == R.id.nav_cash_payment) {
            startActivity(new Intent(StaffOrdersActivity.this, CashPaymentManagementActivity.class));
            return true;
        } else if (id == R.id.nav_takeaway_cash) {
            startActivity(new Intent(StaffOrdersActivity.this, TakeawayCashPaymentActivity.class));
            return true;
        } else if (id == R.id.nav_create_dish) {
            startActivity(new Intent(StaffOrdersActivity.this, CreateDishActivity.class));
            return true;
        } else if (id == R.id.nav_create_material) {
            startActivity(new Intent(StaffOrdersActivity.this, CreateMaterialActivity.class));
            return true;
        } else if (id == R.id.nav_create_coupon) {
            startActivity(new Intent(StaffOrdersActivity.this, CreateCouponActivity.class));
            return true;
        } else if (id == R.id.nav_inventory_system) {
            startActivity(new Intent(StaffOrdersActivity.this, InventoryActivity.class));
            return true;
        } else if (id == R.id.nav_inventory_history) {
            startActivity(new Intent(StaffOrdersActivity.this, HistoryActivity.class));
            return true;
        } else if (id == R.id.nav_logout) {
            logout();
            return true;
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("KitchenOrders", "onResume: Refreshing orders and starting auto-refresh");
        // Refresh orders when activity becomes visible (e.g., returning from another screen)
        fetchOrders();
        // Start auto-refresh
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        android.util.Log.d("KitchenOrders", "onPause: Stopping auto-refresh");
        // Stop auto-refresh when activity is not visible
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}