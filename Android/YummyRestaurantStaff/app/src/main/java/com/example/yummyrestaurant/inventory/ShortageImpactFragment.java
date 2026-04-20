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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConstants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShortageImpactFragment extends Fragment implements RefreshableTab {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textSummary;
    private TextView textEmpty;
    private ShortageImpactAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shortage_impact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshImpact);
        progressBar = view.findViewById(R.id.progressBarImpact);
        textSummary = view.findViewById(R.id.textImpactSummaryCard);
        textEmpty = view.findViewById(R.id.textImpactEmpty);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewImpact);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShortageImpactAdapter(this::showAffectedDishDialog);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::fetchImpactAnalysis);
        fetchImpactAnalysis();
    }

    private void fetchImpactAnalysis() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        String url = ApiConstants.baseUrl() + "get_weekly_material_consumption.php";
        Volley.newRequestQueue(requireContext()).add(new StringRequest(Request.Method.GET, url,
                response -> {
                    if (!isAdded()) return;
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    parseImpactResponse(response);
                },
                error -> {
                    if (!isAdded()) return;
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(getContext(), R.string.failed_load_weekly_consumption, Toast.LENGTH_SHORT).show();
                }));
    }

    private void parseImpactResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            if (!json.optBoolean("success", false)) {
                Toast.makeText(getContext(), json.optString("message", getString(R.string.load_failed)), Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject period = json.getJSONObject("period");
            JSONObject summary = json.getJSONObject("summary");
            String summaryText = String.format(
                    Locale.getDefault(),
                    getString(R.string.shortage_impact_summary_format),
                    period.optString("start_date", "-"),
                    period.optString("end_date", "-"),
                    summary.optInt("ingredient_count", 0),
                    summary.optDouble("total_weekly_consumed", 0),
                    summary.optInt("total_activity_records", 0),
                    summary.optInt("deduction_count", 0),
                    summary.optInt("reorder_count", 0),
                    summary.optInt("forecast_count", 0),
                    summary.optString("top_ingredient_name", getString(R.string.no_data))
            );
            textSummary.setText(summaryText);

            JSONArray data = json.getJSONArray("data");
            List<ShortageImpactItem> items = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject itemObj = data.getJSONObject(i);
                ShortageImpactItem item = new ShortageImpactItem(
                        itemObj.getInt("mid"),
                        itemObj.getString("ingredient_name"),
                        itemObj.getString("unit"),
                        itemObj.getDouble("current_qty"),
                        itemObj.getDouble("reorder_level"),
                        itemObj.getDouble("weekly_consumed"),
                        itemObj.getDouble("avg_daily_consumed"),
                        itemObj.optInt("recent_activity_count", 0),
                        itemObj.optString("latest_log_type", ""),
                        itemObj.optString("latest_log_date", ""),
                        itemObj.optString("latest_log_details", "")
                );

                JSONArray recentLogs = itemObj.optJSONArray("recent_logs");
                if (recentLogs != null) {
                    for (int j = 0; j < recentLogs.length(); j++) {
                        JSONObject logObj = recentLogs.getJSONObject(j);
                        item.recentLogs.add(new ShortageImpactItem.ActivityLog(
                                logObj.getInt("log_id"),
                                logObj.getString("log_date"),
                                logObj.getString("log_type"),
                                logObj.getString("details")
                        ));
                    }
                }
                items.add(item);
            }

            adapter.setItems(items);
            textEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.failed_parse_weekly_consumption, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAffectedDishDialog(ShortageImpactItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.getDefault(),
                getString(R.string.shortage_impact_detail_header),
                item.weeklyConsumed,
                item.unit,
                item.avgDailyConsumed,
                item.unit,
                item.currentQty,
                item.unit,
                item.reorderLevel,
                item.unit));

        if (item.recentLogs.isEmpty()) {
            builder.append(getString(R.string.no_manual_activity_log_7_days));
        } else {
            builder.append(getString(R.string.recent_records)).append("\n");
            for (ShortageImpactItem.ActivityLog log : item.recentLogs) {
                builder.append("• ")
                        .append(log.logDate)
                        .append(" [")
                        .append(log.logType)
                        .append("] ")
                        .append(log.details)
                        .append("\n");
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.ingredient_records_title, item.ingredientName))
                .setMessage(builder.toString().trim())
            .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    public void refreshData() {
        fetchImpactAnalysis();
    }
}