package com.example.yummyrestaurant.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.yummyrestaurant.R;

public class ProductionFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private FoodStockAdapter foodStockAdapter;
    private ApiService apiService;
    private TextView textEmptyWarning; // 顯示「庫存充足」的文字

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 請確保你的 fragment_production.xml 裡有一個 ID 為 text_empty_warning 的 TextView
        return inflater.inflate(R.layout.fragment_production, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(super.onCreateView(null, null, null), null); // 此行僅為示意，請保留原本生成的程式碼

        // 初始化組件
        apiService = ApiClient.getClient().create(ApiService.class);
        textEmptyWarning = view.findViewById(R.id.text_empty_warning);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_production);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        foodStockAdapter = new FoodStockAdapter();
        recyclerView.setAdapter(foodStockAdapter);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_production);
        swipeRefreshLayout.setOnRefreshListener(this::fetchFoodStock);

        fetchFoodStock();
    }

    private void fetchFoodStock() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        apiService.getFoodStock().enqueue(new Callback<ApiResponse<List<FoodStock>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<FoodStock>>> call, @NonNull Response<ApiResponse<List<FoodStock>>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                ApiResponse<List<FoodStock>> apiResponse = response.body();
                if (isAdded() && response.isSuccessful() && apiResponse != null && apiResponse.success) {
                    List<FoodStock> data = apiResponse.data;
                    foodStockAdapter.setFoodStockList(data);

                    // 邏輯控制：如果沒有預警項目，顯示「全部充足」
                    if (data == null || data.isEmpty()) {
                        textEmptyWarning.setVisibility(View.VISIBLE);
                    } else {
                        textEmptyWarning.setVisibility(View.GONE);
                    }
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load production data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<FoodStock>>> call, @NonNull Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}