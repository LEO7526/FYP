package com.example.yummyrestaurant.activities;

import android.app.DatePickerDialog;
import android.app.AlertDialog; // 記得是這個
import android.os.Bundle;
import android.view.LayoutInflater; // 記得加
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.adapters.CouponListAdapter;
import com.example.yummyrestaurant.api.ApiConstants;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateCouponActivity extends AppCompatActivity {

    // Tabs & List
    private TabLayout tabMain;
    private View viewCreate;
    private RecyclerView rvCouponList;
    private CouponListAdapter listAdapter;
    private List<JSONObject> couponList = new ArrayList<>();

    // UI Components
    private EditText etPoints, etDiscountValue, etMinSpend, etMaxDiscount, etLimit;
    private TextView tvExpiryDate, tvSelectedScope;
    private RadioGroup rgDiscountType, rgAppliesTo;
    private CheckBox cbMinSpend, cbMaxDiscount, cbLimit, cbDineIn, cbTakeaway, cbDelivery, cbCombine, cbBirthday;
    private Button btnSelectScope;

    // Language Fields
    private EditText etTitleEn, etDescEn, etTermEn;
    private EditText etTitleTw, etDescTw, etTermTw;
    private EditText etTitleCn, etDescCn, etTermCn;

    // Data
    private String selectedDate = "";
    private List<String> selectedScopeIds = new ArrayList<>();
    private List<ScopeItem> allCategories = new ArrayList<>();
    private List<ScopeItem> allItems = new ArrayList<>();
    private List<ScopeItem> allPackages = new ArrayList<>();

    private String[] dialogNames;
    private String[] dialogIds;
    private boolean[] dialogChecked;

    class ScopeItem {
        String id; String name;
        ScopeItem(String id, String name) { this.id = id; this.name = name; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_coupon);

        initViews();
        setupTabs();
        fetchMetadata();
    }

    private void initViews() {
        tabMain = findViewById(R.id.tabMain);
        viewCreate = findViewById(R.id.viewCreate);
        rvCouponList = findViewById(R.id.rvCouponList);

        rvCouponList.setLayoutManager(new LinearLayoutManager(this));
        // 修改 Adapter 建構，傳入點擊事件
        listAdapter = new CouponListAdapter(this, couponList, this::showCouponDetails);
        rvCouponList.setAdapter(listAdapter);

        etPoints = findViewById(R.id.etPoints);
        etDiscountValue = findViewById(R.id.etDiscountValue);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        rgDiscountType = findViewById(R.id.rgDiscountType);
        rgAppliesTo = findViewById(R.id.rgAppliesTo);
        btnSelectScope = findViewById(R.id.btnSelectScope);
        tvSelectedScope = findViewById(R.id.tvSelectedScope);
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

        etTitleEn = findViewById(R.id.etTitleEn); etDescEn = findViewById(R.id.etDescEn); etTermEn = findViewById(R.id.etTermEn);
        etTitleTw = findViewById(R.id.etTitleTw); etDescTw = findViewById(R.id.etDescTw); etTermTw = findViewById(R.id.etTermTw);
        etTitleCn = findViewById(R.id.etTitleCn); etDescCn = findViewById(R.id.etDescCn); etTermCn = findViewById(R.id.etTermCn);

        findViewById(R.id.btnSubmit).setOnClickListener(v -> submitCoupon());

        setupListeners();
    }

    // === 顯示詳細資料彈窗 (新增方法) ===
    private void showCouponDetails(int couponId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_coupon_detail, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvType = view.findViewById(R.id.tvDetailType);
        TextView tvPoints = view.findViewById(R.id.tvDetailPoints);
        TextView tvExpiry = view.findViewById(R.id.tvDetailExpiry);
        TextView tvRules = view.findViewById(R.id.tvDetailRules);
        TextView tvScope = view.findViewById(R.id.tvDetailScope);
        TextView tvTrans = view.findViewById(R.id.tvDetailTransTW);
        Button btnClose = view.findViewById(R.id.btnCloseDetail);

        tvTitle.setText("Loading...");
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        String url = ApiConstants.BASE_URL + "get_coupon_detail.php?coupon_id=" + couponId;
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONObject data = json.getJSONObject("data");
                            JSONObject info = data.getJSONObject("info");
                            JSONObject trans = data.getJSONObject("translations");
                            JSONArray scope = data.getJSONArray("scope_list");

                            // 1. Title & Type
                            String enTitle = trans.getJSONObject("en").getString("title");
                            tvTitle.setText(enTitle);

                            String typeStr = info.getString("type");
                            String val = info.getString("discount_amount");
                            if (typeStr.equals("percent")) tvType.setText(val + "% OFF");
                            else if (typeStr.equals("cash")) tvType.setText("$" + val + " OFF");
                            else tvType.setText("Free Item");

                            // 2. Info
                            tvPoints.setText(info.getString("points_required"));
                            tvExpiry.setText(info.getString("expiry_date"));

                            // 3. Rules
                            StringBuilder sbRules = new StringBuilder();
                            if (!info.isNull("min_spend")) sbRules.append("• Min Spend: $").append(info.getString("min_spend")).append("\n");
                            if (!info.isNull("max_discount")) sbRules.append("• Max Discount: $").append(info.getString("max_discount")).append("\n");
                            if (!info.isNull("per_customer_per_day")) sbRules.append("• Limit: ").append(info.getString("per_customer_per_day")).append("/day\n");
                            if (info.getInt("birthday_only") == 1) sbRules.append("• Birthday Only\n");
                            if (info.getInt("valid_dine_in") == 1) sbRules.append("• Dine-in  ");
                            if (info.getInt("valid_takeaway") == 1) sbRules.append("• Takeaway  ");
                            tvRules.setText(sbRules.length() > 0 ? sbRules.toString() : "No special rules");

                            // 4. Scope
                            String applies = info.getString("applies_to");
                            if (applies.equals("whole_order")) {
                                tvScope.setText("All Items (Whole Order)");
                            } else {
                                StringBuilder sbScope = new StringBuilder(applies + ": ");
                                for(int i=0; i<scope.length(); i++) sbScope.append(scope.getString(i)).append(", ");
                                tvScope.setText(sbScope.toString());
                            }

                            // 5. Translation Preview (TW)
                            StringBuilder sbTrans = new StringBuilder();
                            String[] langKeys = {"en", "zh-TW", "zh-CN"};
                            String[] langLabels = {"🇬🇧 EN", "🇭🇰 TW", "🇨🇳 CN"};

                            for (int i = 0; i < langKeys.length; i++) {
                                if (trans.has(langKeys[i])) {
                                    JSONObject t = trans.getJSONObject(langKeys[i]);

                                    // 標題
                                    sbTrans.append(langLabels[i]).append(": ")
                                            .append(t.optString("title", "-")).append("\n");

                                    // 描述
                                    String desc = t.optString("desc", "");
                                    if (!desc.isEmpty()) sbTrans.append(desc).append("\n");

                                    // 條款 (Terms) - 這裡就是你原本缺少的
                                    JSONArray terms = t.optJSONArray("terms");
                                    if (terms != null && terms.length() > 0) {
                                        for (int j = 0; j < terms.length(); j++) {
                                            sbTrans.append("  • ").append(terms.getString(j)).append("\n");
                                        }
                                    }
                                    sbTrans.append("\n"); // 語言之間空一行
                                }
                            }

                            tvTrans.setText(sbTrans.toString());
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void setupTabs() {
        tabMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    viewCreate.setVisibility(View.VISIBLE);
                    rvCouponList.setVisibility(View.GONE);
                } else {
                    viewCreate.setVisibility(View.GONE);
                    rvCouponList.setVisibility(View.VISIBLE);
                    fetchCouponList();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchCouponList() {
        String url = ApiConstants.BASE_URL + "get_coupon_list.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONArray arr = json.getJSONArray("data");
                            couponList.clear();
                            for(int i=0; i<arr.length(); i++) couponList.add(arr.getJSONObject(i));
                            listAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {}
                }, error -> Toast.makeText(this, "Failed to load list", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void setupListeners() {
        tvExpiryDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                selectedDate = year + "-" + (month + 1) + "-" + day;
                tvExpiryDate.setText(selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        rgAppliesTo.setOnCheckedChangeListener((group, checkedId) -> {
            selectedScopeIds.clear();
            tvSelectedScope.setText("None selected");

            if (checkedId == R.id.rbWhole) {
                btnSelectScope.setVisibility(View.GONE);
                tvSelectedScope.setVisibility(View.GONE);
            } else {
                btnSelectScope.setVisibility(View.VISIBLE);
                tvSelectedScope.setVisibility(View.VISIBLE);
                prepareScopeData(checkedId);
            }
        });

        btnSelectScope.setOnClickListener(v -> showMultiSelectDialog());

        cbMinSpend.setOnCheckedChangeListener((bv, isChecked) -> etMinSpend.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        cbMaxDiscount.setOnCheckedChangeListener((bv, isChecked) -> etMaxDiscount.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        cbLimit.setOnCheckedChangeListener((bv, isChecked) -> etLimit.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    private void prepareScopeData(int checkedRadioId) {
        List<ScopeItem> targetList;
        if (checkedRadioId == R.id.rbCategory) targetList = allCategories;
        else if (checkedRadioId == R.id.rbItem) targetList = allItems;
        else if (checkedRadioId == R.id.rbPackage) targetList = allPackages;
        else return;

        int size = targetList.size();
        dialogNames = new String[size];
        dialogIds = new String[size];
        dialogChecked = new boolean[size];

        for (int i=0; i<size; i++) {
            dialogNames[i] = targetList.get(i).name;
            dialogIds[i] = targetList.get(i).id;
            dialogChecked[i] = false;
        }
    }

    private void showMultiSelectDialog() {
        if (dialogNames == null || dialogNames.length == 0) {
            Toast.makeText(this, "No items available.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Items");
        builder.setMultiChoiceItems(dialogNames, dialogChecked, (dialog, which, isChecked) -> dialogChecked[which] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedScopeIds.clear();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dialogChecked.length; i++) {
                if (dialogChecked[i]) {
                    selectedScopeIds.add(dialogIds[i]);
                    sb.append(dialogNames[i]).append(", ");
                }
            }
            tvSelectedScope.setText(sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "None selected");
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void fetchMetadata() {
        String url = ApiConstants.BASE_URL + "get_coupon_metadata.php";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONObject data = json.getJSONObject("data");
                            JSONArray cats = data.getJSONArray("categories");
                            for (int i=0; i<cats.length(); i++) allCategories.add(new ScopeItem(cats.getJSONObject(i).getString("category_id"), cats.getJSONObject(i).getString("category_name")));

                            JSONArray items = data.getJSONArray("items");
                            for (int i=0; i<items.length(); i++) allItems.add(new ScopeItem(items.getJSONObject(i).getString("item_id"), items.getJSONObject(i).getString("item_name")));

                            JSONArray pkgs = data.getJSONArray("packages");
                            for (int i=0; i<pkgs.length(); i++) allPackages.add(new ScopeItem(pkgs.getJSONObject(i).getString("package_id"), pkgs.getJSONObject(i).getString("package_name")));
                        }
                    } catch (Exception e) {}
                }, error -> {}
        );
        Volley.newRequestQueue(this).add(request);
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
            if(applyId == R.id.rbCategory) applyStr = "category";
            else if(applyId == R.id.rbItem) applyStr = "item";
            else if(applyId == R.id.rbPackage) applyStr = "package";
            json.put("applies_to", applyStr);

            JSONArray ids = new JSONArray();
            for(String id : selectedScopeIds) ids.put(Integer.parseInt(id));
            json.put("selected_ids", ids);

            if(cbMinSpend.isChecked()) json.put("min_spend", etMinSpend.getText().toString());
            if(cbMaxDiscount.isChecked()) json.put("max_discount", etMaxDiscount.getText().toString());
            if(cbLimit.isChecked()) json.put("per_customer_limit", etLimit.getText().toString());

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

            String url = ApiConstants.BASE_URL + "create_coupon_api.php";
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        Toast.makeText(this, "Coupon Created!", Toast.LENGTH_SHORT).show();
                        tabMain.getTabAt(1).select();
                    },
                    error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );
            Volley.newRequestQueue(this).add(req);

        } catch (JSONException e) { e.printStackTrace(); }
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
}