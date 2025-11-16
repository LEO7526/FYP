package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.MyCouponAdapter;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.models.MyCouponListResponse;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.example.yummyrestaurant.utils.CouponValidator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCouponsActivity extends BaseCustomerActivity {
    private static final String TAG = "MyCouponsActivity";

    private RecyclerView rvMyCoupons;
    private MyCouponAdapter adapter;
    private List<Coupon> myCoupons = new ArrayList<>();
    private int customerId;
    private boolean fromCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "=== Entering MyCouponsActivity ===");
        setContentView(R.layout.activity_my_coupons);
        setupBottomFunctionBar();

        rvMyCoupons = findViewById(R.id.rvMyCoupons);
        rvMyCoupons.setLayoutManager(new LinearLayoutManager(this));

        fromCart = getIntent().getBooleanExtra("fromCart", false);
        int intentCid = getIntent().getIntExtra("customer_id", Integer.MIN_VALUE);

        if (fromCart) {
            Log.i(TAG, "Activity launched from CartActivity");

            ArrayList<Integer> menuItemIds = getIntent().getIntegerArrayListExtra("menu_item_ids");
            if (menuItemIds != null) {
                Log.d(TAG, "Received menu_item_ids: " + menuItemIds);
            }

        } else {
            Log.i(TAG, "Activity launched directly (not from cart)");
        }

        if (intentCid != Integer.MIN_VALUE) {
            customerId = intentCid;
            Log.i(TAG, "Using customerId from Intent: " + customerId);
        } else {
            try {
                customerId = Integer.parseInt(RoleManager.getUserId());
                Log.i(TAG, "Using customerId from RoleManager: " + customerId);
            } catch (Exception e) {
                Log.e(TAG, "Invalid userId from RoleManager", e);
                customerId = 0;
            }
        }

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

        fetchMyCoupons(customerId);
    }

    private void fetchMyCoupons(int customerId) {
        Log.i(TAG, "Requesting coupons for customerId=" + customerId + ", lang=en");

        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.getMyCoupons(customerId, "en").enqueue(new Callback<MyCouponListResponse>() {
            @Override
            public void onResponse(Call<MyCouponListResponse> call, Response<MyCouponListResponse> response) {
                Log.i(TAG, "fetchMyCoupons onResponse: HTTP " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "API Response body: " + new Gson().toJson(response.body()));
                    if (response.body().isSuccess()) {
                        myCoupons.clear();
                        myCoupons.addAll(response.body().getCoupons());
                        adapter.notifyDataSetChanged();
                        Log.i(TAG, "Coupons loaded successfully, count=" + myCoupons.size());
                    } else {
                        Log.w(TAG, "API returned success=false, body=" + new Gson().toJson(response.body()));
                        Toast.makeText(MyCouponsActivity.this, "No coupons found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "API call failed, errorBody=" + response.errorBody());
                    Toast.makeText(MyCouponsActivity.this, "Failed to load coupons", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyCouponListResponse> call, Throwable t) {
                Log.e(TAG, "fetchMyCoupons onFailure", t);
                Toast.makeText(MyCouponsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuantityPickerAndUse(Coupon coupon, int position) {
        int ownedQty = coupon.getQuantity();
        int maxUsable = ownedQty;

        Log.i(TAG, "Preparing to use couponId=" + coupon.getCouponId() + ", ownedQty=" + ownedQty);

        // clamp by per_customer_per_day
        Integer perDayLimit = coupon.getPerCustomerPerDay();
        if (perDayLimit != null && perDayLimit > 0 && maxUsable > perDayLimit) {
            Log.i(TAG, "Applying per_customer_per_day limit=" + perDayLimit);
            maxUsable = perDayLimit;
        }

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
                    Log.i(TAG, "User selected quantity=" + quantity + " for couponId=" + coupon.getCouponId());
                    if (!CouponValidator.isCouponValidForCart(coupon, quantity)) {
                        Toast.makeText(this, "Coupon not valid for this cart", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Coupon validation failed for couponId=" + coupon.getCouponId());
                        return;
                    }
                    useCoupon(coupon, position, quantity);
                })
                .show();
    }

    private void useCoupon(Coupon coupon, int position, int quantity) {
        int orderTotal = getIntent().getIntExtra("order_total", 0);
        ArrayList<Integer> menuItemIds = getIntent().getIntegerArrayListExtra("menu_item_ids");
        if (menuItemIds == null) {
            menuItemIds = new ArrayList<>();
        }

        Log.i(TAG, "Attempting to use couponId=" + coupon.getCouponId() +
                ", qty=" + quantity +
                ", orderTotal=" + orderTotal +
                ", eligibleItems=" + menuItemIds);

        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.useCoupon(customerId, coupon.getCouponId(), quantity, orderTotal, menuItemIds)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        Log.i(TAG, "useCoupon API Response: HTTP " + response.code());
                        Log.d(TAG, "useCoupon Response body: " + new Gson().toJson(response.body()));

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Log.i(TAG, "Coupon applied successfully, couponId=" + coupon.getCouponId());
                            Intent result = new Intent();
                            result.putExtra("selectedCoupon", coupon);
                            setResult(RESULT_OK, result);

                            adapter.decrementCouponQuantity(position, quantity);
                            finish();
                        } else {
                            Log.w(TAG, "Coupon apply failed, body=" + new Gson().toJson(response.body()));
                            Toast.makeText(MyCouponsActivity.this,
                                    "Failed to apply coupon", Toast.LENGTH_SHORT).show();
                            reEnableButton(position);
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Log.e(TAG, "useCoupon API request failed", t);
                        Toast.makeText(MyCouponsActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
