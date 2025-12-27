package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.CouponApiService;
import com.example.yummyrestaurant.api.OrderApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.RoleManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TempPaymentActivity extends AppCompatActivity {

    private static final String TAG = "TempPaymentActivity";

    private ProgressBar loadingSpinner;
    private ImageView successIcon;
    private Button confirmButton;
    private TextView amountText;

    private String dishJson;
    private final List<Map<String, Object>> items = new ArrayList<>();
    private final List<Map<String, Object>> itemsForDisplay = new ArrayList<>();

    private int finalAmount;
    private int subtotalAmount; // ✅ keep original subtotal
    private ArrayList<Coupon> selectedCoupons;
    private HashMap<Integer, Integer> couponQuantities; // ✅ keep coupon quantities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_payment);

        loadingSpinner = findViewById(R.id.loadingSpinner);
        successIcon = findViewById(R.id.successIcon);
        confirmButton = findViewById(R.id.confirmButton1);
        amountText = findViewById(R.id.amountText);

        // ✅ Read values passed from CartActivity
        subtotalAmount = getIntent().getIntExtra("subtotalAmount", 0);
        int totalAmount = getIntent().getIntExtra("totalAmount", 0);
        selectedCoupons = getIntent().getParcelableArrayListExtra("selectedCoupons");
        couponQuantities = (HashMap<Integer, Integer>) getIntent().getSerializableExtra("couponQuantities");

        finalAmount = Math.max(0, totalAmount);

        Log.d(TAG, "Received subtotalAmount=" + subtotalAmount +
                ", totalAmount=" + totalAmount +
                ", couponQuantities=" + (couponQuantities != null ? new Gson().toJson(couponQuantities) : "null"));

        String discountLabel = (selectedCoupons != null && !selectedCoupons.isEmpty())
                ? " (after discounts)" : "";
        amountText.setText(String.format(
                Locale.getDefault(),
                "Total: HK$%.2f%s",
                finalAmount / 100.0,
                discountLabel
        ));

        confirmButton.setOnClickListener(v -> {
            confirmButton.setEnabled(false);
            loadingSpinner.setVisibility(View.VISIBLE);
            saveOrderDirectly();
        });
    }

    private List<Integer> extractCouponIds(List<Coupon> coupons) {
        List<Integer> ids = new ArrayList<>();
        for (Coupon c : coupons) {
            ids.add(c.getCouponId());
        }
        return ids;
    }

    private void saveOrderDirectly() {
        Log.d(TAG, "saveOrderDirectly: preparing payload");

        boolean isStaff = RoleManager.isStaff();
        String userId = RoleManager.getUserId();
        int customerId = isStaff ? 0 : Integer.parseInt(userId);

        // Build order header map
        Map<String, Object> orderHeader = new HashMap<>();
        orderHeader.put("cid", customerId);
        orderHeader.put("ostatus", 1);
        orderHeader.put("odate", System.currentTimeMillis());
        orderHeader.put("orderRef", "temp_order_" + System.currentTimeMillis());
        if (isStaff) {
            orderHeader.put("sid", Integer.parseInt(RoleManager.getUserId()));
            orderHeader.put("table_number", RoleManager.getAssignedTable());
        } else {
            orderHeader.put("sid", null);
            orderHeader.put("table_number", "not chosen");
        }

        if (selectedCoupons != null && !selectedCoupons.isEmpty()) {
            orderHeader.put("coupon_ids", extractCouponIds(selectedCoupons));
        }

        // Build order_items array
        items.clear();
        itemsForDisplay.clear();
        for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
            CartItem cartItem = entry.getKey();
            MenuItem menuItem = cartItem.getMenuItem();
            int qty = entry.getValue();

            Map<String, Object> item = new HashMap<>();
            item.put("item_id", menuItem.getId());
            item.put("qty", qty);
            item.put("name", menuItem.getName());
            
            // ✅ 新增：包含 category 字段，以識別套餐項目
            if (menuItem.getCategory() != null) {
                item.put("category", menuItem.getCategory());
                
                // ✅ 如果是套餐，還要發送套餐內的菜品
                if ("PACKAGE".equals(menuItem.getCategory())) {
                    Map<Integer, Map<String, Object>> packageDetails = CartManager.getPackageDetails();
                    if (packageDetails != null && packageDetails.containsKey(menuItem.getId())) {
                        Map<String, Object> pkgDetail = packageDetails.get(menuItem.getId());
                        @SuppressWarnings("unchecked")
                        List<com.example.yummyrestaurant.models.MenuItem> packageItems = 
                            (List<com.example.yummyrestaurant.models.MenuItem>) pkgDetail.get("items");
                        
                        if (packageItems != null && !packageItems.isEmpty()) {
                            List<Map<String, Object>> packageItemsList = new ArrayList<>();
                            for (com.example.yummyrestaurant.models.MenuItem pkgItem : packageItems) {
                                Map<String, Object> pkgItemMap = new HashMap<>();
                                pkgItemMap.put("id", pkgItem.getId());
                                pkgItemMap.put("name", pkgItem.getName());
                                pkgItemMap.put("qty", 1);
                                
                                // ✅ v4.6: Add customizations for package items
                                if (pkgItem.getCustomizations() != null && !pkgItem.getCustomizations().isEmpty()) {
                                    List<Map<String, Object>> customizationsList = new ArrayList<>();
                                    for (com.example.yummyrestaurant.models.OrderItemCustomization custom : pkgItem.getCustomizations()) {
                                        Map<String, Object> customMap = new HashMap<>();
                                        customMap.put("option_id", custom.getOptionId());
                                        customMap.put("group_id", custom.getGroupId());
                                        
                                        // Add selected value IDs
                                        if (custom.getSelectedValueIds() != null && !custom.getSelectedValueIds().isEmpty()) {
                                            customMap.put("selected_value_ids", custom.getSelectedValueIds());
                                        }
                                        
                                        // Add selected values (for display)
                                        if (custom.getSelectedValues() != null && !custom.getSelectedValues().isEmpty()) {
                                            customMap.put("selected_values", custom.getSelectedValues());
                                        }
                                        
                                        // Add text value if present
                                        if (custom.getTextValue() != null && !custom.getTextValue().isEmpty()) {
                                            customMap.put("text_value", custom.getTextValue());
                                        }
                                        
                                        customizationsList.add(customMap);
                                    }
                                    pkgItemMap.put("customizations", customizationsList);
                                    Log.d(TAG, "Added " + customizationsList.size() + " customizations for package item: " + pkgItem.getName());
                                }
                                
                                packageItemsList.add(pkgItemMap);
                            }
                            item.put("packageItems", packageItemsList);
                            Log.d(TAG, "Added " + packageItemsList.size() + " items to package");
                        }
                    }
                }
            }

            // ✅ 改變：使用完整的customizationDetails結構
            if (cartItem.getCustomization() != null) {
                com.example.yummyrestaurant.models.Customization customization = cartItem.getCustomization();
                Map<String, Object> customizationMap = new HashMap<>();
                List<Map<String, Object>> customizationDetails = new ArrayList<>();

                Log.d(TAG, "Processing customization for item: " + menuItem.getName() + 
                           ", has details: " + (customization.getCustomizationDetails() != null ? 
                           customization.getCustomizationDetails().size() : 0));

                // 🔴 DIAGNOSTIC: 列印第一個 detail 對象的完整信息
                if (customization.getCustomizationDetails() != null && 
                    !customization.getCustomizationDetails().isEmpty()) {
                    com.example.yummyrestaurant.models.OrderItemCustomization firstDetail = 
                        customization.getCustomizationDetails().get(0);
                    Log.d(TAG, "🔍 FIRST DETAIL DEBUG:");
                    Log.d(TAG, "   Object: " + firstDetail.toString());
                    Log.d(TAG, "   selectedChoices field: " + firstDetail.getSelectedChoices());
                    Log.d(TAG, "   selectedChoices class: " + (firstDetail.getSelectedChoices() != null ? firstDetail.getSelectedChoices().getClass().getName() : "null"));
                    Log.d(TAG, "   choiceNames field: " + firstDetail.getChoiceNames());
                }

                // 收集所有customizationDetails
                if (customization.getCustomizationDetails() != null && 
                    !customization.getCustomizationDetails().isEmpty()) {
                    
                    for (com.example.yummyrestaurant.models.OrderItemCustomization detail : 
                         customization.getCustomizationDetails()) {
                        
                        // 🔴 強制添加 selected_choices - 使用 LinkedHashMap 保證順序且不被過濾
                        Map<String, Object> detailMap = new java.util.LinkedHashMap<>();
                        
                        // 🔴 CRITICAL: 構造選擇列表 - 直接使用字符串而不是 List，避免序列化問題
                        String selectedChoicesJson = "[]";  // 默認空列表
                        
                        // 優先使用 selectedChoices
                        if (detail.getSelectedChoices() != null && !detail.getSelectedChoices().isEmpty()) {
                            // 🔴 WORKAROUND: 轉換為新的 ArrayList 以避免匿名類序列化問題
                            List<String> normalizedList = new ArrayList<>(detail.getSelectedChoices());
                            selectedChoicesJson = new Gson().toJson(normalizedList);
                            Log.d(TAG, "  ✅ Using selectedChoices: " + selectedChoicesJson);
                        } else if (detail.getChoiceNames() != null && !detail.getChoiceNames().isEmpty()) {
                            // 備用方案：使用 choiceNames (逗號分隔字符串)
                            List<String> choiceList = Arrays.asList(detail.getChoiceNames().split(",\\s*"));
                            selectedChoicesJson = new Gson().toJson(choiceList);
                            Log.d(TAG, "  ✅ Converted choiceNames to selectedChoices: " + selectedChoicesJson);
                        } else {
                            Log.d(TAG, "  ⚠️ No selected_choices or choiceNames found! Using empty list");
                        }
                        
                        // 🔴 MANDATORY FIELDS - 使用原始值不通過 Gson
                        detailMap.put("option_id", detail.getOptionId());
                        detailMap.put("option_name", detail.getOptionName());
                        detailMap.put("group_id", detail.getGroupId());
                        detailMap.put("group_name", detail.getGroupName());
                        detailMap.put("additional_cost", detail.getAdditionalCost());
                        
                        // ⚠️ 不添加 selected_choices 到 detailMap，而是在最後的 JSON 中手動添加
                        if (detail.getTextValue() != null && !detail.getTextValue().isEmpty()) {
                            detailMap.put("text_value", detail.getTextValue());
                            Log.d(TAG, "  ✅ Added text_value: " + detail.getTextValue());
                        }
                        
                        Log.d(TAG, "  📝 Detail map keys: " + detailMap.keySet().toString());
                        
                        // 🔴 WORKAROUND: 手動構造 JSON 以確保 selected_choices 被正確包含
                        StringBuilder detailJsonBuilder = new StringBuilder("{");
                        detailJsonBuilder.append("\"option_id\":").append(detail.getOptionId()).append(",");
                        detailJsonBuilder.append("\"option_name\":\"").append(detail.getOptionName()).append("\",");
                        detailJsonBuilder.append("\"group_id\":").append(detail.getGroupId()).append(",");
                        detailJsonBuilder.append("\"group_name\":\"").append(detail.getGroupName()).append("\",");
                        detailJsonBuilder.append("\"selected_choices\":").append(selectedChoicesJson).append(",");
                        detailJsonBuilder.append("\"additional_cost\":").append(detail.getAdditionalCost());
                        if (detail.getTextValue() != null && !detail.getTextValue().isEmpty()) {
                            detailJsonBuilder.append(",\"text_value\":\"").append(detail.getTextValue()).append("\"");
                        }
                        detailJsonBuilder.append("}");
                        
                        String detailJsonString = detailJsonBuilder.toString();
                        Log.d(TAG, "  📝 Detail map JSON (手動構造): " + detailJsonString);
                        
                        // 驗證 selected_choices 確實在 JSON 中
                        if (!detailJsonString.contains("\"selected_choices\"")) {
                            Log.e(TAG, "  🔥 ERROR: selected_choices NOT in JSON! This is a critical bug!");
                        }
                        
                        // 轉換回 Map 以保持兼容性
                        Map<String, Object> jsonMap = new Gson().fromJson(detailJsonString, Map.class);
                        customizationDetails.add(jsonMap);
                    }
                }

                if (!customizationDetails.isEmpty()) {
                    customizationMap.put("customization_details", customizationDetails);
                }

                // 添加特殊要求
                String notes = customization.getExtraNotes();
                if (notes != null && !notes.isEmpty()) {
                    customizationMap.put("extra_notes", notes);
                    Log.d(TAG, "  - Added extra notes: " + notes);
                }

                if (!customizationMap.isEmpty()) {
                    item.put("customization", customizationMap);
                    Log.d(TAG, "✅ Added complete customization structure to item with " + 
                           customizationDetails.size() + " details");
                }
            } else {
                Log.d(TAG, "No customization for item: " + menuItem.getName());
            }

            items.add(item);

            Map<String, Object> display = new HashMap<>();
            display.put("item_id", menuItem.getId());
            display.put("qty", qty);
            display.put("dish_name", menuItem.getName());
            display.put("dish_price", menuItem.getPrice());
            if (cartItem.getCustomization() != null) {
                display.put("spice_level", cartItem.getCustomization().getSpiceLevel());
                display.put("extra_notes", cartItem.getCustomization().getExtraNotes());
                
                // ✅ 新增：添加完整的自訂項詳情到顯示
                if (cartItem.getCustomization().getCustomizationDetails() != null && 
                    !cartItem.getCustomization().getCustomizationDetails().isEmpty()) {
                    
                    List<Map<String, Object>> customDetails = new ArrayList<>();
                    Log.d(TAG, "Processing " + cartItem.getCustomization().getCustomizationDetails().size() + " customization details");
                    
                    for (com.example.yummyrestaurant.models.OrderItemCustomization detail : 
                         cartItem.getCustomization().getCustomizationDetails()) {
                        
                        Log.d(TAG, "Detail: optionId=" + detail.getOptionId() + 
                                   ", optionName=" + detail.getOptionName() + 
                                   ", selectedChoices=" + detail.getSelectedChoices() + 
                                   ", choiceNames=" + detail.getChoiceNames() + 
                                   ", textValue=" + detail.getTextValue());
                        
                        Map<String, Object> detailMap = new HashMap<>();
                        detailMap.put("option_name", detail.getOptionName());
                        
                        // ✅ 改變：使用 getSelectedChoices() 而不是 getChoiceNames()
                        if (detail.getSelectedChoices() != null && !detail.getSelectedChoices().isEmpty()) {
                            String joinedChoices = String.join(",", detail.getSelectedChoices());
                            detailMap.put("choice_names", joinedChoices);
                            Log.d(TAG, "  ✅ Using selectedChoices: " + joinedChoices);
                        } else if (detail.getChoiceNames() != null && !detail.getChoiceNames().isEmpty()) {
                            detailMap.put("choice_names", detail.getChoiceNames());
                            Log.d(TAG, "  ✅ Using choiceNames: " + detail.getChoiceNames());
                        } else {
                            Log.d(TAG, "  ❌ No choices found!");
                        }
                        
                        detailMap.put("text_value", detail.getTextValue() != null ? detail.getTextValue() : "");
                        customDetails.add(detailMap);
                    }
                    display.put("customization_details", customDetails);
                    Log.d(TAG, "✅ Added " + customDetails.size() + " customization details to display");
                } else {
                    Log.d(TAG, "❌ No customization details or empty");
                }
            }
            itemsForDisplay.add(display);
            Log.d(TAG, "Display object: " + new Gson().toJson(display));
        }

        orderHeader.put("items", items);
        orderHeader.put("total_amount", finalAmount);
        dishJson = new Gson().toJson(itemsForDisplay);
        
        // 📊 詳細日誌：記錄發送到後端的完整結構
        Log.d(TAG, "📤 SENDING TO BACKEND:");
        Log.d(TAG, "   Items count: " + items.size());
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            Log.d(TAG, "   Item " + i + ": id=" + item.get("item_id") + 
                       ", qty=" + item.get("qty") + 
                       ", name=" + item.get("name") +
                       ", has_customization=" + (item.containsKey("customization") ? "YES" : "NO"));
            
            if (item.containsKey("customization")) {
                Map<String, Object> customization = (Map<String, Object>) item.get("customization");
                Log.d(TAG, "      Customization keys: " + customization.keySet().toString());
                
                if (customization.containsKey("customization_details")) {
                    List<?> details = (List<?>) customization.get("customization_details");
                    Log.d(TAG, "      ✅ customization_details: " + details.size() + " items");
                    for (int j = 0; j < details.size(); j++) {
                        Object detail = details.get(j);
                        Log.d(TAG, "        Detail " + j + ": " + new Gson().toJson(detail));
                    }
                } else {
                    Log.d(TAG, "      ❌ NO customization_details key");
                }
            }
        }
        
        // 🔴 CRITICAL: Use Gson with setSerializeNulls() to ensure all fields are included
        Gson gsonForSerialization = new com.google.gson.GsonBuilder()
            .serializeNulls()
            .create();
        Log.d(TAG, "📦 Complete orderHeader JSON: " + gsonForSerialization.toJson(orderHeader));
        
        // Call the backend via Retrofit
        OrderApiService service = RetrofitClient.getClient(this).create(OrderApiService.class);
        Call<ResponseBody> call = service.saveOrderDirect(orderHeader);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingSpinner.setVisibility(View.GONE);
                confirmButton.setEnabled(true);

                if (response.isSuccessful()) {
                    try {
                        String body = response.body() != null ? response.body().string() : "";
                        Log.i(TAG, "Order saved directly. Server response: " + body);
                        successIcon.setVisibility(View.VISIBLE);
                        Toast.makeText(TempPaymentActivity.this, "Order saved!", Toast.LENGTH_SHORT).show();

                        // ✅ Only mark coupon as used here, after order save succeeds
                        if (selectedCoupons != null && !selectedCoupons.isEmpty()) {
                            markCouponsAsUsed(customerId, selectedCoupons);
                        }

                        // Navigate to confirmation
                        Intent intent = new Intent(TempPaymentActivity.this, OrderConfirmationActivity.class);
                        intent.putExtra("customerId", String.valueOf(customerId));
                        intent.putExtra("subtotalAmount", subtotalAmount); // ✅ forward original subtotal
                        intent.putExtra("totalAmount", finalAmount);
                        intent.putExtra("itemCount", items.size());
                        intent.putExtra("dishJson", dishJson);
                        if (selectedCoupons != null && !selectedCoupons.isEmpty()) {
                            intent.putParcelableArrayListExtra("selectedCoupons", selectedCoupons);
                        }
                        if (couponQuantities != null) {
                            intent.putExtra("couponQuantities", couponQuantities); // ✅ forward quantities
                        }
                        startActivity(intent);

                        finish();
                        CartManager.clearCart();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read response body", e);
                        Toast.makeText(TempPaymentActivity.this, "Order saved but failed to read response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "Failed to save order. Code=" + response.code() + ", error=" + err);
                        Toast.makeText(TempPaymentActivity.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading errorBody", e);
                        Toast.makeText(TempPaymentActivity.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                confirmButton.setEnabled(true);
                Log.e(TAG, "saveOrderDirectly onFailure: " + t.getMessage(), t);
                Toast.makeText(TempPaymentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markCouponsAsUsed(int customerId, List<Coupon> coupons) {
        int orderTotal = CartManager.getTotalAmountInCents();
        ArrayList<Integer> menuItemIds = new ArrayList<>();
        for (CartItem item : CartManager.getCartItems().keySet()) {
            Integer id = item.getMenuItemId();
            if (id != null) menuItemIds.add(id);
        }

        // Build couponQuantities map with proper keys: coupon_quantities[ID]
        Map<String, Integer> couponQuantitiesMap = new HashMap<>();
        for (Coupon c : coupons) {
            couponQuantitiesMap.put("coupon_quantities[" + c.getCouponId() + "]", c.getQuantity());
        }

        // Get order type dynamically from CartManager
        String orderType = CartManager.getOrderType();
        Log.i(TAG, "Using orderType=" + orderType);

        CouponApiService service = RetrofitClient.getClient(this).create(CouponApiService.class);
        service.useCoupons(customerId, orderTotal, orderType, couponQuantitiesMap, menuItemIds)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Log.i(TAG, "Coupons marked as used: " + new Gson().toJson(couponQuantitiesMap));
                            Log.i(TAG, "customerId=" + customerId);
                            Log.i(TAG, "orderTotal=" + orderTotal);
                            Log.i(TAG, "orderType=" + orderType);
                            Log.i(TAG, "eligibleItemIds=" + menuItemIds);
                            Log.i(TAG, "Coupon use response: " + new Gson().toJson(response.body()));
                        } else {
                            Log.w(TAG, "Failed to mark coupons: " + new Gson().toJson(couponQuantitiesMap));
                            if (response.errorBody() != null) {
                                try {
                                    Log.w(TAG, "Error body: " + response.errorBody().string());
                                } catch (IOException e) {
                                    Log.e(TAG, "Error reading errorBody", e);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Log.e(TAG, "Coupon use API error: " + t.getMessage(), t);
                    }
                });
    }
}