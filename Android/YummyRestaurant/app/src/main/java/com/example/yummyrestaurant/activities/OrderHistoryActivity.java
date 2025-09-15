package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.OrderAdapter;
import com.example.yummyrestaurant.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data for demonstration
        orderList = new ArrayList<>();
        orderList.add(new Order("Order #1001", "2x Burger, 1x Fries", "2025-09-10", "$18.50"));
        orderList.add(new Order("Order #1002", "1x Pizza", "2025-09-12", "$12.00"));
        orderList.add(new Order("Order #1003", "3x Sushi Rolls", "2025-09-14", "$24.75"));

        orderAdapter = new OrderAdapter(orderList);
        orderRecyclerView.setAdapter(orderAdapter);
    }
}