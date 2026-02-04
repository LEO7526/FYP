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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_orders);

        session = new SessionManager(this);

        // 初始化 List
        allOrderList = new ArrayList<>();
        displayOrderList = new ArrayList<>();

        // 1. 設定 RecyclerView
        recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StaffOrdersAdapter(this, displayOrderList);
        recyclerView.setAdapter(adapter);

        // 2. 設定 TabLayout
        tabLayout = findViewById(R.id.orderTabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Tab 0 = New (Status 1)
                // Tab 1 = Cooking (Status 2)
                // Tab 2 = Served (Status 3)
                filterOrders(tab.getPosition() + 1);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 3. 載入資料
        fetchOrders();

        // 4. 設定導航抽屜
        setupNavigationDrawer();

        Toast.makeText(this, "Logged in as: " + session.getStaffName(), Toast.LENGTH_SHORT).show();
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
        StringRequest request = new StringRequest(Request.Method.GET, ApiConstants.GET_ORDERS,
                response -> {
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
                                allOrderList.add(order);
                            }

                            // 更新 Tab 數字
                            updateTabCounts();

                            // 刷新顯示
                            int currentTabPosition = tabLayout.getSelectedTabPosition();
                            filterOrders(currentTabPosition + 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Data Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    // 篩選訂單
    private void filterOrders(int status) {
        displayOrderList.clear();
        for (StaffOrder order : allOrderList) {
            if (order.getStatus() == status) {
                displayOrderList.add(order);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // 更新 Tab 數字
    private void updateTabCounts() {
        int countNew = 0;
        int countCooking = 0;
        int countServed = 0;

        for (StaffOrder order : allOrderList) {
            if (order.getStatus() == 1) countNew++;
            else if (order.getStatus() == 2) countCooking++;
            else if (order.getStatus() == 3) countServed++;
        }

        if (tabLayout.getTabAt(0) != null) tabLayout.getTabAt(0).setText("New (" + countNew + ")");
        if (tabLayout.getTabAt(1) != null) tabLayout.getTabAt(1).setText("Cooking (" + countCooking + ")");
        if (tabLayout.getTabAt(2) != null) tabLayout.getTabAt(2).setText("Delivered (" + countServed + ")");
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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}