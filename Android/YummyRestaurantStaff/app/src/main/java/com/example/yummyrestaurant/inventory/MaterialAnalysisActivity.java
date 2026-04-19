package com.example.yummyrestaurant.inventory;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.example.yummyrestaurant.activities.StaffBaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConstants;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MaterialAnalysisActivity extends StaffBaseActivity {

    private RecyclerView recyclerViewAnalysis;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textSummary;
    private MaterialButton btnAutoRestock;
    
    private MaterialAnalysisAdapter adapter;
    private RequestQueue requestQueue;
    private List<MaterialAnalysisItem> analysisList = new ArrayList<>();
    private JSONObject analysisData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_analysis);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.ingredient_analysis_restock_review);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        initRecyclerView();
        requestQueue = Volley.newRequestQueue(this);

        // Load analysis data on startup
        fetchAnalysisData();

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshAnalysisData);
    }

    private void initViews() {
        recyclerViewAnalysis = findViewById(R.id.recyclerView_analysis);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        textSummary = findViewById(R.id.textSummary);
        btnAutoRestock = findViewById(R.id.btnAutoRestock);

        btnAutoRestock.setOnClickListener(v -> {
            if (analysisList.isEmpty()) {
                Toast.makeText(this, R.string.no_ingredients_to_restock, Toast.LENGTH_SHORT).show();
                return;
            }
            triggerAutoRestock();
        });
    }

    private void initRecyclerView() {
        adapter = new MaterialAnalysisAdapter(analysisList, this::restockSingleItem);
        recyclerViewAnalysis.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnalysis.setAdapter(adapter);
    }

    private void fetchAnalysisData() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        String url = ApiConstants.BASE_URL + "analyze_material_consumption.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    try {
                        analysisData = new JSONObject(response);
                        if (analysisData.getBoolean("success")) {
                            parseAndDisplayAnalysis(analysisData);
                        } else {
                            showError(getString(R.string.api_error_prefix, analysisData.getString("error")));
                        }
                    } catch (JSONException e) {
                        showError(getString(R.string.json_parse_error_prefix, e.getMessage()));
                    }
                },
                error -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    showError(getString(R.string.network_error_prefix, error.toString()));
                });

        requestQueue.add(request);
    }

    private void parseAndDisplayAnalysis(JSONObject data) throws JSONException {
        analysisList.clear();

        // Summary
        JSONObject summaryObj = data.getJSONObject("summary");
        String periodObj = data.getJSONObject("period").getString("start_date") + " ~ " +
                data.getJSONObject("period").getString("end_date");
        
        String summaryText = getString(
            R.string.material_analysis_summary,
                periodObj,
                summaryObj.getInt("total_materials"),
                summaryObj.getInt("materials_needing_restock"),
                summaryObj.getDouble("total_restock_amount_needed")
        );
        textSummary.setText(summaryText);

        boolean needsRestock = summaryObj.getInt("materials_needing_restock") > 0;
        btnAutoRestock.setVisibility(android.view.View.VISIBLE);
        btnAutoRestock.setEnabled(needsRestock);
        btnAutoRestock.setText(needsRestock ? getString(R.string.auto_restock_now) : getString(R.string.all_stocked));

        // Parse materials
        JSONArray materials = data.getJSONArray("materials");
        for (int i = 0; i < materials.length(); i++) {
            JSONObject material = materials.getJSONObject(i);

            MaterialAnalysisItem item = new MaterialAnalysisItem(
                    material.getInt("mid"),
                    material.getString("mname"),
                    material.getString("unit"),
                    material.getDouble("current_qty"),
                    material.getDouble("avg_weekly_usage"),
                    material.getDouble("restock_point_120"),
                    material.getString("status"),
                    material.getDouble("restock_amount")
            );

            analysisList.add(item);
        }

        adapter.notifyDataSetChanged();
    }

    private void refreshAnalysisData() {
        fetchAnalysisData();
    }

    private void triggerAutoRestock() {
        if (analysisList.isEmpty() || analysisData == null) {
            Toast.makeText(this, R.string.no_analysis_data_available, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);

        try {
            JSONArray materialsToRestock = new JSONArray();
            JSONArray materials = analysisData.getJSONArray("materials");

            for (int i = 0; i < materials.length(); i++) {
                JSONObject material = materials.getJSONObject(i);
                if ("NEEDS_RESTOCK".equals(material.getString("status"))) {
                    JSONObject restockItem = new JSONObject();
                    restockItem.put("mid", material.getInt("mid"));
                    restockItem.put("quantity", material.getDouble("restock_amount"));
                    materialsToRestock.put(restockItem);
                }
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("materials", materialsToRestock);
            requestBody.put("source", "analysis_single");
            requestBody.put("mode", "single");

            String url = ApiConstants.BASE_URL + "auto_restock_materials.php";

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        progressBar.setVisibility(android.view.View.GONE);

                        try {
                            JSONObject result = new JSONObject(response);
                            if (result.getBoolean("success")) {
                                Toast.makeText(this, R.string.auto_restock_completed, Toast.LENGTH_SHORT).show();
                                refreshAnalysisData();
                            } else {
                                showError(getString(R.string.restock_failed_prefix, result.getString("error")));
                            }
                        } catch (JSONException e) {
                            showError(getString(R.string.json_error_prefix, e.getMessage()));
                        }
                    },
                    error -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        showError(getString(R.string.network_error_prefix, error.toString()));
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    return requestBody.toString().getBytes();
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            progressBar.setVisibility(android.view.View.GONE);
            showError(getString(R.string.error_preparing_request_prefix, e.getMessage()));
        }
    }

    private void restockSingleItem(MaterialAnalysisItem item) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        try {
            JSONArray materialsToRestock = new JSONArray();
            JSONObject restockItem = new JSONObject();
            restockItem.put("mid", item.mid);
            restockItem.put("quantity", item.restockAmount);
            materialsToRestock.put(restockItem);

            JSONObject requestBody = new JSONObject();
            requestBody.put("materials", materialsToRestock);
            requestBody.put("source", "analysis_bulk");
            requestBody.put("mode", "bulk");

            String url = ApiConstants.BASE_URL + "auto_restock_materials.php";

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        progressBar.setVisibility(android.view.View.GONE);

                        try {
                            JSONObject result = new JSONObject(response);
                            if (result.getBoolean("success")) {
                                Toast.makeText(this, getString(R.string.item_restocked_successfully, item.mname), Toast.LENGTH_SHORT).show();
                                refreshAnalysisData(); // Refresh to show updated quantities
                            } else {
                                showError(getString(R.string.restock_failed_prefix, result.getString("error")));
                            }
                        } catch (JSONException e) {
                            showError(getString(R.string.json_error_prefix, e.getMessage()));
                        }
                    },
                    error -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        showError(getString(R.string.network_error_prefix, error.toString()));
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    return requestBody.toString().getBytes();
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            progressBar.setVisibility(android.view.View.GONE);
            showError(getString(R.string.error_preparing_request_prefix, e.getMessage()));
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_analysis, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_refresh) {
            refreshAnalysisData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Data class for analysis items
    public static class MaterialAnalysisItem {
        public int mid;
        public String mname;
        public String unit;
        public double currentQty;
        public double avgWeeklyUsage;
        public double restockPoint;
        public String status;
        public double restockAmount;

        public MaterialAnalysisItem(int mid, String mname, String unit, double currentQty,
                                    double avgWeeklyUsage, double restockPoint, String status, double restockAmount) {
            this.mid = mid;
            this.mname = mname;
            this.unit = unit;
            this.currentQty = currentQty;
            this.avgWeeklyUsage = avgWeeklyUsage;
            this.restockPoint = restockPoint;
            this.status = status;
            this.restockAmount = restockAmount;
        }
    }
}
