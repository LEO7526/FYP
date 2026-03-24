package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.CouponHistoryAdapter;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CouponHistoryItem;
import com.example.yummyrestaurant.models.CouponHistoryResponse;
import com.example.yummyrestaurant.utils.LanguageManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CouponHistoryActivity extends BaseCustomerActivity {

    private static final String TAG = "CouponHistoryActivity";

    private RecyclerView recyclerView;
    private CouponHistoryAdapter adapter;
    private final List<CouponHistoryItem> historyList = new ArrayList<>();
    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_history);

        setupBottomFunctionBar();
        currentLanguage = LanguageManager.getCurrentLanguage(this);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CouponHistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        // ✅ Get customerId from Intent
        int customerId = getIntent().getIntExtra("customer_id", 0);
        Log.d(TAG, "onCreate: Received customerId = " + customerId);

        if (customerId != 0) {
            loadHistory(customerId);
        } else {
            Log.w(TAG, "onCreate: No customerId provided, user not logged in");
            Toast.makeText(this, getString(R.string.please_login_view_history), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHistory(int customerId) {
        Log.d(TAG, "loadHistory: Fetching history for customerId=" + customerId);

        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.getCouponHistory(customerId, currentLanguage).enqueue(new Callback<CouponHistoryResponse>() {
            @Override
            public void onResponse(Call<CouponHistoryResponse> call, Response<CouponHistoryResponse> response) {
                Log.d(TAG, "onResponse: HTTP code=" + response.code());

                if (response.isSuccessful()) {
                    CouponHistoryResponse body = response.body();

                    if (body != null) {
                        Log.d(TAG, "onResponse: success=" + body.isSuccess()
                                + ", historyCount=" + (body.getHistory() != null ? body.getHistory().size() : 0));

                        if (body.isSuccess() && body.getHistory() != null) {
                            historyList.clear();
                            historyList.addAll(body.getHistory());
                            adapter.notifyDataSetChanged();

                            // 🔎 Detailed logging of each history item
                            for (int i = 0; i < body.getHistory().size(); i++) {
                                CouponHistoryItem item = body.getHistory().get(i);
                                Log.d(TAG, "History[" + i + "]: "
                                        + "delta=" + item.getDelta()
                                        + ", resulting_points=" + item.getResulting_points()
                                        + ", action=" + item.getAction()
                                        + ", note=" + item.getNote()
                                        + ", created_at=" + item.getCreated_at()
                                        + ", coupon_title=" + item.getCouponTitle());
                            }

                            Log.d(TAG, "onResponse: History list updated, size=" + historyList.size());
                        } else {
                            Log.w(TAG, "onResponse: API returned success=false or empty history");
                            Toast.makeText(CouponHistoryActivity.this,
                                    getString(R.string.no_history_found_for_customer), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "onResponse: Body is null despite success");
                        Toast.makeText(CouponHistoryActivity.this,
                                getString(R.string.unexpected_empty_response), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 🔎 Log raw error body for debugging
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "onResponse: Response unsuccessful, errorBody=" + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: Failed to read errorBody", e);
                    }

                    Toast.makeText(CouponHistoryActivity.this,
                            getString(R.string.failed_load_history_http, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CouponHistoryResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: Error fetching history", t);
                Toast.makeText(CouponHistoryActivity.this, getString(R.string.network_error_with_reason, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}