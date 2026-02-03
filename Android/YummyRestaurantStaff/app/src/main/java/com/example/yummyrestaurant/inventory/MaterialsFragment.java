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

public class MaterialsFragment extends Fragment implements MaterialAdapter.OnMaterialClickListener {

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
                    Toast.makeText(getContext(), "Failed to load materials.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Material>>> call, @NonNull Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMaterialClick(Material material) {
        showQuantityInputDialog(material);
    }

    private void showQuantityInputDialog(Material material) {
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        String currentStockInfo = String.format(Locale.getDefault(), "Current stock: %.2f %s", material.mqty, material.unit);
        input.setHint(currentStockInfo);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Adjust Stock for: " + material.mname)
                .setMessage("Enter the quantity to add or subtract.")
                .setView(input)
                .setPositiveButton("Add (入庫)", (dialog, which) -> handleDialogInput(input.getText().toString(), material, "in"))
                // *** 這裡就是修改的地方 ***
                .setNegativeButton("Decrease (出庫)", (dialog, which) -> handleDialogInput(input.getText().toString(), material, "out"))
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void handleDialogInput(String quantityStr, Material material, String action) {
        if (quantityStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a quantity.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            double quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(getContext(), "Please enter a positive quantity.", Toast.LENGTH_SHORT).show();
                return;
            }
            performStockAdjustment(material.mid, quantity, action);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), "Adjustment successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = (apiResponse != null) ? apiResponse.message : "Update failed.";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    fetchMaterials();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}