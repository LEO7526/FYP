package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.CouponAdapter;
import com.example.yummyrestaurant.api.ApiService;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.BirthdayResponse;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.CouponListResponse;
import com.example.yummyrestaurant.models.CouponPointResponse;
import com.example.yummyrestaurant.models.CouponPointsResponse;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.models.RedeemCouponResponse;
import com.example.yummyrestaurant.utils.LanguageManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.airbnb.lottie.LottieAnimationView;

import java.time.LocalDate;
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
    private Button btnHistory;

    private Button btnMyCoupons; // new
    private int currentPoints = 0; // add this field
    private View redeemSuccessOverlay;
    private LottieAnimationView redeemSuccessLottie;
    private TextView redeemSuccessText;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private String currentLanguage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);

        setupBottomFunctionBar();
        currentLanguage = LanguageManager.getCurrentLanguage(this);

        tvCouponPoints = findViewById(R.id.tvCouponPoints);
        rvCoupons = findViewById(R.id.rvCoupons);
        btnHistory = findViewById(R.id.btnHistory);
        btnMyCoupons = findViewById(R.id.btnMyCoupons);
        redeemSuccessOverlay = findViewById(R.id.redeemSuccessOverlay);
        redeemSuccessLottie = findViewById(R.id.redeemSuccessLottie);
        redeemSuccessText = findViewById(R.id.redeemSuccessText);


        // Initial login state
        try {
            customerId = Integer.parseInt(RoleManager.getUserId());
        } catch (Exception e) {
            Log.w(TAG, "Invalid userId, defaulting to 0");
            customerId = 0;
        }

        rvCoupons.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CouponAdapter(
                couponList,
                new CouponAdapter.OnRedeemClickListener() {
                    @Override
                    public void onRedeemClick(Coupon coupon) {
                        if (coupon.isBirthdayOnly()) {
                            checkBirthdayAndRedeem(coupon);   // ✅ birthday logic
                        } else {
                            redeemCoupon(coupon);             // ✅ normal logic
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        showInlineLogin(() -> refreshLoginState(), null, 0, null, null);
                    }
                },
                coupon -> {
                    // detail page navigation
                    Intent intent = new Intent(CouponActivity.this, CouponDetailActivity.class);
                    intent.putExtra("coupon_id", coupon.getCouponId());
                    intent.putExtra("current_points", currentPoints);
                    startActivity(intent);
                },
                customerId != 0
        );

        rvCoupons.setAdapter(adapter);

        fetchCoupons();

        if (customerId != 0) {
            fetchCouponPoints(customerId);
        } else {
            tvCouponPoints.setText(getString(R.string.login_to_earn_points));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLoginState();
    }

    private void refreshLoginState() {
        try {
            customerId = Integer.parseInt(RoleManager.getUserId());
        } catch (Exception e) {
            customerId = 0;
        }

        Log.d(TAG, "refreshLoginState: RoleManager.getUserId() = " + RoleManager.getUserId());
        Log.d(TAG, "refreshLoginState: Parsed customerId = " + customerId);

        if (customerId == 0) {
            Log.d(TAG, "User not logged in → hiding buttons");
            btnHistory.setVisibility(View.GONE);
            btnHistory.setOnClickListener(null);

            btnMyCoupons.setVisibility(View.GONE);
            btnMyCoupons.setOnClickListener(null);

            tvCouponPoints.setText(getString(R.string.login_to_earn_points));
            adapter.setLoggedIn(false);
        } else {
            Log.d(TAG, "User logged in (id=" + customerId + ") → showing buttons");
            btnHistory.setVisibility(View.VISIBLE);
            btnMyCoupons.setVisibility(View.VISIBLE);

            final int finalCustomerId = customerId;

            btnHistory.setOnClickListener(v -> {
                Log.d(TAG, "History button clicked → opening CouponHistoryActivity");
                Intent intent = new Intent(CouponActivity.this, CouponHistoryActivity.class);
                intent.putExtra("customer_id", finalCustomerId);
                startActivity(intent);
            });

            btnMyCoupons.setOnClickListener(v -> {
                Log.d(TAG, "My Coupons button clicked → opening MyCouponsActivity");
                Intent intent = new Intent(CouponActivity.this, MyCouponsActivity.class);
                intent.putExtra("customer_id", finalCustomerId);
                startActivity(intent);
            });

            adapter.setLoggedIn(true);
            fetchCouponPoints(customerId);
        }
    }

    private void fetchCouponPoints(int customerId) {
        Log.d(TAG, "Fetching coupon points for customerId=" + customerId);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getCouponPoints(customerId).enqueue(new Callback<CouponPointsResponse>() {
            @Override
            public void onResponse(Call<CouponPointsResponse> call, Response<CouponPointsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    int points = response.body().getCouponPoints();
                    currentPoints = points;
                    tvCouponPoints.setText(getString(R.string.points_format, points));
                    adapter.setCurrentPoints(points);
                    Log.d(TAG, "fetchCouponPoints success: points=" + points);
                } else {
                    Log.w(TAG, "fetchCouponPoints failed: code=" + response.code());
                    tvCouponPoints.setText(getString(R.string.failed_load_points));
                }
            }

            @Override
            public void onFailure(Call<CouponPointsResponse> call, Throwable t) {
                Log.e(TAG, "fetchCouponPoints onFailure", t);
                tvCouponPoints.setText(getString(R.string.error_loading_points));
            }
        });
    }

    private void fetchCoupons() {
        Log.d(TAG, "Fetching coupons...");

        CouponApiService service = RetrofitClient.getClient(this).create(CouponApiService.class);
        service.getCoupons(currentLanguage).enqueue(new Callback<CouponListResponse>() {
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
                Toast.makeText(CouponActivity.this, getString(R.string.failed_load_coupons), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redeemCoupon(Coupon coupon) {
        if (customerId == 0) {
            Toast.makeText(this, getString(R.string.please_login_to_redeem), Toast.LENGTH_SHORT).show();
            Log.w(TAG, "redeemCoupon called with customerId=0");
            return;
        }

        int requiredPoints = coupon.getPointsRequired();

        // Calculate max redeemable quantity using the Activity’s currentPoints field
        int maxRedeemable;
        if (requiredPoints > 0) {
            maxRedeemable = currentPoints / requiredPoints;
        } else {
            maxRedeemable = 1; // free coupon, only allow one
        }


        if (maxRedeemable <= 0) {
            Toast.makeText(this, getString(R.string.not_enough_points_to_redeem_coupon), Toast.LENGTH_SHORT).show();
            return;
        }

        // Build options array: "1", "2", "3", ...
        String[] options = new String[maxRedeemable];
        for (int i = 0; i < maxRedeemable; i++) {
            options[i] = String.valueOf(i + 1);
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quantity_selector, null);
        ListView listView = dialogView.findViewById(R.id.quantityListView);
        ArrayAdapter<String> adapterList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapterList);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_quantity_to_redeem))
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            int quantity = position + 1;
            Log.d(TAG, "Redeeming " + quantity + " of couponId=" + coupon.getCouponId());

            CouponApiService service = RetrofitClient.getClient(this).create(CouponApiService.class);
            service.redeemCoupon(customerId, coupon.getCouponId(), quantity)
                    .enqueue(new Callback<RedeemCouponResponse>() {
                        @Override
                        public void onResponse(Call<RedeemCouponResponse> call, Response<RedeemCouponResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                RedeemCouponResponse res = response.body();

                                if (res.isSuccess()) {
                                    Toast.makeText(CouponActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                                    showRedeemSuccessOverlay(res.getMessage());
                                    if (res.getPointsAfter() != null) {
                                        int remaining = res.getPointsAfter();
                                        currentPoints = remaining;
                                        tvCouponPoints.setText(getString(R.string.points_format, remaining));
                                        adapter.setCurrentPoints(remaining);
                                    }
                                } else {
                                    if ("BIRTHDAY_ALREADY_REDEEMED".equals(res.getErrorCode())) {
                                        Toast.makeText(CouponActivity.this,
                                                getString(R.string.birthday_coupon_redeemed_this_year),
                                                Toast.LENGTH_LONG).show();

                                        coupon.setRedeemable(false);   // mark coupon
                                        adapter.notifyDataSetChanged(); // refresh list
                                    } else {
                                        Toast.makeText(CouponActivity.this,
                                                res.getError() != null ? res.getError() : getString(R.string.redeem_failed),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(CouponActivity.this, getString(R.string.redeem_failed), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<RedeemCouponResponse> call, Throwable t) {
                            Toast.makeText(CouponActivity.this, getString(R.string.network_error_with_reason, t.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    private void showRedeemSuccessOverlay(String message) {
        if (redeemSuccessOverlay == null || redeemSuccessLottie == null) return;
        redeemSuccessText.setText(message != null && !message.isEmpty() ? message : getString(R.string.redeemed_successfully));
        redeemSuccessOverlay.setVisibility(View.VISIBLE);
        redeemSuccessLottie.setProgress(0f);
        redeemSuccessLottie.playAnimation();

        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.postDelayed(() -> redeemSuccessOverlay.setVisibility(View.GONE), 1400);
    }

    @Override
    protected void onDestroy() {
        uiHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void checkBirthdayAndRedeem(Coupon coupon) {
        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.getBirthday(customerId).enqueue(new Callback<BirthdayResponse>() {
            @Override
            public void onResponse(Call<BirthdayResponse> call, Response<BirthdayResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String birthdayStr = response.body().getCbirthday();

                    if (birthdayStr == null || birthdayStr.trim().isEmpty()) {
                        // No birthday set → direct to ProfileActivity
                        Toast.makeText(CouponActivity.this,
                                getString(R.string.please_set_birthday_to_redeem),
                                Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(CouponActivity.this, ProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return;
                    }

                    try {
                        // Expecting format "MM-DD"
                        String[] parts = birthdayStr.trim().split("-");
                        if (parts.length == 2) {
                            int birthdayMonth = Integer.parseInt(parts[0]); // "11" → 11
                            int currentMonth = java.time.LocalDate.now().getMonthValue();

                            if (birthdayMonth != currentMonth) {
                                Toast.makeText(CouponActivity.this,
                                        getString(R.string.birthday_coupon_month_only),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            // ✅ Safe to redeem now
                            redeemCoupon(coupon);
                        } else {
                            Toast.makeText(CouponActivity.this,
                                    getString(R.string.invalid_birthday_format, birthdayStr),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(CouponActivity.this,
                                getString(R.string.error_parsing_birthday, e.getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CouponActivity.this,
                            getString(R.string.failed_check_birthday),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BirthdayResponse> call, Throwable t) {
                Toast.makeText(CouponActivity.this,
                        getString(R.string.error_checking_birthday, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }



}
