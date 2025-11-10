package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.CouponDetailResponse;
import com.example.yummyrestaurant.models.RedeemCouponResponse;
import com.example.yummyrestaurant.utils.RoleManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CouponDetailActivity extends BaseCustomerActivity {

    private static final String TAG = "CouponDetailActivity";

    private TextView tvTitle, tvTerms, tvRequiredPoints, tvRemainingPoints;
    private Button btnRedeem;

    private int couponId;
    private int customerId;
    private int requiredPoints = 0;   // from API
    private int currentPoints = 0;    // passed from intent or fetched separately

    private Coupon currentCoupon;     // hold the full coupon object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_detail);

        setupBottomFunctionBar();

        tvTitle = findViewById(R.id.tvTitle);
        tvTerms = findViewById(R.id.tvTerms);
        tvRequiredPoints = findViewById(R.id.tvRequiredPoints);
        tvRemainingPoints = findViewById(R.id.tvRemainingPoints);
        btnRedeem = findViewById(R.id.btnRedeem);

        couponId = getIntent().getIntExtra("coupon_id", 0);
        currentPoints = getIntent().getIntExtra("current_points", 0);

        if (couponId == 0) {
            Toast.makeText(this, "Invalid coupon selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            customerId = Integer.parseInt(RoleManager.getUserId());
        } catch (Exception e) {
            Log.w(TAG, "Invalid userId, defaulting to guest", e);
            customerId = 0;
        }

        fetchCouponDetails(couponId);

        btnRedeem.setOnClickListener(v -> {
            if (customerId == 0) {
                Toast.makeText(this, "Please login to redeem", Toast.LENGTH_SHORT).show();
            } else if (btnRedeem.isEnabled()) {
                showQuantityPickerAndRedeem();
            }
        });

    }

    private void fetchCouponDetails(int couponId) {
        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.getCouponDetail(couponId, "en").enqueue(new Callback<CouponDetailResponse>() {
            @Override
            public void onResponse(Call<CouponDetailResponse> call, Response<CouponDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentCoupon = response.body().getCoupon();
                    updateCouponUI(currentCoupon);
                } else {
                    Log.w(TAG, "fetchCouponDetails failed: " + response.code());
                    tvTerms.setText("Failed to load terms");
                }
            }

            @Override
            public void onFailure(Call<CouponDetailResponse> call, Throwable t) {
                Log.e(TAG, "fetchCouponDetails onFailure", t);
                tvTerms.setText("Error loading terms");
            }
        });
    }

    private void updateCouponUI(Coupon coupon) {
        if (coupon != null) {
            tvTitle.setText(coupon.getTitle());
            requiredPoints = coupon.getPointsRequired();
            tvRequiredPoints.setText("Points required: " + requiredPoints);

            // Show current points passed from intent
            tvRemainingPoints.setText("Remaining points: " + currentPoints);

            // 🚨 Disable redeem button if not enough points
            if (requiredPoints > 0 && currentPoints < requiredPoints) {
                btnRedeem.setEnabled(false);
                btnRedeem.setAlpha(0.5f); // optional visual cue
                Toast.makeText(this, "Not enough points to redeem this coupon", Toast.LENGTH_SHORT).show();
            } else {
                btnRedeem.setEnabled(true);
                btnRedeem.setAlpha(1f);
            }

            if (coupon.getTerms() != null && !coupon.getTerms().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String term : coupon.getTerms()) {
                    sb.append("• ").append(term).append("\n");
                }
                tvTerms.setText(sb.toString());
            } else {
                tvTerms.setText("No terms available");
            }
        }
    }



    private void showQuantityPickerAndRedeem() {
        int maxRedeemable = (requiredPoints > 0)
                ? (currentPoints / requiredPoints)
                : 5; // fallback if free coupon

        if (maxRedeemable <= 0) {
            Toast.makeText(this, "Not enough points to redeem", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = new String[maxRedeemable];
        for (int i = 0; i < maxRedeemable; i++) {
            options[i] = String.valueOf(i + 1);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select quantity to redeem")
                .setItems(options, (dialog, which) -> {
                    int quantity = which + 1;
                    redeemCoupon(couponId, customerId, quantity);
                })
                .show();
    }

    private void redeemCoupon(int couponId, int customerId, int quantity) {
        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.redeemCoupon(customerId, couponId, quantity).enqueue(new Callback<RedeemCouponResponse>() {
            @Override
            public void onResponse(Call<RedeemCouponResponse> call, Response<RedeemCouponResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RedeemCouponResponse res = response.body();
                    Log.d(TAG, "redeemCoupon response: " + res.getMessage());

                    if (res.isSuccess()) {
                        Toast.makeText(CouponDetailActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        if (res.getPointsAfter() != null) {
                            tvRemainingPoints.setText("Remaining points: " + res.getPointsAfter());
                        }
                    } else {
                        Toast.makeText(CouponDetailActivity.this, res.getError(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "redeemCoupon failed: " + response.code());
                    Toast.makeText(CouponDetailActivity.this, "Redeem failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RedeemCouponResponse> call, Throwable t) {
                Log.e(TAG, "redeemCoupon onFailure", t);
                Toast.makeText(CouponDetailActivity.this, "Redeem failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}