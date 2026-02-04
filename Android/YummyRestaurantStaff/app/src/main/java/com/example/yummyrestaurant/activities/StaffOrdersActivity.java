package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.OrdersAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.models.Order;
import com.example.yummyrestaurant.utils.SessionManager;
import com.example.yummyrestaurant.inventory.InventoryActivity;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StaffOrdersActivity extends AppCompatActivity {

    private SessionManager session;
    private RecyclerView recyclerView;
    private OrdersAdapter adapter;

    // 資料列表
    private List<Order> allOrderList;      // 總表
    private List<Order> displayOrderList;  // 顯示表

    private TabLayout tabLayout;

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

        adapter = new OrdersAdapter(this, displayOrderList);
        recyclerView.setAdapter(adapter);

        // 2. 設定 TabLayout
        tabLayout = findViewById(R.id.orderTabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Tab 0 = New, Tab 1 = Making (both show ostatus=1)
                // Tab 2 = Delivered (shows ostatus=2)
                filterOrders(tab.getPosition() + 1);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 3. 載入資料
        fetchOrders();

        // 4. 設定右上角選單
        ImageView menuBtn = findViewById(R.id.staffSettingsIcon);
        menuBtn.setOnClickListener(v -> showPopupMenu(v));

        Toast.makeText(this, "Logged in as: " + session.getStaffName(), Toast.LENGTH_SHORT).show();
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
                                Order order = new Order(
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
    private void filterOrders(int tabPosition) {
        android.util.Log.d("KitchenOrders", "Filtering orders for tab position: " + tabPosition);
        android.util.Log.d("KitchenOrders", "Total orders to filter: " + allOrderList.size());
        
        displayOrderList.clear();
        int filteredCount = 0;
        
        for (Order order : allOrderList) {
            android.util.Log.d("KitchenOrders", "Checking order OID: " + order.getOid() + ", Status: " + order.getStatus());
            
            // Tab 1 (New) 和 Tab 2 (Making) 都顯示 ostatus = 1
            // Tab 3 (Delivered) 顯示 ostatus = 2
            if ((tabPosition == 1 || tabPosition == 2) && order.getStatus() == 1) {
                displayOrderList.add(order);
                filteredCount++;
                android.util.Log.d("KitchenOrders", "Added to New/Making tab - OID: " + order.getOid());
            } else if (tabPosition == 3 && order.getStatus() == 2) {
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
        int countNew = 0;
        int countMaking = 0;
        int countDelivered = 0;

        for (Order order : allOrderList) {
            // New 和 Making 都計算 ostatus = 1 的訂單
            if (order.getStatus() == 1) {
                countNew++;
                countMaking++;
            }
            // Delivered 計算 ostatus = 2 的訂單
            else if (order.getStatus() == 2) {
                countDelivered++;
            }
        }

        if (tabLayout.getTabAt(0) != null) tabLayout.getTabAt(0).setText("New (" + countNew + ")");
        if (tabLayout.getTabAt(1) != null) tabLayout.getTabAt(1).setText("Making (" + countMaking + ")");
        if (tabLayout.getTabAt(2) != null) tabLayout.getTabAt(2).setText("Delivered (" + countDelivered + ")");
    }

    // 選單邏輯
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.staff_main_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_orders) return true;
            else if (id == R.id.nav_tables) {
                startActivity(new Intent(StaffOrdersActivity.this, TableSelectionActivity.class));
                return true;
            } else if (id == R.id.nav_create_dish) {
                startActivity(new Intent(StaffOrdersActivity.this, CreateDishActivity.class));
                return true;
            } else if (id == R.id.nav_create_coupon) {
                startActivity(new Intent(StaffOrdersActivity.this, CreateCouponActivity.class));
                return true;
            } else if (id == R.id.nav_inventory_system) {
                startActivity(new Intent(StaffOrdersActivity.this, InventoryActivity.class));
                return true;
            } else if (id == R.id.nav_logout) {
                logout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logout() {
        session.logout();
        startActivity(new Intent(this, StaffLoginActivity.class));
        finish();
    }
}