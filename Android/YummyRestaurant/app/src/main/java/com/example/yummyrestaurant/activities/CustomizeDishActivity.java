package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.api.ApiService;
import com.example.yummyrestaurant.api.RetrofitClient;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Customization;
import com.example.yummyrestaurant.models.CustomizationOptionsResponse;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.models.OrderItemCustomization;
import com.example.yummyrestaurant.utils.CartManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 菜品自訂 Activity - 支持多選項、多選、實時價格計算
 * - 動態生成UI：RadioGroup（單選）/ CheckBox（多選）
 * - 實時計算總價格（基礎 + 額外費用）
 * - 支持多個自訂選項分組
 * - 驗證必填項和多選上限
 */
public class CustomizeDishActivity extends AppCompatActivity {

    private static final String TAG = "CustomizeDishActivity";

    public static final String EXTRA_MENU_ITEM = "menuItem";
    public static final String EXTRA_QUANTITY = "quantity";

    private LinearLayout optionsContainer;
    private EditText notesEditText;
    private Button saveBtn;
    private ProgressBar loadingSpinner;
    private TextView basePriceText;
    private TextView addonsCostText;
    private TextView totalPriceText;

    private MenuItem menuItem;
    private int quantity = 1;
    private double basePrice = 0;
    private List<CustomizationOptionsResponse.CustomizationOptionDetail> customizationOptions = new ArrayList<>();
    private Map<Integer, RadioGroup> radioGroupMap = new HashMap<>();  // optionId -> RadioGroup
    private Map<Integer, List<CheckBox>> checkboxGroupMap = new HashMap<>();  // optionId -> CheckBox List

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_dish);

        optionsContainer = findViewById(R.id.optionsContainer);
        notesEditText = findViewById(R.id.notesEditText);
        saveBtn = findViewById(R.id.saveCustomizationBtn);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        basePriceText = findViewById(R.id.basePriceText);
        addonsCostText = findViewById(R.id.addonsCostText);
        totalPriceText = findViewById(R.id.totalPriceText);

        // Load extras
        menuItem = (MenuItem) getIntent().getSerializableExtra(EXTRA_MENU_ITEM);
        quantity = getIntent().getIntExtra(EXTRA_QUANTITY, 1);

        if (menuItem == null) {
            Toast.makeText(this, "No dish data provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        basePrice = menuItem.getPrice();
        updatePriceDisplay();

        saveBtn.setEnabled(false);
        if (loadingSpinner != null) {
            loadingSpinner.setVisibility(android.view.View.VISIBLE);
        }

        fetchCustomizationOptions(menuItem.getId());
        saveBtn.setOnClickListener(v -> validateAndSaveCustomization());
    }

    /**
     * 從API獲取菜品的所有自訂選項
     */
    private void fetchCustomizationOptions(int itemId) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<CustomizationOptionsResponse> call = apiService.getCustomizationOptions(itemId);

        call.enqueue(new Callback<CustomizationOptionsResponse>() {
            @Override
            public void onResponse(Call<CustomizationOptionsResponse> call, Response<CustomizationOptionsResponse> response) {
                if (loadingSpinner != null) {
                    loadingSpinner.setVisibility(android.view.View.GONE);
                }

                if (!response.isSuccessful()) {
                    Log.e(TAG, "API response not successful: " + response.code());
                    setupUIWithDefaults();
                    return;
                }

                CustomizationOptionsResponse responseBody = response.body();
                if (responseBody == null || !responseBody.isSuccess()) {
                    Log.e(TAG, "API returned null or success=false");
                    setupUIWithDefaults();
                    return;
                }

                List<CustomizationOptionsResponse.CustomizationOptionDetail> options = responseBody.getOptions();
                if (options == null || options.isEmpty()) {
                    Log.w(TAG, "No customization options found for item " + itemId);
                    saveBtn.setEnabled(true);  // 沒有選項也能保存
                    return;
                }

                customizationOptions = options;
                Log.d(TAG, "Loaded " + customizationOptions.size() + " customization options");
                setupUI();
            }

            @Override
            public void onFailure(Call<CustomizationOptionsResponse> call, Throwable t) {
                if (loadingSpinner != null) {
                    loadingSpinner.setVisibility(android.view.View.GONE);
                }
                Log.e(TAG, "Failed to fetch customization options: " + t.getMessage());
                setupUIWithDefaults();
            }
        });
    }

    /**
     * 動態生成UI：為每個選項生成RadioGroup或CheckBox組
     */
    private void setupUI() {
        optionsContainer.removeAllViews();
        radioGroupMap.clear();
        checkboxGroupMap.clear();

        for (CustomizationOptionsResponse.CustomizationOptionDetail option : customizationOptions) {
            if (option.getChoices() == null || option.getChoices().isEmpty()) {
                Log.w(TAG, "Option " + option.getOptionName() + " has no choices, skipping");
                continue;
            }

            if (option.getMaxSelections() == 1) {
                addRadioGroupOption(option);
            } else if (option.getMaxSelections() > 1) {
                addCheckboxGroupOption(option);
            }
        }

        // 特殊要求輸入框
        addSpecialRequestsSection();
        
        saveBtn.setEnabled(true);
        Log.d(TAG, "UI setup completed with " + customizationOptions.size() + " options");
    }

    /**
     * 為單選選項添加RadioGroup
     */
    private void addRadioGroupOption(CustomizationOptionsResponse.CustomizationOptionDetail option) {
        LinearLayout groupLayout = new LinearLayout(this);
        groupLayout.setOrientation(LinearLayout.VERTICAL);
        groupLayout.setPadding(0, 16, 0, 16);

        // 選項標題 + 必填標記
        TextView title = new TextView(this);
        String titleText = option.getOptionName();
        if (option.isRequired() == 1) {
            titleText += " *";  // ✅ 添加紅色星號用於必填
        }
        title.setText(titleText);
        title.setTextSize(16);
        title.setTextStyle(android.graphics.Typeface.BOLD);
        if (option.isRequired() == 1) {
            title.setTextColor(android.graphics.Color.RED);  // ✅ 必填項顯示為紅色
        }
        groupLayout.addView(title);

        // RadioGroup
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        for (CustomizationOptionsResponse.ChoiceItem choice : option.getChoices()) {
            RadioButton rb = new RadioButton(this);
            rb.setTag(choice.getChoiceId());
            String displayText = choice.getChoiceName();
            if (choice.getAdditionalCost() > 0) {
                displayText += String.format(" (+₹%.2f)", choice.getAdditionalCost());
            }
            rb.setText(displayText);

            rb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    updateTotalPrice();
                }
            });

            radioGroup.addView(rb);
        }

        groupLayout.addView(radioGroup);
        optionsContainer.addView(groupLayout);
        radioGroupMap.put(option.getOptionId(), radioGroup);

        Log.d(TAG, "Added RadioGroup for option: " + option.getOptionName());
    }

    /**
     * 為多選選項添加CheckBox組
     */
    private void addCheckboxGroupOption(CustomizationOptionsResponse.CustomizationOptionDetail option) {
        LinearLayout groupLayout = new LinearLayout(this);
        groupLayout.setOrientation(LinearLayout.VERTICAL);
        groupLayout.setPadding(0, 16, 0, 16);

        // 選項標題 + 最大數量提示 + 必填標記
        TextView title = new TextView(this);
        String titleText = String.format("%s (Choose up to %d)", option.getOptionName(), option.getMaxSelections());
        if (option.isRequired() == 1) {
            titleText += " *";  // ✅ 添加紅色星號用於必填
        }
        title.setText(titleText);
        title.setTextSize(16);
        title.setTextStyle(android.graphics.Typeface.BOLD);
        if (option.isRequired() == 1) {
            title.setTextColor(android.graphics.Color.RED);  // ✅ 必填項顯示為紅色
        }
        groupLayout.addView(title);

        // CheckBox 組
        List<CheckBox> checkboxes = new ArrayList<>();
        for (CustomizationOptionsResponse.ChoiceItem choice : option.getChoices()) {
            CheckBox cb = new CheckBox(this);
            cb.setTag(choice.getChoiceId());
            String displayText = choice.getChoiceName();
            if (choice.getAdditionalCost() > 0) {
                displayText += String.format(" (+₹%.2f)", choice.getAdditionalCost());
            }
            cb.setText(displayText);

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int selectedCount = 0;
                for (CheckBox checkbox : checkboxes) {
                    if (checkbox.isChecked()) selectedCount++;
                }

                if (isChecked && selectedCount > option.getMaxSelections()) {
                    // 超過上限，取消選中
                    cb.setChecked(false);
                    Toast.makeText(CustomizeDishActivity.this,
                            "Maximum " + option.getMaxSelections() + " selection(s) allowed",
                            Toast.LENGTH_SHORT).show();
                } else {
                    updateTotalPrice();
                }
            });

            checkboxes.add(cb);
            groupLayout.addView(cb);
        }

        optionsContainer.addView(groupLayout);
        checkboxGroupMap.put(option.getOptionId(), checkboxes);

        Log.d(TAG, "Added CheckBox group for option: " + option.getOptionName());
    }

    /**
     * 添加特殊要求部分
     */
    private void addSpecialRequestsSection() {
        LinearLayout sectionLayout = new LinearLayout(this);
        sectionLayout.setOrientation(LinearLayout.VERTICAL);
        sectionLayout.setPadding(0, 16, 0, 0);

        TextView label = new TextView(this);
        label.setText("Special Instructions");
        label.setTextSize(16);
        label.setTextStyle(android.graphics.Typeface.BOLD);
        label.setPadding(0, 0, 0, 8);
        sectionLayout.addView(label);

        notesEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        sectionLayout.addView(notesEditText);

        optionsContainer.addView(sectionLayout);
    }

    /**
     * 實時計算並更新顯示的總價格
     */
    private void updateTotalPrice() {
        double additionalCost = 0;

        // 計算RadioGroup選中項的額外費用
        for (Map.Entry<Integer, RadioGroup> entry : radioGroupMap.entrySet()) {
            RadioGroup rg = entry.getValue();
            int checkedId = rg.getCheckedRadioButtonId();
            if (checkedId != -1) {
                RadioButton rb = findViewById(checkedId);
                if (rb != null) {
                    additionalCost += getChoiceCost((Integer) rb.getTag());
                }
            }
        }

        // 計算CheckBox選中項的額外費用
        for (Map.Entry<Integer, List<CheckBox>> entry : checkboxGroupMap.entrySet()) {
            List<CheckBox> checkboxes = entry.getValue();
            for (CheckBox cb : checkboxes) {
                if (cb.isChecked()) {
                    additionalCost += getChoiceCost((Integer) cb.getTag());
                }
            }
        }

        // 更新UI顯示
        addonsCostText.setText(String.format("+₹%.2f", additionalCost));
        double totalPrice = basePrice + additionalCost;
        totalPriceText.setText(String.format("₹%.2f", totalPrice));
        basePriceText.setText(String.format("₹%.2f", basePrice));

        Log.d(TAG, "Price updated: base=" + basePrice + ", addons=" + additionalCost + ", total=" + totalPrice);
    }

    /**
     * 根據choiceId查詢額外費用
     */
    private double getChoiceCost(Integer choiceId) {
        for (CustomizationOptionsResponse.CustomizationOptionDetail option : customizationOptions) {
            if (option.getChoices() != null) {
                for (CustomizationOptionsResponse.ChoiceItem choice : option.getChoices()) {
                    if (choice.getChoiceId() == choiceId) {
                        return choice.getAdditionalCost();
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 如果API失敗，允許無自訂選項保存
     */
    private void setupUIWithDefaults() {
        addSpecialRequestsSection();
        saveBtn.setEnabled(true);
        Log.i(TAG, "Using default setup (no customization options)");
    }

    /**
     * 驗證並保存自訂選項到購物車
     */
    private void validateAndSaveCustomization() {
        List<OrderItemCustomization> customizationDetails = new ArrayList<>();
        double totalAdditionalCost = 0;

        // ✅ 改變：驗證所有必填項
        for (CustomizationOptionsResponse.CustomizationOptionDetail option : customizationOptions) {
            if (option.isRequired() != 1) continue;  // 跳過非必填項

            int optionId = option.getOptionId();
            boolean hasSelection = false;

            // 檢查RadioGroup選擇
            if (radioGroupMap.containsKey(optionId)) {
                RadioGroup rg = radioGroupMap.get(optionId);
                if (rg.getCheckedRadioButtonId() != -1) {
                    hasSelection = true;
                }
            }

            // 檢查CheckBox選擇
            if (checkboxGroupMap.containsKey(optionId)) {
                List<CheckBox> checkboxes = checkboxGroupMap.get(optionId);
                for (CheckBox cb : checkboxes) {
                    if (cb.isChecked()) {
                        hasSelection = true;
                        break;
                    }
                }
            }

            if (!hasSelection) {
                Toast.makeText(this, "Required: " + option.getOptionName(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 收集RadioGroup選擇
        for (Map.Entry<Integer, RadioGroup> entry : radioGroupMap.entrySet()) {
            int optionId = entry.getKey();
            RadioGroup rg = entry.getValue();
            int checkedId = rg.getCheckedRadioButtonId();

            if (checkedId == -1) {
                // 非必填項可以不選
                continue;
            }

            RadioButton rb = findViewById(checkedId);
            if (rb != null) {
                String choiceName = rb.getText().toString();
                // 移除額外費用部分，只保留選項名稱
                if (choiceName.contains(" (+₹")) {
                    choiceName = choiceName.substring(0, choiceName.indexOf(" (+₹"));
                }

                OrderItemCustomization custom = new OrderItemCustomization(optionId, getOptionName(optionId));
                custom.setSelectedChoices(new ArrayList<String>() {{ add(choiceName); }});
                custom.setAdditionalCost(getChoiceCost((Integer) rb.getTag()));
                customizationDetails.add(custom);

                totalAdditionalCost += custom.getAdditionalCost();
            }
        }        // 收集CheckBox選擇
        for (Map.Entry<Integer, List<CheckBox>> entry : checkboxGroupMap.entrySet()) {
            int optionId = entry.getKey();
            List<CheckBox> checkboxes = entry.getValue();
            List<String> selectedChoices = new ArrayList<>();

            for (CheckBox cb : checkboxes) {
                if (cb.isChecked()) {
                    String choiceName = cb.getText().toString();
                    if (choiceName.contains(" (+₹")) {
                        choiceName = choiceName.substring(0, choiceName.indexOf(" (+₹"));
                    }
                    selectedChoices.add(choiceName);
                    totalAdditionalCost += getChoiceCost((Integer) cb.getTag());
                }
            }

            if (!selectedChoices.isEmpty()) {
                OrderItemCustomization custom = new OrderItemCustomization(optionId, getOptionName(optionId));
                custom.setSelectedChoices(selectedChoices);
                custom.setAdditionalCost(totalAdditionalCost);
                customizationDetails.add(custom);
            }
        }

        // 收集特殊要求
        String notes = notesEditText.getText().toString().trim();
        if (notes.length() > 500) {
            Toast.makeText(this, "Special instructions are too long (max 500 characters)", Toast.LENGTH_SHORT).show();
            return;
        }

        // 創建Customization對象
        Customization customization = new Customization();
        customization.setExtraNotes(notes);
        customization.setCustomizationDetails(customizationDetails);

        // 添加到購物車
        CartItem cartItem = new CartItem(menuItem, customization);
        Integer currentQty = CartManager.getItemQuantity(cartItem);
        int newQty = (currentQty != null ? currentQty : 0) + quantity;
        CartManager.updateQuantity(cartItem, newQty);

        Toast.makeText(this, quantity + " × " + menuItem.getName() + " added to cart", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 根據optionId查詢optionName
     */
    private String getOptionName(int optionId) {
        for (CustomizationOptionsResponse.CustomizationOptionDetail option : customizationOptions) {
            if (option.getOptionId() == optionId) {
                return option.getOptionName();
            }
        }
        return "Unknown";
    }

    /**
     * 更新價格顯示（初始化）
     */
    private void updatePriceDisplay() {
        basePriceText.setText(String.format("₹%.2f", basePrice));
        addonsCostText.setText("+₹0");
        totalPriceText.setText(String.format("₹%.2f", basePrice));
    }
}
