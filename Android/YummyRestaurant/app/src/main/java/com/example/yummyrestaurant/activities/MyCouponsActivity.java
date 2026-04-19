package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Parcel;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.MyCouponAdapter;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.MyCouponListResponse;
import com.example.yummyrestaurant.utils.LanguageManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.example.yummyrestaurant.utils.CouponValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCouponsActivity extends BaseCustomerActivity {
    private static final String TAG = "MyCouponsActivity";

    private RecyclerView rvMyCoupons;
    private TextView tvMyCouponsEmpty;
    private MyCouponAdapter adapter;
    private List<Coupon> myCoupons = new ArrayList<>();
    private int customerId;
    private boolean fromCart;

    private Button btnDone;

    private final ArrayList<Coupon> selectedCoupons = new ArrayList<>();
    private final HashMap<Integer, Integer> couponQuantities = new HashMap<>();
    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "=== Entering MyCouponsActivity ===");
        setContentView(R.layout.activity_my_coupons);
        setupBottomFunctionBar();

        rvMyCoupons = findViewById(R.id.rvMyCoupons);
        tvMyCouponsEmpty = findViewById(R.id.tvMyCouponsEmpty);
        rvMyCoupons.setLayoutManager(new LinearLayoutManager(this));

        btnDone = findViewById(R.id.btnDone);

        fromCart = getIntent().getBooleanExtra("fromCart", false);
        currentLanguage = LanguageManager.getCurrentLanguage(this);
        int intentCid = getIntent().getIntExtra("customer_id", Integer.MIN_VALUE);

        if (fromCart) {
            Log.i(TAG, "Activity launched from CartActivity");
        } else {
            Log.i(TAG, "Activity launched directly (not from cart)");
            // Disable Done button if not from cart
            btnDone.setEnabled(false);
            btnDone.setAlpha(0.5f);
        }

        if (intentCid != Integer.MIN_VALUE) {
            customerId = intentCid;
        } else {
            try {
                customerId = Integer.parseInt(RoleManager.getUserId());
            } catch (Exception e) {
                customerId = 0;
            }
        }

        adapter = new MyCouponAdapter(myCoupons, (coupon, position) -> {
            if (!fromCart) {
                Toast.makeText(this, getString(R.string.coupons_checkout_only), Toast.LENGTH_SHORT).show();
                return;
            }
            showQuantityPickerAndUse(coupon, position);
        }, fromCart);
        rvMyCoupons.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                updateEmptyState();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateEmptyState();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateEmptyState();
            }
        });
        updateEmptyState();

        fetchMyCoupons(customerId);

        // Done button logic
        // Done button logic: always return to CartActivity
        btnDone.setOnClickListener(v -> {
            // Build result to return to CartActivity
            Intent result = new Intent();
            result.putParcelableArrayListExtra("selectedCoupons", selectedCoupons);
            result.putExtra("couponQuantities", couponQuantities);


            // Whether zero or many coupons were selected, return to CartActivity
            setResult(RESULT_OK, result);
            finish();
        });

    }

    private void fetchMyCoupons(int customerId) {
        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.getMyCoupons(customerId, currentLanguage).enqueue(new Callback<MyCouponListResponse>() {
            @Override
            public void onResponse(Call<MyCouponListResponse> call, Response<MyCouponListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        myCoupons.clear();
                        myCoupons.addAll(response.body().getCoupons());
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        Log.i(TAG, "Coupons loaded successfully, count=" + myCoupons.size());

                        if (myCoupons.isEmpty()) {
                            btnDone.setText(getString(R.string.proceed_without_coupon));
                        }
                    } else {
                        myCoupons.clear();
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        Toast.makeText(MyCouponsActivity.this, getString(R.string.no_coupons_found), Toast.LENGTH_SHORT).show();
                        btnDone.setText(getString(R.string.proceed_without_coupon));
                    }
                } else {
                    myCoupons.clear();
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    Toast.makeText(MyCouponsActivity.this, getString(R.string.failed_load_coupons), Toast.LENGTH_SHORT).show();
                    btnDone.setText(getString(R.string.proceed_without_coupon));
                }
            }

            @Override
            public void onFailure(Call<MyCouponListResponse> call, Throwable t) {
                myCoupons.clear();
                adapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(MyCouponsActivity.this, getString(R.string.network_error_with_reason, t.getMessage()), Toast.LENGTH_SHORT).show();
                btnDone.setText(getString(R.string.proceed_without_coupon));
            }
        });
    }

    private void updateEmptyState() {
        if (tvMyCouponsEmpty == null || rvMyCoupons == null) return;
        boolean empty = adapter == null || adapter.getItemCount() == 0;
        tvMyCouponsEmpty.setVisibility(empty ? TextView.VISIBLE : TextView.GONE);
        rvMyCoupons.setVisibility(empty ? RecyclerView.GONE : RecyclerView.VISIBLE);
    }

    private void showQuantityPickerAndUse(Coupon coupon, int position) {
        int ownedQty = coupon.getQuantity();
        int maxUsable = CouponValidator.getMaxUsableQuantityForCart(coupon);
        if (maxUsable > ownedQty) {
            maxUsable = ownedQty;
        }

        if (maxUsable <= 0) {
            Toast.makeText(this, getString(R.string.no_coupons_available_to_use), Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = new String[maxUsable];
        for (int i = 0; i < maxUsable; i++) {
            options[i] = String.valueOf(i + 1);
        }

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_quantity_to_use))
                .setItems(options, (dialog, which) -> {
                    int quantity = which + 1;
                    
                    // 客戶端驗證
                    CouponValidator.ValidationResult validationResult = 
                        CouponValidator.validateCouponWithReason(coupon, quantity);
                    
                    if (!validationResult.isValid) {
                        String message = validationResult.reason.isEmpty() ? 
                            getString(R.string.coupon_not_valid_for_cart) : validationResult.reason;
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    useCoupon(coupon, position, quantity);
                })
                .show();
    }


    private void useCoupon(Coupon coupon, int position, int quantity) {
        // 防止重複使用檢查 - 檢查是否已經在此次結帳中使用過該優惠券
        if (couponQuantities.containsKey(coupon.getCouponId())) {
            int alreadyUsedQty = couponQuantities.get(coupon.getCouponId());
            int remainingQty = coupon.getQuantity() - alreadyUsedQty;
            
            if (remainingQty < quantity) {
                Toast.makeText(this, 
                    getString(R.string.can_only_use_more_coupon, remainingQty), 
                    Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int previousQty = couponQuantities.getOrDefault(coupon.getCouponId(), 0);
        couponQuantities.put(coupon.getCouponId(), previousQty + quantity);

        selectedCoupons.add(copyCouponWithQuantity(coupon, quantity));

        // 只做本地選擇，實際標記使用會等付款成功後才送去後端
        adapter.decrementCouponQuantity(position, quantity);

        Toast.makeText(MyCouponsActivity.this,
                getString(R.string.coupon_applied_press_done, quantity),
                Toast.LENGTH_SHORT).show();
    }

    private Coupon copyCouponWithQuantity(Coupon source, int quantity) {
        Parcel parcel = Parcel.obtain();
        try {
            source.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            Coupon copy = Coupon.CREATOR.createFromParcel(parcel);
            copy.setQuantity(quantity);
            return copy;
        } finally {
            parcel.recycle();
        }
    }
}
