package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.OrderAdapter;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.api.OrderApiService;
import com.example.yummyrestaurant.models.Order;
import com.example.yummyrestaurant.utils.RoleManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

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
        OrderApiService service = RetrofitClient.getClient().create(OrderApiService.class);
        Call<List<Order>> call = service.getOrdersByCustomer(cid); // 👈 Updated method

        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderList = response.body();
                    orderAdapter = new OrderAdapter(orderList);
                    orderRecyclerView.setAdapter(orderAdapter);
                } else {
                    Toast.makeText(OrderHistoryActivity.this, "Failed to load order history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e("OrderHistory", "Error fetching orders", t);
                Toast.makeText(OrderHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}