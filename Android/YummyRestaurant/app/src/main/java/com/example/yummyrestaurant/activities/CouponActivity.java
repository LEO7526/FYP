package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.util.Log;
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

        // Determine login state first
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
                // Logged-in user clicked Redeem
                redeemCoupon(coupon);
            }

            @Override
            public void onLoginRequired() {
                // Guest clicked "Login to Redeem"
                showInlineLogin(() -> {
                    // After successful login, refresh state
                    try {
                        customerId = Integer.parseInt(RoleManager.getUserId());
                    } catch (Exception e) {
                        customerId = 0;
                    }

                    adapter.setLoggedIn(customerId != 0);

                    if (customerId != 0) {
                        // Refresh points after login
                        fetchCouponPoints(customerId);

                        // Optionally, you could auto-trigger the pending redeem action here
                        // e.g., redeemCoupon(pendingCoupon);
                    }
                }, null, 0, null, null);
            }
        }, customerId != 0);
        rvCoupons.setAdapter(adapter);

        // Always show coupons
        fetchCoupons();

        // Only fetch points if logged in
        if (customerId != 0) {
            fetchCouponPoints(customerId);
        } else {
            tvCouponPoints.setText("Login to earn and redeem points");
        }
    }

    private void fetchCouponPoints(int customerId) {
        CouponApiService service = RetrofitClient.getClient(this).create(CouponApiService.class);
        service.getCouponPoints(customerId).enqueue(new Callback<CouponPointResponse>() {
            @Override
            public void onResponse(Call<CouponPointResponse> call, Response<CouponPointResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    int points = response.body().getPoints();
                    tvCouponPoints.setText("Points: " + points);
                    adapter.setCurrentPoints(points); // update adapter
                } else {
                    tvCouponPoints.setText("Points: 0");
                    adapter.setCurrentPoints(0);
                }
            }

            @Override
            public void onFailure(Call<CouponPointResponse> call, Throwable t) {
                tvCouponPoints.setText("Points: 0");
                adapter.setCurrentPoints(0);
            }
        });
    }

    private void fetchCoupons() {
        CouponApiService service = RetrofitClient.getClient(this).create(CouponApiService.class);
        service.getCoupons().enqueue(new Callback<CouponListResponse>() {
            @Override
            public void onResponse(Call<CouponListResponse> call, Response<CouponListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    couponList.clear();
                    couponList.addAll(response.body().getCoupons());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CouponListResponse> call, Throwable t) {
                Toast.makeText(CouponActivity.this, "Failed to load coupons", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "fetchCoupons failed", t);
            }
        });
    }

    private void redeemCoupon(Coupon coupon) {
        if (customerId == 0) {
            Toast.makeText(this, "Please login to redeem", Toast.LENGTH_SHORT).show();
            return;
        }

        CouponApiService service = RetrofitClient.getClient(this).create(CouponApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("cid", customerId);
        body.put("coupon_id", coupon.getCoupon_id());

        service.redeemCoupon(body).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse res = response.body();
                    if (res.isSuccess()) {
                        // ✅ Success: show message
                        Toast.makeText(CouponActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();

                        // ✅ Update points in UI
                        if (res.getRemaining_points() != null) {
                            int remaining = res.getRemaining_points();
                            tvCouponPoints.setText("Points: " + remaining);

                            // ✅ Update adapter so other coupons dim/enable correctly
                            adapter.setCurrentPoints(remaining);
                        }
                    } else {
                        // ❌ Backend returned error
                        Toast.makeText(CouponActivity.this, res.getError(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CouponActivity.this, "Redeem failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(CouponActivity.this, "Redeem failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}