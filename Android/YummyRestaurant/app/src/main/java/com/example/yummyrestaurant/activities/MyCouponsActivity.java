package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;   // 👈 import Log
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private static final String TAG = "MyCouponsActivity"; // 👈 tag for logs

    private RecyclerView rvMyCoupons;
    private MyCouponAdapter adapter;
    private List<Coupon> myCoupons = new ArrayList<>();
    private int customerId;
    private boolean fromCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_coupons);

        setupBottomFunctionBar();

        rvMyCoupons = findViewById(R.id.rvMyCoupons);
        rvMyCoupons.setLayoutManager(new LinearLayoutManager(this));

        fromCart = getIntent().getBooleanExtra("fromCart", false);
        Log.d(TAG, "onCreate: fromCart=" + fromCart);

        adapter = new MyCouponAdapter(myCoupons, (coupon, position) -> {
            Log.d(TAG, "Coupon clicked: id=" + coupon.getCouponId() + ", pos=" + position);

            if (!fromCart) {
                Toast.makeText(this, "Coupons can only be used during checkout", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Attempted to use coupon outside checkout");
                return;
            }

            showQuantityPickerAndUse(coupon, position);

        }, fromCart);

        rvMyCoupons.setAdapter(adapter);

        try {
            customerId = Integer.parseInt(RoleManager.getUserId());
        } catch (Exception e) {
            Log.e(TAG, "Invalid userId from RoleManager", e);
            customerId = 0;
        }

        Log.d(TAG, "onCreate: customerId=" + customerId);
        fetchMyCoupons(customerId);
    }

    private void fetchMyCoupons(int customerId) {
        Log.d(TAG, "Fetching coupons for customerId=" + customerId);

        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.getMyCoupons(customerId, "en").enqueue(new Callback<MyCouponListResponse>() {
            @Override
            public void onResponse(Call<MyCouponListResponse> call, Response<MyCouponListResponse> response) {
                Log.d(TAG, "fetchMyCoupons onResponse: code=" + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    myCoupons.clear();
                    myCoupons.addAll(response.body().getCoupons());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Coupons loaded: count=" + myCoupons.size());
                } else {
                    Toast.makeText(MyCouponsActivity.this, "No coupons found", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "fetchMyCoupons failed: body=" + response.body());
                }
            }

            @Override
            public void onFailure(Call<MyCouponListResponse> call, Throwable t) {
                Toast.makeText(MyCouponsActivity.this, "Failed to load coupons", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "fetchMyCoupons onFailure", t);
            }
        });
    }

    private void showQuantityPickerAndUse(Coupon coupon, int position) {
        int maxUsable = coupon.getQuantity();
        Log.d(TAG, "showQuantityPickerAndUse: couponId=" + coupon.getCouponId() + ", maxUsable=" + maxUsable);

        if (maxUsable <= 0) {
            Toast.makeText(this, "No coupons available to use", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Coupon quantity is 0 for id=" + coupon.getCouponId());
            return;
        }

        String[] options = new String[maxUsable];
        for (int i = 0; i < maxUsable; i++) {
            options[i] = String.valueOf(i + 1);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select quantity to use")
                .setItems(options, (dialog, which) -> {
                    int quantity = which + 1;
                    Log.d(TAG, "User selected quantity=" + quantity + " for couponId=" + coupon.getCouponId());
                    useCoupon(coupon, position, quantity);
                })
                .show();
    }

    private void useCoupon(Coupon coupon, int position, int quantity) {
        Log.d(TAG, "useCoupon: couponId=" + coupon.getCouponId() + ", quantity=" + quantity);

        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.useCoupon(customerId, coupon.getCouponId(), quantity).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                Log.d(TAG, "useCoupon onResponse: code=" + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.i(TAG, "Coupon applied successfully: id=" + coupon.getCouponId());

                    Intent result = new Intent();
                    result.putExtra("selectedCouponId", coupon.getCouponId());
                    result.putExtra("discountAmount", coupon.getDiscountAmount());
                    result.putExtra("couponType", coupon.getType());
                    result.putExtra("itemCategory", coupon.getItemCategory());
                    setResult(RESULT_OK, result);

                    adapter.decrementCouponQuantity(position, quantity);
                    finish();
                } else {
                    Toast.makeText(MyCouponsActivity.this, "Failed to apply coupon", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "useCoupon failed: body=" + response.body());
                    reEnableButton(position);
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(MyCouponsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "useCoupon onFailure", t);
                reEnableButton(position);
            }
        });
    }

    private void reEnableButton(int position) {
        Log.d(TAG, "Re-enabling button at position=" + position);
        RecyclerView.ViewHolder vh = rvMyCoupons.findViewHolderForAdapterPosition(position);
        if (vh != null) {
            vh.itemView.findViewById(R.id.btnUseCoupon).setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed → RESULT_CANCELED");
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
