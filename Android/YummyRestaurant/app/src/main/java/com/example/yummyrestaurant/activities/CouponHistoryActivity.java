package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.CouponHistoryAdapter;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CouponHistoryItem;
import com.example.yummyrestaurant.models.CouponHistoryResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CouponHistoryActivity extends BaseCustomerActivity {

    private RecyclerView recyclerView;
    private CouponHistoryAdapter adapter;
    private final List<CouponHistoryItem> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_history);

        setupBottomFunctionBar();

        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CouponHistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        // ✅ Get customerId from Intent
        int customerId = getIntent().getIntExtra("customer_id", 0);

        if (customerId != 0) {
            loadHistory(customerId);
        } else {
            Toast.makeText(this, "Please log in to view history", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHistory(int customerId) {
        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.getCouponHistory(customerId).enqueue(new Callback<CouponHistoryResponse>() {
            @Override
            public void onResponse(Call<CouponHistoryResponse> call, Response<CouponHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    historyList.clear();
                    historyList.addAll(response.body().getHistory());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CouponHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CouponHistoryResponse> call, Throwable t) {
                Toast.makeText(CouponHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}