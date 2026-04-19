package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.MaterialApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.inventory.ApiResponse;
import com.example.yummyrestaurant.inventory.Material;
import com.example.yummyrestaurant.inventory.MaterialAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateMaterialActivity extends StaffBaseActivity {
    private EditText etName, etStock, etReorderLevel;
    private Spinner unitSpinner;
    private SwipeRefreshLayout viewMaterialList;
    private RecyclerView recyclerMaterialList;
    private TextView tvEmptyState;

    private View viewCreateMaterial;
    private MaterialApiService materialApiService;
    private MaterialAdapter materialAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_material);

        initViews();
        setupSpinner();
        setupMaterialList();

        materialApiService = RetrofitClient.getClient(this).create(MaterialApiService.class);
        findViewById(R.id.btnSaveMaterial).setOnClickListener(v -> submitMaterial());
        viewMaterialList.setOnRefreshListener(this::fetchMaterials);

        setupTabs();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabMaterial);
        viewCreateMaterial = findViewById(R.id.viewCreateMaterial);
        viewMaterialList = findViewById(R.id.viewMaterialList);
        recyclerMaterialList = findViewById(R.id.recyclerMaterialList);
        tvEmptyState = findViewById(R.id.tvMaterialEmpty);

        etName = findViewById(R.id.materialName);
        etStock = findViewById(R.id.materialStock);
        etReorderLevel = findViewById(R.id.materialCost);
        unitSpinner = findViewById(R.id.unitSpinner);
    }

    private void setupSpinner() {
        String[] units = {"grams", "kg", "ml", "L", "pcs"};
        unitSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units));
    }

    private void setupMaterialList() {
        materialAdapter = new MaterialAdapter(material -> {
            // Keep list view read-only in this page; stock adjustment stays in Materials tab.
        });
        recyclerMaterialList.setLayoutManager(new LinearLayoutManager(this));
        recyclerMaterialList.setAdapter(materialAdapter);
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(R.string.add_ingredient), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.view_ingredients));

        showCreateView();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showCreateView();
                } else {
                    showListView();
                    fetchMaterials();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    fetchMaterials();
                }
            }
        });
    }

    private void showCreateView() {
        viewCreateMaterial.setVisibility(View.VISIBLE);
        viewMaterialList.setVisibility(View.GONE);
    }

    private void showListView() {
        viewCreateMaterial.setVisibility(View.GONE);
        viewMaterialList.setVisibility(View.VISIBLE);
    }

    private void fetchMaterials() {
        viewMaterialList.setRefreshing(true);
        materialApiService.getMaterials().enqueue(new Callback<ApiResponse<List<Material>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Material>>> call,
                                   @NonNull Response<ApiResponse<List<Material>>> response) {
                viewMaterialList.setRefreshing(false);
                ApiResponse<List<Material>> body = response.body();
                if (response.isSuccessful() && body != null && body.success && body.data != null) {
                    materialAdapter.setMaterials(body.data);
                    boolean isEmpty = body.data.isEmpty();
                    tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(CreateMaterialActivity.this, R.string.failed_load_ingredients, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Material>>> call, @NonNull Throwable t) {
                viewMaterialList.setRefreshing(false);
                Toast.makeText(CreateMaterialActivity.this,
                        getString(R.string.error_prefix, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitMaterial() {
        String name = etName.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String reorderStr = etReorderLevel.getText().toString().trim();
        String unit = unitSpinner.getSelectedItem().toString();

        if (name.isEmpty() || stockStr.isEmpty() || reorderStr.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        final double stock;
        final double reorderLevel;
        try {
            stock = Double.parseDouble(stockStr);
            reorderLevel = Double.parseDouble(reorderStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.please_enter_valid_numbers, Toast.LENGTH_SHORT).show();
            return;
        }

        if (stock < 0 || reorderLevel < 0) {
            Toast.makeText(this, R.string.values_cannot_be_negative, Toast.LENGTH_SHORT).show();
            return;
        }

        Material material = new Material();
        material.mname = name;
        material.mqty = stock;
        material.unit = unit;
        material.reorderLevel = reorderLevel;

        materialApiService.addMaterial(material).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                ApiResponse<Void> body = response.body();
                if (response.isSuccessful() && body != null && body.success) {
                    Toast.makeText(CreateMaterialActivity.this, R.string.ingredient_added_successfully, Toast.LENGTH_SHORT).show();
                    clearForm();

                    TabLayout.Tab listTab = tabLayout.getTabAt(1);
                    if (listTab != null) {
                        listTab.select();
                    } else {
                        showListView();
                        fetchMaterials();
                    }
                } else {
                    String msg = body != null && body.message != null ? body.message : getString(R.string.failed_create_ingredient);
                    Toast.makeText(CreateMaterialActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                Toast.makeText(CreateMaterialActivity.this,
                        getString(R.string.error_prefix, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        etName.setText("");
        etStock.setText("");
        etReorderLevel.setText("");
        unitSpinner.setSelection(0);
    }
}