package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.example.yummyrestaurant.utils.CouponValidator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
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

    private Button btnDone;

    private final ArrayList<Coupon> selectedCoupons = new ArrayList<>();
    private final HashMap<Integer, Integer> couponQuantities = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "=== Entering MyCouponsActivity ===");
        setContentView(R.layout.activity_my_coupons);
        setupBottomFunctionBar();

        rvMyCoupons = findViewById(R.id.rvMyCoupons);
        rvMyCoupons.setLayoutManager(new LinearLayoutManager(this));

        btnDone = findViewById(R.id.btnDone);

        fromCart = getIntent().getBooleanExtra("fromCart", false);
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
                Toast.makeText(this, "Coupons can only be used during checkout", Toast.LENGTH_SHORT).show();
                return;
            }
            showQuantityPickerAndUse(coupon, position);
        }, fromCart);
        rvMyCoupons.setAdapter(adapter);

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
        api.getMyCoupons(customerId, "en").enqueue(new Callback<MyCouponListResponse>() {
            @Override
            public void onResponse(Call<MyCouponListResponse> call, Response<MyCouponListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        myCoupons.clear();
                        myCoupons.addAll(response.body().getCoupons());
                        adapter.notifyDataSetChanged();
                        Log.i(TAG, "Coupons loaded successfully, count=" + myCoupons.size());

                        if (myCoupons.isEmpty()) {
                            btnDone.setText("Proceed without coupon");
                        }
                    } else {
                        Toast.makeText(MyCouponsActivity.this, "No coupons found", Toast.LENGTH_SHORT).show();
                        btnDone.setText("Proceed without coupon");
                    }
                } else {
                    Toast.makeText(MyCouponsActivity.this, "Failed to load coupons", Toast.LENGTH_SHORT).show();
                    btnDone.setText("Proceed without coupon");
                }
            }

            @Override
            public void onFailure(Call<MyCouponListResponse> call, Throwable t) {
                Toast.makeText(MyCouponsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnDone.setText("Proceed without coupon");
            }
        });
    }

    private void showQuantityPickerAndUse(Coupon coupon, int position) {
        int ownedQty = coupon.getQuantity();
        int maxUsable = ownedQty;

        if (maxUsable <= 0) {
            Toast.makeText(this, "No coupons available to use", Toast.LENGTH_SHORT).show();
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
                    
                    // 客戶端驗證
                    CouponValidator.ValidationResult validationResult = 
                        CouponValidator.validateCouponWithReason(coupon, quantity);
                    
                    if (!validationResult.isValid) {
                        String message = validationResult.reason.isEmpty() ? 
                            "Coupon not valid for this cart" : validationResult.reason;
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

        // 防止重複使用檢查 - 檢查是否已經在此次結帳中使用過該優惠券
        if (couponQuantities.containsKey(coupon.getCouponId())) {
            int alreadyUsedQty = couponQuantities.get(coupon.getCouponId());
            int remainingQty = coupon.getQuantity() - alreadyUsedQty;
            
            if (remainingQty < quantity) {
                Toast.makeText(this, 
                    "You can only use " + remainingQty + " more of this coupon", 
                    Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Map<String, Integer> apiCouponQuantities = new HashMap<>();
        apiCouponQuantities.put("coupon_quantities[" + coupon.getCouponId() + "]", quantity);

        String orderType = CartManager.getOrderType();

        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.useCoupons(customerId, orderTotal, orderType, apiCouponQuantities, menuItemIds)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // 更新已使用的數量
                            int previousQty = couponQuantities.getOrDefault(coupon.getCouponId(), 0);
                            couponQuantities.put(coupon.getCouponId(), previousQty + quantity);
                            
                            // 添加到已選擇列表（用於返回結果）
                            selectedCoupons.add(coupon);
                            
                            // 從列表中移除或更新數量
                            adapter.decrementCouponQuantity(position, quantity);
                            
                            Toast.makeText(MyCouponsActivity.this,
                                    "Coupon applied (" + quantity + "). Press Done when finished selecting.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = response.body() != null ? 
                                response.body().getMessage() : "Failed to apply coupon";
                            Toast.makeText(MyCouponsActivity.this,
                                    errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Toast.makeText(MyCouponsActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
