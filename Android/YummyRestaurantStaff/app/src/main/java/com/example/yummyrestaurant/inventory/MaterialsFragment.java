package com.example.yummyrestaurant.inventory;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.yummyrestaurant.R;

public class MaterialsFragment extends Fragment implements MaterialAdapter.OnMaterialClickListener, RefreshableTab {

    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialAdapter materialAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_materials, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getClient().create(ApiService.class);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_materials);
        materialAdapter = new MaterialAdapter(this);
        recyclerView.setAdapter(materialAdapter);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_materials);
        swipeRefreshLayout.setOnRefreshListener(this::fetchMaterials);
        fetchMaterials();
    }

    private void fetchMaterials() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        apiService.getMaterials().enqueue(new Callback<ApiResponse<List<Material>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Material>>> call, @NonNull Response<ApiResponse<List<Material>>> response) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                ApiResponse<List<Material>> apiResponse = response.body();
                if (isAdded() && response.isSuccessful() && apiResponse != null && apiResponse.success) {
                    materialAdapter.setMaterials(apiResponse.data);
                } else if (isAdded()) {
                    Toast.makeText(getContext(), R.string.failed_load_ingredients, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Material>>> call, @NonNull Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.network_error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMaterials();
    }

    @Override
    public void refreshData() {
        fetchMaterials();
    }

    @Override
    public void onMaterialClick(Material material) {
        showQuantityInputDialog(material);
    }

    private void showQuantityInputDialog(Material material) {
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        String currentStockInfo = getString(R.string.current_stock_info, material.mqty, material.unit);
        input.setHint(currentStockInfo);
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.adjust_stock_for, material.mname))
            .setMessage(R.string.enter_quantity_add_subtract)
                .setView(input)
            .setPositiveButton(R.string.add_stock, (dialog, which) -> handleDialogInput(input.getText().toString(), material, "in"))
                // *** 這裡就是修改的地方 ***
            .setNegativeButton(R.string.decrease_stock, (dialog, which) -> handleDialogInput(input.getText().toString(), material, "out"))
            .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void handleDialogInput(String quantityStr, Material material, String action) {
        if (quantityStr.isEmpty()) {
            Toast.makeText(getContext(), R.string.please_enter_quantity, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            double quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(getContext(), R.string.please_enter_positive_quantity, Toast.LENGTH_SHORT).show();
                return;
            }
            performStockAdjustment(material.mid, quantity, action);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.invalid_number_format, Toast.LENGTH_SHORT).show();
        }
    }

    private void performStockAdjustment(int materialId, double quantity, String action) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        StockAdjustRequest request = new StockAdjustRequest(materialId, quantity, action);
        apiService.adjustStock(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (isAdded()) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (response.isSuccessful() && apiResponse != null && apiResponse.success) {
                        Toast.makeText(getContext(), R.string.adjustment_successful, Toast.LENGTH_SHORT).show();
                    } else {
                        String message = (apiResponse != null) ? apiResponse.message : getString(R.string.update_failed);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    fetchMaterials();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), getString(R.string.network_error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}