package com.example.yummyrestaurant.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConstants;
import com.example.yummyrestaurant.inventory.MaterialAnalysisActivity.MaterialAnalysisItem;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MaterialAnalysisFragment extends Fragment implements RefreshableTab {

    private RecyclerView recyclerViewAnalysis;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textSummary;
    private MaterialButton btnAutoRestock;

    private MaterialAnalysisAdapter adapter;
    private RequestQueue requestQueue;
    private final List<MaterialAnalysisItem> analysisList = new ArrayList<>();
    private JSONObject analysisData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_material_analysis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewAnalysis = view.findViewById(R.id.recyclerView_analysis);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        textSummary = view.findViewById(R.id.textSummary);
        btnAutoRestock = view.findViewById(R.id.btnAutoRestock);

        adapter = new MaterialAnalysisAdapter(analysisList, this::restockSingleItem);
        recyclerViewAnalysis.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAnalysis.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::fetchAnalysisData);

        btnAutoRestock.setOnClickListener(v -> {
            if (analysisList.isEmpty()) {
                Toast.makeText(getContext(), R.string.no_ingredients_to_restock, Toast.LENGTH_SHORT).show();
                return;
            }
            triggerAutoRestock();
        });

        requestQueue = Volley.newRequestQueue(requireContext());
        fetchAnalysisData();
    }

    private void fetchAnalysisData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String url = ApiConstants.BASE_URL + "analyze_material_consumption.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (!isAdded()) return;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

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
                    if (!isAdded()) return;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    showError(getString(R.string.network_error_prefix, error.toString()));
                });

        requestQueue.add(request);
    }

    private void parseAndDisplayAnalysis(JSONObject data) throws JSONException {
        analysisList.clear();

        JSONObject summaryObj = data.getJSONObject("summary");
        String periodObj = data.getJSONObject("period").getString("start_date") + " ~ " +
                data.getJSONObject("period").getString("end_date");

        String summaryText = getString(
            R.string.material_analysis_summary_fragment,
            periodObj,
            summaryObj.getInt("total_materials"),
            summaryObj.getInt("materials_needing_restock"),
            summaryObj.getDouble("total_restock_amount_needed")
        );
        textSummary.setText(summaryText);

        boolean needsRestock = summaryObj.getInt("materials_needing_restock") > 0;
        btnAutoRestock.setVisibility(View.VISIBLE);
        btnAutoRestock.setEnabled(needsRestock);
        btnAutoRestock.setText(needsRestock ? getString(R.string.auto_restock_now) : getString(R.string.all_stocked));

        JSONArray materials = data.getJSONArray("materials");
        for (int i = 0; i < materials.length(); i++) {
            JSONObject material = materials.getJSONObject(i);
            analysisList.add(new MaterialAnalysisItem(
                    material.getInt("mid"),
                    material.getString("mname"),
                    material.getString("unit"),
                    material.getDouble("current_qty"),
                    material.getDouble("avg_weekly_usage"),
                    material.getDouble("restock_point_120"),
                    material.getString("status"),
                    material.getDouble("restock_amount")
            ));
        }

        adapter.notifyDataSetChanged();
    }

    private void triggerAutoRestock() {
        if (analysisData == null) {
            Toast.makeText(getContext(), R.string.no_analysis_data_available, Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

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
            final JSONObject finalRequestBody = requestBody;

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (!isAdded()) return;
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject result = new JSONObject(response);
                            if (result.getBoolean("success")) {
                                Toast.makeText(getContext(), R.string.auto_restock_completed, Toast.LENGTH_SHORT).show();
                                fetchAnalysisData();
                            } else {
                                showError(getString(R.string.restock_failed_prefix, result.getString("error")));
                            }
                        } catch (JSONException e) {
                            showError(getString(R.string.json_error_prefix, e.getMessage()));
                        }
                    },
                    error -> {
                        if (!isAdded()) return;
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        showError(getString(R.string.network_error_prefix, error.toString()));
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    return finalRequestBody.toString().getBytes();
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            showError(getString(R.string.error_preparing_request_prefix, e.getMessage()));
        }
    }

    private void restockSingleItem(MaterialAnalysisItem item) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

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
            final JSONObject finalRequestBody = requestBody;

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (!isAdded()) return;
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject result = new JSONObject(response);
                            if (result.getBoolean("success")) {
                                Toast.makeText(getContext(), getString(R.string.item_restocked_successfully, item.mname), Toast.LENGTH_SHORT).show();
                                fetchAnalysisData();
                            } else {
                                showError(getString(R.string.restock_failed_prefix, result.getString("error")));
                            }
                        } catch (JSONException e) {
                            showError(getString(R.string.json_error_prefix, e.getMessage()));
                        }
                    },
                    error -> {
                        if (!isAdded()) return;
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        showError(getString(R.string.network_error_prefix, error.toString()));
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    return finalRequestBody.toString().getBytes();
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            showError(getString(R.string.error_preparing_request_prefix, e.getMessage()));
        }
    }

    @Override
    public void refreshData() {
        fetchAnalysisData();
    }

    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
