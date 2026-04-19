package com.example.yummyrestaurant.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.yummyrestaurant.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestockFragment extends Fragment implements RefreshableTab {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ApiService apiService;
    private RestockAdapter adapter;
    private EditText etDays;
    private TextView tvSummary;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient().create(ApiService.class);
        etDays = view.findViewById(R.id.etRestockDays);
        tvSummary = view.findViewById(R.id.tvRestockSummary);
        tvEmpty = view.findViewById(R.id.tvRestockEmpty);
        MaterialButton btnGenerate = view.findViewById(R.id.btnGenerateRestock);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_restock);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RestockAdapter(new RestockAdapter.OnDecisionListener() {
            @Override
            public void onApprove(RestockRecommendation item) {
                showDecisionDialog(item, "approved");
            }

            @Override
            public void onReject(RestockRecommendation item) {
                showDecisionDialog(item, "rejected");
            }
        });
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_restock);
        swipeRefreshLayout.setOnRefreshListener(() -> fetchRecommendations(false));

        btnGenerate.setOnClickListener(v -> fetchRecommendations(true));

        MaterialButton btnAnalyze = view.findViewById(R.id.btnAnalyze);
        btnAnalyze.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MaterialAnalysisActivity.class);
            startActivity(intent);
        });

        fetchRecommendations(true);
    }

    private int getDays() {
        try {
            int value = Integer.parseInt(etDays.getText().toString().trim());
            return Math.max(1, Math.min(value, 30));
        } catch (Exception e) {
            return 7;
        }
    }

    private void fetchRecommendations(boolean regenerate) {
        fetchRecommendations(regenerate, 0);
    }

    private void fetchRecommendations(boolean regenerate, int forceDemo) {
        int days = getDays();
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);

        apiService.getRestockRecommendations(days, regenerate ? 1 : 0, forceDemo)
                .enqueue(new Callback<ApiResponse<List<RestockRecommendation>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<RestockRecommendation>>> call,
                                           @NonNull Response<ApiResponse<List<RestockRecommendation>>> response) {
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                        ApiResponse<List<RestockRecommendation>> body = response.body();
                        if (response.isSuccessful() && body != null && body.success) {
                            adapter.setItems(body.data);
                            int count = body.data == null ? 0 : body.data.size();
                            int generatedCount = body.generated_count;

                            if (regenerate && count == 0 && forceDemo == 0) {
                                Toast.makeText(getContext(), R.string.no_real_suggestions_loading_demo, Toast.LENGTH_SHORT).show();
                                fetchRecommendations(true, 1);
                                return;
                            }

                            tvSummary.setText(getString(R.string.pending_predict_days, count, days));
                            if (regenerate) {
                                Toast.makeText(getContext(), getString(R.string.generated_suggestions_count, generatedCount), Toast.LENGTH_SHORT).show();
                            }
                            if (tvEmpty != null) {
                                tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                            }
                        } else {
                            if (tvEmpty != null) {
                                tvEmpty.setVisibility(View.VISIBLE);
                            }
                            Toast.makeText(getContext(), R.string.failed_load_restock_suggestions, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<RestockRecommendation>>> call, @NonNull Throwable t) {
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), getString(R.string.network_error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDecisionDialog(RestockRecommendation item, String decision) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_restock_decision, null, false);
        EditText etStaff = dialogView.findViewById(R.id.etDecisionStaffId);
        EditText etNote = dialogView.findViewById(R.id.etDecisionNote);
        com.google.android.material.checkbox.MaterialCheckBox cbApply = dialogView.findViewById(R.id.cbApplyStockNow);

        if ("rejected".equals(decision)) {
            cbApply.setChecked(false);
            cbApply.setEnabled(false);
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("approved".equals(decision) ? getString(R.string.approve_restock) : getString(R.string.reject_restock))
                .setMessage(item.materialName + " | Suggested: " + (int) item.suggestedQty + " " + item.unit)
                .setView(dialogView)
            .setPositiveButton(R.string.confirm, (d, w) -> {
                    int staffId = 1;
                    try {
                        String s = etStaff.getText().toString().trim();
                        if (!s.isEmpty()) {
                            staffId = Integer.parseInt(s);
                        }
                    } catch (Exception ignored) {
                    }

                    String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();
                    boolean applyNow = cbApply.isChecked();

                    RestockDecisionRequest req = new RestockDecisionRequest(
                            item.recommendationId,
                            decision,
                            staffId,
                            note,
                            applyNow
                    );
                    sendDecision(req);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void sendDecision(RestockDecisionRequest req) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        apiService.decideRestockRecommendation(req).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                ApiResponse<Object> body = response.body();
                if (response.isSuccessful() && body != null && body.success) {
                    Toast.makeText(getContext(), body.message != null ? body.message : getString(R.string.decision_submitted), Toast.LENGTH_SHORT).show();
                    fetchRecommendations(false);
                } else {
                    String message = body != null && body.message != null ? body.message : getString(R.string.failed_submit_decision);
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), getString(R.string.network_error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchRecommendations(false);
    }

    @Override
    public void refreshData() {
        fetchRecommendations(false);
    }
}
