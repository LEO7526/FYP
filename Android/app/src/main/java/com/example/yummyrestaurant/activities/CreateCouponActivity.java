package com.example.yummyrestaurant.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiConfig;
import com.example.yummyrestaurant.api.ApiService;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.CouponListResponse;
import com.example.yummyrestaurant.models.CouponPointsResponse;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.android.material.tabs.TabLayout;
import com.example.yummyrestaurant.utils.LanguageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateCouponActivity extends StaffBaseActivity {

    private static final String TAG = "CreateCouponActivity";

    private EditText etPoints, etDiscountValue, etMinSpend, etMaxDiscount, etLimit;
    private TextView tvExpiryDate;
    private RadioGroup rgDiscountType, rgAppliesTo;
    private CheckBox cbMinSpend, cbMaxDiscount, cbLimit, cbDineIn, cbTakeaway, cbDelivery, cbCombine, cbBirthday;
    private Button btnSubmit;

    private TabLayout tabMain;
    private View viewCreate;
    private RecyclerView rvCouponList;

    private EditText etTitleEn, etDescEn, etTermEn;
    private EditText etTitleTw, etDescTw, etTermTw;
    private EditText etTitleCn, etDescCn, etTermCn;

    private final List<Coupon> couponList = new ArrayList<>();
    private CouponListAdapter couponListAdapter;
    private int currentPoints = 0;
    private int customerId = 0;

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_coupon);

        etPoints = findViewById(R.id.etPoints);
        etDiscountValue = findViewById(R.id.etDiscountValue);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        rgDiscountType = findViewById(R.id.rgDiscountType);
        rgAppliesTo = findViewById(R.id.rgAppliesTo);
        cbMinSpend = findViewById(R.id.cbMinSpend);
        etMinSpend = findViewById(R.id.etMinSpend);
        cbMaxDiscount = findViewById(R.id.cbMaxDiscount);
        etMaxDiscount = findViewById(R.id.etMaxDiscount);
        cbLimit = findViewById(R.id.cbLimit);
        etLimit = findViewById(R.id.etLimit);
        cbDineIn = findViewById(R.id.cbDineIn);
        cbTakeaway = findViewById(R.id.cbTakeaway);
        cbDelivery = findViewById(R.id.cbDelivery);
        cbCombine = findViewById(R.id.cbCombine);
        cbBirthday = findViewById(R.id.cbBirthday);
        btnSubmit = findViewById(R.id.btnSubmit);

        tabMain = findViewById(R.id.tabMain);
        viewCreate = findViewById(R.id.viewCreate);
        rvCouponList = findViewById(R.id.rvCouponList);

        rvCouponList.setLayoutManager(new LinearLayoutManager(this));
        couponListAdapter = new CouponListAdapter(couponList, coupon -> {
            Intent intent = new Intent(CreateCouponActivity.this, CouponDetailActivity.class);
            intent.putExtra("coupon_id", coupon.getCouponId());
            intent.putExtra("current_points", currentPoints);
            intent.putExtra("from_staff", true);
            startActivity(intent);
        });
        rvCouponList.setAdapter(couponListAdapter);

        etTitleEn = findViewById(R.id.etTitleEn);
        etDescEn = findViewById(R.id.etDescEn);
        etTermEn = findViewById(R.id.etTermEn);
        etTitleTw = findViewById(R.id.etTitleTw);
        etDescTw = findViewById(R.id.etDescTw);
        etTermTw = findViewById(R.id.etTermTw);
        etTitleCn = findViewById(R.id.etTitleCn);
        etDescCn = findViewById(R.id.etDescCn);
        etTermCn = findViewById(R.id.etTermCn);

        tvExpiryDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                selectedDate = year + "-" + (month + 1) + "-" + day;
                tvExpiryDate.setText(selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        cbMinSpend.setOnCheckedChangeListener((bv, isChecked) -> etMinSpend.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        cbMaxDiscount.setOnCheckedChangeListener((bv, isChecked) -> etMaxDiscount.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        cbLimit.setOnCheckedChangeListener((bv, isChecked) -> etLimit.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        setupTabSwitching();
        resolveCustomerAndLoadPoints();
        btnSubmit.setOnClickListener(v -> submitCoupon());
    }

    private void resolveCustomerAndLoadPoints() {
        try {
            customerId = Integer.parseInt(RoleManager.getUserId());
        } catch (Exception e) {
            customerId = 0;
        }

        if (customerId <= 0) {
            currentPoints = 0;
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getCouponPoints(customerId).enqueue(new Callback<CouponPointsResponse>() {
            @Override
            public void onResponse(Call<CouponPointsResponse> call, Response<CouponPointsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentPoints = response.body().getCouponPoints();
                } else {
                    currentPoints = 0;
                }
            }

            @Override
            public void onFailure(Call<CouponPointsResponse> call, Throwable t) {
                currentPoints = 0;
            }
        });
    }

    private void setupTabSwitching() {
        showCreateTab();

        tabMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showCreateTab();
                } else {
                    showListTab();
                    fetchCouponList();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    fetchCouponList();
                }
            }
        });
    }

    private void showCreateTab() {
        viewCreate.setVisibility(View.VISIBLE);
        rvCouponList.setVisibility(View.GONE);
    }

    private void showListTab() {
        viewCreate.setVisibility(View.GONE);
        rvCouponList.setVisibility(View.VISIBLE);
    }

    private void fetchCouponList() {
        CouponApiService api = RetrofitClient.getClient(this).create(CouponApiService.class);
        api.getCoupons(LanguageManager.getLangCode(this)).enqueue(new Callback<CouponListResponse>() {
            @Override
            public void onResponse(Call<CouponListResponse> call, Response<CouponListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    couponList.clear();
                    couponList.addAll(response.body().getCoupons());
                    couponListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CreateCouponActivity.this, R.string.failed_load_coupon_list, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CouponListResponse> call, Throwable t) {
                Log.e(TAG, "fetchCouponList failed", t);
                Toast.makeText(CreateCouponActivity.this,
                        getString(R.string.error_prefix, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitCoupon() {
        try {
            JSONObject json = new JSONObject();
            json.put("points_required", etPoints.getText().toString());
            json.put("expiry_date", selectedDate);

            int typeId = rgDiscountType.getCheckedRadioButtonId();
            String typeStr = (typeId == R.id.rbPercent) ? "percent" : (typeId == R.id.rbCash ? "cash" : "free_item");
            json.put("discount_type", typeStr);
            json.put("discount_value", etDiscountValue.getText().toString());

            int applyId = rgAppliesTo.getCheckedRadioButtonId();
            String applyStr = "whole_order";
            if (applyId == R.id.rbCategory) applyStr = "category";
            else if (applyId == R.id.rbItem) applyStr = "item";
            else if (applyId == R.id.rbPackage) applyStr = "package";
            json.put("applies_to", applyStr);

            JSONArray ids = new JSONArray();
            json.put("selected_ids", ids);

            if (cbMinSpend.isChecked()) json.put("min_spend", etMinSpend.getText().toString());
            if (cbMaxDiscount.isChecked()) json.put("max_discount", etMaxDiscount.getText().toString());
            if (cbLimit.isChecked()) json.put("per_customer_limit", etLimit.getText().toString());

            json.put("valid_dine_in", cbDineIn.isChecked());
            json.put("valid_takeaway", cbTakeaway.isChecked());
            json.put("valid_delivery", cbDelivery.isChecked());
            json.put("combine_discount", cbCombine.isChecked());
            json.put("birthday_only", cbBirthday.isChecked());

            JSONObject transObj = new JSONObject();
            transObj.put("en", buildTransJson(etTitleEn, etDescEn, etTermEn));
            transObj.put("zh-TW", buildTransJson(etTitleTw, etDescTw, etTermTw));
            transObj.put("zh-CN", buildTransJson(etTitleCn, etDescCn, etTermCn));
            json.put("translations", transObj);

            // Use the same environment-aware base URL as Retrofit APIs.
            String url = ApiConfig.getBaseUrl(this) + "create_coupon_api.php";
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        String status = response.optString("status", "");
                        if ("success".equalsIgnoreCase(status)) {
                            Toast.makeText(this, R.string.coupon_created, Toast.LENGTH_SHORT).show();

                            TabLayout.Tab listTab = tabMain.getTabAt(1);
                            if (listTab != null) {
                                listTab.select();
                            } else {
                                showListTab();
                                fetchCouponList();
                            }
                        } else {
                                String msg = response.optString("message", getString(R.string.failed_create_coupon));
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    },
                            error -> Toast.makeText(this,
                                getString(R.string.error_prefix, error.getMessage()),
                                Toast.LENGTH_SHORT).show()
            );
            Volley.newRequestQueue(this).add(req);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject buildTransJson(EditText title, EditText desc, EditText term) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("title", title.getText().toString());
        obj.put("description", desc.getText().toString());
        JSONArray termArr = new JSONArray();
        if (!term.getText().toString().isEmpty()) termArr.put(term.getText().toString());
        obj.put("terms", termArr);
        return obj;
    }

    private static class CouponListAdapter extends RecyclerView.Adapter<CouponListAdapter.ViewHolder> {
        interface OnCouponItemClickListener {
            void onCouponItemClick(Coupon coupon);
        }

        private final List<Coupon> items;
        private final OnCouponItemClickListener clickListener;

        CouponListAdapter(List<Coupon> items, OnCouponItemClickListener clickListener) {
            this.items = items;
            this.clickListener = clickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Coupon c = items.get(position);
            android.content.Context context = holder.itemView.getContext();
            holder.tvTitle.setText(c.getTitle());
            holder.tvDesc.setText(c.getDescription());
            holder.tvPoints.setText(context.getString(R.string.requires_points, c.getPointsRequired()));
            holder.tvExpiry.setText(c.getExpiryDate() != null && !c.getExpiryDate().isEmpty()
                ? context.getString(R.string.valid_until, c.getExpiryDate())
                : context.getString(R.string.no_expiry));
            holder.btnRedeem.setText(R.string.view_details);
            holder.btnRedeem.setEnabled(true);
            holder.btnRedeem.setAlpha(1f);

            View.OnClickListener openDetail = v -> {
                if (clickListener != null) {
                    clickListener.onCouponItemClick(c);
                }
            };
            holder.itemView.setOnClickListener(openDetail);
            holder.btnRedeem.setOnClickListener(openDetail);
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc, tvPoints, tvExpiry;
            Button btnRedeem;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvCouponTitle);
                tvDesc = itemView.findViewById(R.id.tvCouponDescription);
                tvPoints = itemView.findViewById(R.id.tvCouponPointsRequired);
                tvExpiry = itemView.findViewById(R.id.tvCouponExpiry);
                btnRedeem = itemView.findViewById(R.id.btnRedeem);
            }
        }
    }
}
