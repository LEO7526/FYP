package com.example.yummyrestaurant.inventory;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.yummyrestaurant.R;

public class HistoryActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private HistoryAdapter historyAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Consumption History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        setupRecyclerView();
        setupSwipeToRefresh();

        fetchHistory();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_history);
        historyAdapter = new HistoryAdapter();
        recyclerView.setAdapter(historyAdapter);
    }

    private void setupSwipeToRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_history);
        swipeRefreshLayout.setOnRefreshListener(this::fetchHistory);
    }

    private void fetchHistory() {
        swipeRefreshLayout.setRefreshing(true);
        // 呼叫新的 getConsumptionHistory() API
        apiService.getConsumptionHistory().enqueue(new Callback<ApiResponse<List<ConsumptionLog>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ConsumptionLog>>> call, @NonNull Response<ApiResponse<List<ConsumptionLog>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                ApiResponse<List<ConsumptionLog>> apiResponse = response.body();
                if (response.isSuccessful() && apiResponse != null && apiResponse.success) {
                    historyAdapter.setHistoryLogs(apiResponse.data);
                } else {
                    Toast.makeText(HistoryActivity.this, "Failed to load history.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ConsumptionLog>>> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(HistoryActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}