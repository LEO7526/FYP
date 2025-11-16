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
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.models.MyCouponListResponse;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        // Prefer the customer_id passed via Intent (from CartActivity). Fall back to RoleManager.
        int intentCid = getIntent().getIntExtra("customer_id", Integer.MIN_VALUE);
        if (intentCid != Integer.MIN_VALUE) {
            customerId = intentCid;
            Log.d(TAG, "onCreate: customerId from Intent = " + customerId);
        } else {
            try {
                customerId = Integer.parseInt(RoleManager.getUserId());
            } catch (Exception e) {
                Log.e(TAG, "Invalid userId from RoleManager", e);
                customerId = 0;
            }
            Log.d(TAG, "onCreate: customerId from RoleManager = " + customerId);
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

    // --- Coupon picker replacement ---
    private void showQuantityPickerAndUse(Coupon coupon, int position) {
        String appliesTo = coupon.getAppliesTo();
        int maxUsable;
        int ownedQty = coupon.getQuantity();

        maxUsable = ownedQty;

        // For item-specific coupons:
        if ("item".equalsIgnoreCase(appliesTo) && coupon.getApplicableItems() != null && !coupon.getApplicableItems().isEmpty()) {
            int eligibleCount = 0;
            Map<CartItem, Integer> cartItems = CartManager.getCartItems();
            List<Integer> applicableIds = coupon.getApplicableItems();

            for (Map.Entry<CartItem, Integer> entry : cartItems.entrySet()) {
                MenuItem mItem = entry.getKey().getMenuItem();
                Log.d(TAG, "Cart contains item_id=" + mItem.getId() + " category_id=" + mItem.getCategoryId());
                if (mItem != null && applicableIds.contains(mItem.getId())) {
                    eligibleCount += entry.getValue();
                }
            }
            if (eligibleCount == 0) {
                Toast.makeText(this, "No applicable items in cart for this coupon", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Coupon not usable, no matching items in cart for couponId=" + coupon.getCouponId());
                return;
            }
            maxUsable = Math.min(ownedQty, eligibleCount);
        }

        // For category-specific coupons (if used in backend, update similar to above)
        if ("category".equalsIgnoreCase(appliesTo) && coupon.getApplicableCategories() != null && !coupon.getApplicableCategories().isEmpty()) {
            int eligibleCount = 0;
            List<Integer> applicableCats = coupon.getApplicableCategories();
            Map<CartItem, Integer> cartItems = CartManager.getCartItems();
            for (Map.Entry<CartItem, Integer> entry : cartItems.entrySet()) {
                MenuItem mItem = entry.getKey().getMenuItem();
                Log.d(TAG, "Cart contains item_id=" + mItem.getId() + " category_id=" + mItem.getCategoryId());
                if (mItem != null && mItem.getCategoryId() != null && applicableCats.contains(mItem.getCategoryId())) {
                    eligibleCount += entry.getValue();
                }
            }
            if (eligibleCount == 0) {
                Toast.makeText(this, "No applicable category items in cart for this coupon", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Coupon not usable, no matching category in cart for couponId=" + coupon.getCouponId());
                return;
            }
            maxUsable = Math.min(ownedQty, eligibleCount);
        }

        // Clamp maxUsable per any 'per_customer_per_day' limit
        Integer perDayLimit = coupon.getPerCustomerPerDay();
        if (perDayLimit != null && perDayLimit > 0 && maxUsable > perDayLimit) {
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
                    if (!isCouponValidForCart(coupon, quantity)) {
                        return;
                    }
                    useCoupon(coupon, position, quantity);
                })
                .show();
    }

    // --- Validator replacement ---
    private boolean isCouponValidForCart(Coupon coupon, int requestedQty) {
        if (coupon == null) {
            Log.d(TAG, "Coupon is null");
            return false;
        }

        // Minimum spend
        Double minSpend = coupon.getMinSpend();
        int totalCents = CartManager.getTotalAmountInCents();
        if (minSpend != null && totalCents < (int) Math.round(minSpend * 100)) {
            Log.d(TAG, "Invalid: below min spend");
            return false;
        }

        // Applies-to/order type
        String appliesTo = coupon.getAppliesTo();
        String orderType = CartManager.getOrderType();
        boolean appliesToAll = (appliesTo == null) || appliesTo.trim().isEmpty();
        if (!appliesToAll && !appliesTo.equalsIgnoreCase(orderType)) {
            Log.d(TAG, "Invalid: not valid for " + orderType);
            return false;
        }

        // Birthday-only
        if (coupon.isBirthdayOnly()) {
            try {
                if (!RoleManager.isTodayUserBirthday()) {
                    Log.d(TAG, "Invalid: not user's birthday");
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking birthday", e);
                return false;
            }
        }

        // Coupon stacking: only allow if all applied coupons can be combined
        if (!coupon.isCombineWithOtherDiscounts()) {
            if (CartManager.hasOtherDiscountsApplied()) {
                Log.d(TAG, "Invalid: other discounts already applied");
                return false;
            }
        }

        // Check per_customer_per_day (if tracked or can query from backend/history)
        Integer perDayLimit = coupon.getPerCustomerPerDay();
        if (perDayLimit != null && perDayLimit > 0 && requestedQty > perDayLimit) {
            Log.d(TAG, "Invalid: requested exceeds per_customer_per_day limit");
            return false;
        }

        // Item/category applicability already checked in picker above

        Log.d(TAG, "Coupon is valid ✅");
        return true;
    }



    private void useCoupon(Coupon coupon, int position, int quantity) {
        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.useCoupon(customerId, coupon.getCouponId(), quantity).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                // 👇 Always log the raw body, regardless of success
                Log.d(TAG, "useCoupon response: " + new Gson().toJson(response.body()));

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Intent result = new Intent();
                    result.putExtra("selectedCoupon", coupon);
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
                Log.e(TAG, "useCoupon onFailure", t); // 👈 log the error too
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
            Log.d(TAG, "Coupon is null");
            return false;
        }

        Log.d(TAG, "Validating coupon: " + coupon.getTitle() + " (ID=" + coupon.getCouponId() + ")");
        int totalCents = CartManager.getTotalAmountInCents();
        Log.d(TAG, "Cart total (cents): " + totalCents);

        // 1. Minimum spend
        Double minSpend = coupon.getMinSpend();
        if (minSpend != null) {
            Log.d(TAG, "Coupon minSpend=" + minSpend);
            if (totalCents < (int) Math.round(minSpend * 100)) {
                Log.d(TAG, "Invalid: below min spend");
                return false;
            }
        }

        // 2. AppliesTo simplified check
        String appliesTo = coupon.getAppliesTo(); // may return null
        String orderType = CartManager.getOrderType(); // e.g. "dine_in", "takeaway", "delivery"
        boolean appliesToAll = (appliesTo == null) || appliesTo.trim().isEmpty();

        if (!appliesToAll && !appliesTo.equalsIgnoreCase(orderType)) {
            Log.d(TAG, "Invalid: not valid for " + orderType);
            return false;
        } else {
            Log.d(TAG, "Valid: coupon applies");
        }

        // 3. Birthday-only
        if (coupon.isBirthdayOnly()) {
            Log.d(TAG, "Coupon is birthday-only, checking RoleManager...");
            try {
                if (!RoleManager.isTodayUserBirthday()) {
                    Log.d(TAG, "Invalid: not user's birthday");
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking birthday", e);
                return false;
            }
        }

        // 4. Discount stacking
        if (!coupon.isCombineWithOtherDiscounts()) {   // now reflects JSON field (0 = false, 1 = true)
            Log.d(TAG, "Coupon cannot combine with other discounts");
            if (CartManager.hasOtherDiscountsApplied()) {
                Log.d(TAG, "Invalid: other discounts already applied");
                return false;
            }
        }

        Log.d(TAG, "Coupon is valid ✅");
        return true;
    }


}
