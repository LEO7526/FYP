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
import com.example.yummyrestaurant.utils.CartManager;
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

                    // 🚨 Validate coupon before using
                    if (!isCouponValidForCart(coupon)) {
                        return;
                    }

                    useCoupon(coupon, position, quantity);
                })
                .show();
    }

    private void useCoupon(Coupon coupon, int position, int quantity) {
        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.useCoupon(customerId, coupon.getCouponId(), quantity).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Intent result = new Intent();
                    result.putExtra("selectedCoupon", coupon); // 👈 pass full object
                    setResult(RESULT_OK, result);

                    adapter.decrementCouponQuantity(position, quantity);
                    finish();
                } else {
                    Toast.makeText(MyCouponsActivity.this, "Failed to apply coupon", Toast.LENGTH_SHORT).show();
                    reEnableButton(position);
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(MyCouponsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private boolean isCouponValidForCart(Coupon coupon) {
        if (coupon == null) {
            Log.d("CouponDebug", "Coupon is null");
            return false;
        }

        Log.d("CouponDebug", "Validating coupon: " + coupon.getTitle() + " (ID=" + coupon.getCouponId() + ")");
        int totalCents = CartManager.getTotalAmountInCents();
        Log.d("CouponDebug", "Cart total (cents): " + totalCents);

        // 1. Minimum spend
        Double minSpend = coupon.getMinSpend();
        if (minSpend != null) {
            Log.d("CouponDebug", "Coupon minSpend=" + minSpend);
            if (totalCents < (int) Math.round(minSpend * 100)) {
                Log.d("CouponDebug", "Invalid: below min spend");
                return false;
            }
        }

        // 2. Applies to scope
        String appliesTo = coupon.getAppliesTo();
        Log.d("CouponDebug", "Coupon appliesTo=" + appliesTo);

        if ("item".equalsIgnoreCase(appliesTo)) {
            List<Integer> itemIds = coupon.getApplicableItems();
            if (itemIds != null && !itemIds.isEmpty()) {
                Log.d("CouponDebug", "Checking applicableItems=" + itemIds);
                if (!CartManager.hasAnyItem(itemIds)) {
                    Log.d("CouponDebug", "Invalid: no matching items in cart");
                    return false;
                }
            }

            String category = coupon.getItemCategory();
            if (category != null && !category.trim().isEmpty()) {
                Log.d("CouponDebug", "Checking itemCategory=" + category);
                if (!CartManager.hasItemCategory(category)) {
                    Log.d("CouponDebug", "Invalid: no matching category in cart");
                    return false;
                }
            }
        } else if ("category".equalsIgnoreCase(appliesTo)) {
            List<Integer> categoryIds = coupon.getApplicableCategories();
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Log.d("CouponDebug", "Checking applicableCategories=" + categoryIds);
                if (!CartManager.hasAnyCategory(categoryIds)) {
                    Log.d("CouponDebug", "Invalid: no matching categories in cart");
                    return false;
                }
            }
        }

        // 3. Order type
        String orderType = CartManager.getOrderType();
        Log.d("CouponDebug", "Order type=" + orderType);

        if ("dine_in".equals(orderType) && !coupon.isValidDineIn()) {
            Log.d("CouponDebug", "Invalid: not valid for dine-in");
            return false;
        }
        if ("takeaway".equals(orderType) && !coupon.isValidTakeaway()) {
            Log.d("CouponDebug", "Invalid: not valid for takeaway");
            return false;
        }
        if ("delivery".equals(orderType) && !coupon.isValidDelivery()) {
            Log.d("CouponDebug", "Invalid: not valid for delivery");
            return false;
        }

        // 4. Birthday-only
        if (coupon.isBirthdayOnly()) {
            Log.d("CouponDebug", "Coupon is birthday-only, checking RoleManager...");
            try {
                if (!RoleManager.isTodayUserBirthday()) {
                    Log.d("CouponDebug", "Invalid: not user's birthday");
                    return false;
                }
            } catch (Exception e) {
                Log.e("CouponDebug", "Error checking birthday", e);
                return false;
            }
        }

        // 5. Discount stacking
        if (!coupon.isCombineWithOtherDiscounts()) {
            Log.d("CouponDebug", "Coupon cannot combine with other discounts");
            if (CartManager.hasOtherDiscountsApplied()) {
                Log.d("CouponDebug", "Invalid: other discounts already applied");
                return false;
            }
        }

        Log.d("CouponDebug", "Coupon is valid ✅");
        return true;
    }
}
