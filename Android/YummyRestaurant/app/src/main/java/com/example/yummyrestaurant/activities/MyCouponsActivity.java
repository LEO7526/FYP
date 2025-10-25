package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.MyCouponAdapter;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.models.MyCouponListResponse;
import com.example.yummyrestaurant.utils.RoleManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCouponsActivity extends BaseCustomerActivity {
    private RecyclerView rvMyCoupons;
    private MyCouponAdapter adapter;
    private List<Coupon> myCoupons = new ArrayList<>();
    private int customerId;

    private boolean fromCart;   // 👈 new flag


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_coupons);

        setupBottomFunctionBar();

        rvMyCoupons = findViewById(R.id.rvMyCoupons);
        rvMyCoupons.setLayoutManager(new LinearLayoutManager(this));

        // 👇 read flag
        fromCart = getIntent().getBooleanExtra("fromCart", false);

        adapter = new MyCouponAdapter(myCoupons, (coupon, position) -> {
            if (!fromCart) {
                Toast.makeText(this, "Coupons can only be used during checkout", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable handled in adapter
            CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
            api.useCoupon(customerId, coupon.getCoupon_id()).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Success → update UI
                        Intent result = new Intent();
                        result.putExtra("selectedCouponId", coupon.getCoupon_id());
                        result.putExtra("discountAmount", coupon.getDiscount_amount());
                        result.putExtra("couponType", coupon.getType());
                        result.putExtra("itemCategory", coupon.getItemCategory());
                        setResult(RESULT_OK, result);

                        adapter.decrementCouponQuantity(position);
                        finish();
                    } else {
                        // Failure → re‑enable button
                        Toast.makeText(MyCouponsActivity.this, "Failed to apply coupon", Toast.LENGTH_SHORT).show();
                        RecyclerView.ViewHolder vh = rvMyCoupons.findViewHolderForAdapterPosition(position);
                        if (vh != null) {
                            vh.itemView.findViewById(R.id.btnUseCoupon).setEnabled(true);
                        }
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    Toast.makeText(MyCouponsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    RecyclerView.ViewHolder vh = rvMyCoupons.findViewHolderForAdapterPosition(position);
                    if (vh != null) {
                        vh.itemView.findViewById(R.id.btnUseCoupon).setEnabled(true);
                    }
                }
            });
        }, fromCart); // 👈 pass flag into adapter

        rvMyCoupons.setAdapter(adapter);

        customerId = Integer.parseInt(RoleManager.getUserId());
        fetchMyCoupons(customerId);
    }

    private void fetchMyCoupons(int customerId) {
        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.getMyCoupons(customerId).enqueue(new Callback<MyCouponListResponse>() {
            @Override
            public void onResponse(Call<MyCouponListResponse> call, Response<MyCouponListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    myCoupons.clear();
                    myCoupons.addAll(response.body().getCoupons());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MyCouponsActivity.this, "No coupons found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyCouponListResponse> call, Throwable t) {
                Toast.makeText(MyCouponsActivity.this, "Failed to load coupons", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}