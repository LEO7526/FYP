package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.OrderAdapter;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.api.OrderApiService;
import com.example.yummyrestaurant.models.Order;
import com.example.yummyrestaurant.models.OrderItem;
import com.example.yummyrestaurant.models.OrderItemCustomization;
import com.example.yummyrestaurant.utils.RoleManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends BaseCustomerActivity {

    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    // Example: Replace with actual customer ID from login/session
    private int customerId = -1 ;
    private String role;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        setupBottomFunctionBar(); // reuse the same bar + highlight logic


        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        role = RoleManager.getUserRole();

        if ("customer".equals(role)) {
            customerId = Integer.valueOf(RoleManager.getUserId());
        } else {
            customerId = 0;
        }

        fetchOrderHistory(customerId);
    }

    private void fetchOrderHistory(int cid) {
        OrderApiService service = RetrofitClient.getClient(this).create(OrderApiService.class);
        Call<List<Order>> call = service.getOrdersByCustomer(cid); // 👈 Updated method

        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderList = response.body();
                    
                    // 📊 詳細日誌：追蹤 API 返回的數據結構
                    Log.d("OrderHistory", "🔄 API returned " + orderList.size() + " orders");
                    for (Order order : orderList) {
                        Log.d("OrderHistory", "  Order #" + order.getOid() + ":");
                        if (order.getItems() != null) {
                            Log.d("OrderHistory", "    Items: " + order.getItems().size());
                            for (OrderItem item : order.getItems()) {
                                int custCount = (item.getCustomizations() != null) ? item.getCustomizations().size() : 0;
                                Log.d("OrderHistory", "      - " + item.getName() + " (customizations=" + custCount + ")");
                                
                                // 詳細檢查每個 customization
                                if (item.getCustomizations() != null && item.getCustomizations().size() > 0) {
                                    for (OrderItemCustomization cust : item.getCustomizations()) {
                                        Log.d("OrderHistory", "        * " + cust.getOptionName() + "=" + cust.getChoiceNames());
                                    }
                                }
                            }
                        } else {
                            Log.d("OrderHistory", "    Items: null");
                        }
                    }
                    
                    orderAdapter = new OrderAdapter(orderList);
                    orderRecyclerView.setAdapter(orderAdapter);
                } else {
                    Toast.makeText(OrderHistoryActivity.this, "Failed to load order history", Toast.LENGTH_SHORT).show();
                    Log.e("OrderHistory", "❌ Response not successful: " + (response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e("OrderHistory", "❌ Error fetching orders", t);
                Toast.makeText(OrderHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
