package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

    private TextView tvCouponPoints;
    private RecyclerView rvCoupons;
    private CouponAdapter adapter;
    private List<Coupon> couponList = new ArrayList<>();
    private int customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);

        setupBottomFunctionBar();

        tvCouponPoints = findViewById(R.id.tvCouponPoints);
        rvCoupons = findViewById(R.id.rvCoupons);

        rvCoupons.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CouponAdapter(couponList, this::redeemCoupon, customerId != 0);
        rvCoupons.setAdapter(adapter);

        try {
            customerId = Integer.parseInt(RoleManager.getUserId());
        } catch (Exception e) {
            customerId = 0;
        }

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
        CouponApiService service = RetrofitClient.getClient().create(CouponApiService.class);
        service.getCouponPoints(customerId).enqueue(new Callback<CouponPointResponse>() {
            @Override
            public void onResponse(Call<CouponPointResponse> call, Response<CouponPointResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    tvCouponPoints.setText("Points: " + response.body().getPoints());
                }
            }
            @Override
            public void onFailure(Call<CouponPointResponse> call, Throwable t) {
                tvCouponPoints.setText("Points: 0");
            }
        });
    }

    private void fetchCoupons() {
        CouponApiService service = RetrofitClient.getClient().create(CouponApiService.class);
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
            }
        });
    }

    private void redeemCoupon(Coupon coupon) {
        CouponApiService service = RetrofitClient.getClient().create(CouponApiService.class);
        Map<String,Object> body = new HashMap<>();
        body.put("cid", customerId);
        body.put("coupon_id", coupon.getCoupon_id());

        service.redeemCoupon(body).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse res = response.body();
                    if (res.isSuccess()) {
                        Toast.makeText(CouponActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        if (res.getRemaining_points() != null) {
                            tvCouponPoints.setText("Points: " + res.getRemaining_points());
                        }
                    } else {
                        Toast.makeText(CouponActivity.this, res.getError(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(CouponActivity.this, "Redeem failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}