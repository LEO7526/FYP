package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.CouponAdapter;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.CouponListResponse;
import com.example.yummyrestaurant.models.CouponPointResponse;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.utils.RoleManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CouponActivity extends BaseCustomerActivity {

    private static final String TAG = "CouponActivity";

    private TextView tvCouponPoints;
    private RecyclerView rvCoupons;
    private CouponAdapter adapter;
    private final List<Coupon> couponList = new ArrayList<>();
    private int customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);

        setupBottomFunctionBar();

        tvCouponPoints = findViewById(R.id.tvCouponPoints);
        rvCoupons = findViewById(R.id.rvCoupons);

        try {
            customerId = Integer.parseInt(RoleManager.getUserId());
        } catch (Exception e) {
            Log.w(TAG, "Invalid userId, defaulting to 0");
            customerId = 0;
        }

        rvCoupons.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CouponAdapter(couponList, new CouponAdapter.OnRedeemClickListener() {
            @Override
            public void onRedeemClick(Coupon coupon) {
                redeemCoupon(coupon);
            }

            @Override
            public void onLoginRequired() {
                showInlineLogin(() -> {
                    try {
                        customerId = Integer.parseInt(RoleManager.getUserId());
                    } catch (Exception e) {
                        customerId = 0;
                    }
                    adapter.setLoggedIn(customerId != 0);
                    if (customerId != 0) {
                        fetchCouponPoints(customerId);
                    }
                }, null, 0, null, null);
            }
        }, customerId != 0);
        rvCoupons.setAdapter(adapter);

        fetchCoupons();

        if (customerId != 0) {
            fetchCouponPoints(customerId);
        } else {
            tvCouponPoints.setText("Login to earn and redeem points");
        }
    }

    private void fetchCouponPoints(int customerId) {
        Log.d(TAG, "Fetching coupon points for customerId=" + customerId);

        CouponApiService service = RetrofitClient.getClient(this).create(CouponApiService.class);
        service.getCouponPoints(customerId).enqueue(new Callback<CouponPointResponse>() {
            @Override
            public void onResponse(Call<CouponPointResponse> call, Response<CouponPointResponse> response) {
                Log.d(TAG, "fetchCouponPoints onResponse: code=" + response.code());

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    int points = response.body().getPoints();
                    Log.d(TAG, "fetchCouponPoints success: points=" + points);
                    tvCouponPoints.setText("Points: " + points);
                    adapter.setCurrentPoints(points);
                } else {
                    Log.w(TAG, "fetchCouponPoints failed: body=" + response.body());
                    tvCouponPoints.setText("Points: 0");
                    adapter.setCurrentPoints(0);
                }
            }

            @Override
            public void onFailure(Call<CouponPointResponse> call, Throwable t) {
                Log.e(TAG, "fetchCouponPoints onFailure", t);
                tvCouponPoints.setText("Points: 0");
                adapter.setCurrentPoints(0);
            }
        });
    }

    private void fetchCoupons() {
        Log.d(TAG, "Fetching coupons...");

        CouponApiService service = RetrofitClient.getClient(this).create(CouponApiService.class);
        service.getCoupons().enqueue(new Callback<CouponListResponse>() {
            @Override
            public void onResponse(Call<CouponListResponse> call, Response<CouponListResponse> response) {
                Log.d(TAG, "fetchCoupons onResponse: code=" + response.code());

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "fetchCoupons success: count=" + response.body().getCoupons().size());
                    couponList.clear();
                    couponList.addAll(response.body().getCoupons());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.w(TAG, "fetchCoupons failed: body=" + response.body());
                }
            }

            @Override
            public void onFailure(Call<CouponListResponse> call, Throwable t) {
                Log.e(TAG, "fetchCoupons onFailure", t);
                Toast.makeText(CouponActivity.this, "Failed to load coupons", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redeemCoupon(Coupon coupon) {
        if (customerId == 0) {
            Toast.makeText(this, "Please login to redeem", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "redeemCoupon called with customerId=0");
            return;
        }

        Log.d(TAG, "Redeeming coupon: id=" + coupon.getCoupon_id() + " for customerId=" + customerId);

        CouponApiService service = RetrofitClient.getClient(this).create(CouponApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("cid", customerId);
        body.put("coupon_id", coupon.getCoupon_id());

        service.redeemCoupon(body).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                Log.d(TAG, "redeemCoupon onResponse: code=" + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse res = response.body();
                    Log.d(TAG, "redeemCoupon response: success=" + res.isSuccess() +
                            ", message=" + res.getMessage() +
                            ", error=" + res.getError() +
                            ", remaining_points=" + res.getRemaining_points());

                    if (res.isSuccess()) {
                        Toast.makeText(CouponActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();

                        if (res.getRemaining_points() != null) {
                            int remaining = res.getRemaining_points();
                            tvCouponPoints.setText("Points: " + remaining);
                            adapter.setCurrentPoints(remaining);
                        }
                    } else {
                        Toast.makeText(CouponActivity.this, res.getError(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "redeemCoupon failed: body=" + response.body());
                    Toast.makeText(CouponActivity.this, "Redeem failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.e(TAG, "redeemCoupon onFailure", t);
                Toast.makeText(CouponActivity.this, "Redeem failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Button btnHistory = findViewById(R.id.btnHistory);

        int customerId;
        try {
            customerId = Integer.parseInt(RoleManager.getUserId());
        } catch (Exception e) {
            Log.w(TAG, "onResume: Failed to parse userId from RoleManager", e);
            customerId = 0;
        }

        Log.d(TAG, "onResume: RoleManager.getUserId() = " + RoleManager.getUserId());
        Log.d(TAG, "onResume: Parsed customerId = " + customerId);

        if (customerId == 0) {
            Log.d(TAG, "onResume: User not logged in → hiding History button");
            btnHistory.setVisibility(View.GONE);
            btnHistory.setOnClickListener(null);
        } else {
            Log.d(TAG, "onResume: User logged in (id=" + customerId + ") → showing History button");
            btnHistory.setVisibility(View.VISIBLE);

            final int finalCustomerId = customerId;
            btnHistory.setOnClickListener(v -> {
                Log.d(TAG, "History button clicked → opening CouponHistoryActivity with customerId=" + finalCustomerId);
                Intent intent = new Intent(CouponActivity.this, CouponHistoryActivity.class);
                intent.putExtra("customer_id", finalCustomerId);
                startActivity(intent);
            });
        }
    }
}