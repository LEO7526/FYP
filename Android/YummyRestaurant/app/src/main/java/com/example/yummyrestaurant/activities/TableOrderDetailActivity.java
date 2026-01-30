package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.OrderApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.OrderItem;
import com.example.yummyrestaurant.adapters.OrderItemAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableOrderDetailActivity extends ThemeBaseActivity {

    private TextView tableTitle;
    private TextView totalCostText;
    private ListView orderItemsListView;

    private int tableOrderId;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_order_detail);

        tableTitle = findViewById(R.id.tableTitle);
        totalCostText = findViewById(R.id.totalCostText);
        orderItemsListView = findViewById(R.id.orderItemsListView);

        tableOrderId = getIntent().getIntExtra("table_order_id", -1);
        orderId = getIntent().getIntExtra("order_id", -1);

        tableTitle.setText("Table Order #" + tableOrderId);

        if (orderId != -1) {
            fetchOrderItems(orderId);
        } else {
            Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOrderItems(int orderId) {
        OrderApiService service = RetrofitClient.getClient(this).create(OrderApiService.class);
        Call<List<OrderItem>> call = service.getOrderItems(orderId);

        call.enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderItem> items = response.body();
                    displayOrderItems(items);
                } else {
                    Toast.makeText(TableOrderDetailActivity.this, "Failed to load order items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                Log.e("TableOrderDetail", "Error fetching order items", t);
                Toast.makeText(TableOrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrderItems(List<OrderItem> items) {
        OrderItemAdapter adapter = new OrderItemAdapter(this, items);
        orderItemsListView.setAdapter(adapter);

        double total = 0;
        for (OrderItem item : items) {
            total += item.getItemCost() * item.getQuantity();
        }

        totalCostText.setText("Total: HK$" + String.format("%.2f", total));
    }
}
