package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 菜品自訂 Activity
 * - 從資料庫動態加載自訂選項
 * - 支持多個自訂選項（辛辣度、特殊要求等）
 * - 驗證必填項
 * - 保存到購物車
 */
public class CustomizeDishActivity extends AppCompatActivity {

    private static final String TAG = "CustomizeDishActivity";

    public static final String EXTRA_MENU_ITEM = "menuItem";
    public static final String EXTRA_QUANTITY = "quantity";

    private Spinner spiceSpinner;
    private EditText notesEditText;
    private Button saveBtn;
    private ProgressBar loadingSpinner;
    private TextView optionLabel;

    private MenuItem menuItem;
    private int quantity = 1;
    private List<CustomizationOptionsResponse.CustomizationOptionDetail> customizationOptions = new ArrayList<>();
    private CustomizationOptionsResponse.CustomizationOptionDetail primaryOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_dish);

        spiceSpinner = findViewById(R.id.spiceSpinner);
        notesEditText = findViewById(R.id.notesEditText);
        saveBtn = findViewById(R.id.saveCustomizationBtn);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        optionLabel = findViewById(R.id.optionLabel);

        // Load extras
        menuItem = (MenuItem) getIntent().getSerializableExtra(EXTRA_MENU_ITEM);
        quantity = getIntent().getIntExtra(EXTRA_QUANTITY, 1);

        // Safety check
        if (menuItem == null) {
            Toast.makeText(this, "No dish data provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        saveBtn.setEnabled(false);
        if (loadingSpinner != null) {
            loadingSpinner.setVisibility(android.view.View.VISIBLE);
        }

        // Load customization options from database
        fetchCustomizationOptions(menuItem.getId());

        saveBtn.setOnClickListener(v -> validateAndSaveCustomization());
    }

    /**
     * 從資料庫獲取菜品的自訂選項
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

                Log.d(TAG, "fetchCustomizationOptions onResponse: response code = " + response.code());
                Log.d(TAG, "fetchCustomizationOptions onResponse: isSuccessful = " + response.isSuccessful());

                if (!response.isSuccessful()) {
                    Log.e(TAG, "API response not successful: " + response.code());
                    setupUIWithDefaults();
                    return;
                }

                CustomizationOptionsResponse responseBody = response.body();
                if (responseBody == null) {
                    Log.e(TAG, "response.body() is null");
                    setupUIWithDefaults();
                    return;
                }

                Log.d(TAG, "response.isSuccess() = " + responseBody.isSuccess());
                
                if (!responseBody.isSuccess()) {
                    Log.e(TAG, "API success=false, error: " + responseBody.getError());
                    setupUIWithDefaults();
                    return;
                }

                List<CustomizationOptionsResponse.CustomizationOptionDetail> options = responseBody.getOptions();
                Log.d(TAG, "response.getOptions() returned: " + (options == null ? "null" : "list with " + options.size() + " items"));
                
                if (options == null) {
                    Log.w(TAG, "options list is null, using defaults");
                    setupUIWithDefaults();
                    return;
                }

                customizationOptions = options;
                Log.d(TAG, "Loaded " + customizationOptions.size() + " customization options from API");
                Log.d(TAG, "Full response: " + responseBody.toString());
                
                setupUI();
            }

            @Override
            public void onFailure(Call<CustomizationOptionsResponse> call, Throwable t) {
                if (loadingSpinner != null) {
                    loadingSpinner.setVisibility(android.view.View.GONE);
                }
                Log.e(TAG, "Failed to fetch customization options: " + t.getMessage());
                t.printStackTrace();
                setupUIWithDefaults();
            }
        });
    }

    /**
     * 使用資料庫中的選項設置 UI
     * 動態支持任何類型的選項（Spice Level, Temperature, Sugar Level 等）
     */
    private void setupUI() {
        if (customizationOptions == null || customizationOptions.isEmpty()) {
            Log.w(TAG, "customizationOptions is null or empty");
            setupUIWithDefaults();
            return;
        }

        Log.d(TAG, "setupUI: customizationOptions size = " + customizationOptions.size());
        
        // 找到第一個 single_choice 或 multi_choice 類型的選項來填充 Spinner
        primaryOption = null;
        for (int i = 0; i < customizationOptions.size(); i++) {
            CustomizationOptionsResponse.CustomizationOptionDetail option = customizationOptions.get(i);
            Log.d(TAG, "Option " + i + ": option_name='" + option.getOptionName() + "', type=" + option.getOptionType());
            
            if (option != null && option.getOptionType() != null && 
                (option.getOptionType().equals("single_choice") || option.getOptionType().equals("multi_choice"))) {
                primaryOption = option;
                Log.d(TAG, "Found primary option: " + option.getOptionName());
                break;
            }
        }

        if (primaryOption != null && primaryOption.getChoices() != null && !primaryOption.getChoices().isEmpty()) {
            Log.d(TAG, "Setting up spinner with option: " + primaryOption.getOptionName() + ", choices count: " + primaryOption.getChoices().size());
            setupSpiceSpinner(primaryOption);
            
            // 動態設置標籤文本
            if (optionLabel != null) {
                optionLabel.setText("Choose " + primaryOption.getOptionName() + ":");
            }
            
            saveBtn.setEnabled(true);
        } else {
            Log.w(TAG, "No suitable option found, using defaults");
            setupUIWithDefaults();
        }
    }

    /**
     * 設置選項 Spinner（支持任何類型的選項）
     */
    private void setupSpiceSpinner(CustomizationOptionsResponse.CustomizationOptionDetail option) {
        List<String> optionValues = new ArrayList<>();
        optionValues.add("Select " + option.getOptionName());

        if (option.getChoices() != null && !option.getChoices().isEmpty()) {
            for (CustomizationOptionsResponse.ChoiceItem choice : option.getChoices()) {
                if (choice != null && choice.getChoiceName() != null) {
                    optionValues.add(choice.getChoiceName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, optionValues);
        spiceSpinner.setAdapter(adapter);
        spiceSpinner.setSelection(0);

        Log.d(TAG, "Option spinner '" + option.getOptionName() + "' populated with " + optionValues.size() + " options");
    }

    /**
     * 如果 API 失敗，使用預設的選項
     */
    private void setupUIWithDefaults() {
        String[] spiceLevels = {"Select spice level", "Mild", "Medium", "Hot", "Numbing"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spiceLevels);
        spiceSpinner.setAdapter(adapter);
        spiceSpinner.setSelection(0);
        saveBtn.setEnabled(true);
        Log.i(TAG, "Using default spice levels (fallback)");
    }

    /**
     * 驗證自訂選項並添加到購物車
     */
    private void validateAndSaveCustomization() {
        ValidationResult validation = validateCustomizations();
        if (!validation.isValid) {
            Toast.makeText(this, validation.message, Toast.LENGTH_LONG).show();
            Log.w(TAG, "Validation failed: " + validation.message);
            return;
        }

        String chosenValue = spiceSpinner.getSelectedItem().toString();
        String notes = notesEditText.getText().toString().trim();
        List<OrderItemCustomization> customizationDetails = new ArrayList<>();

        // 添加主要選項到自訂列表
        if (primaryOption != null && !chosenValue.startsWith("Select") && !chosenValue.isEmpty()) {
            OrderItemCustomization customDetail = new OrderItemCustomization(
                primaryOption.getOptionId(), 
                primaryOption.getOptionName()
            );
            customDetail.setTextValue(chosenValue);
            customizationDetails.add(customDetail);
            Log.d(TAG, "Added customization: " + primaryOption.getOptionName() + " = " + chosenValue);
        }

        // 添加特殊要求（如果存在且有內容）
        if (notes != null && !notes.isEmpty()) {
            OrderItemCustomization notesDetail = new OrderItemCustomization(999, "Special Requests");
            notesDetail.setTextValue(notes);
            customizationDetails.add(notesDetail);
            Log.d(TAG, "Added special requests: " + notes);
        }

        Customization customization = new Customization(chosenValue, notes);
        customization.setCustomizationDetails(customizationDetails);

        CartItem cartItem = new CartItem(menuItem, customization);
        Integer qtyFromCart = CartManager.getItemQuantity(cartItem);
        int currentQty = (qtyFromCart != null) ? qtyFromCart : 0;
        CartManager.updateQuantity(cartItem, currentQty + quantity);

        showConfirmationMessage(chosenValue, notes);

        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 驗證自訂選項
     */
    private ValidationResult validateCustomizations() {
        String value = spiceSpinner.getSelectedItem().toString();

        if (value.startsWith("Select")) {
            return new ValidationResult(false, "Please select an option");
        }

        String notes = notesEditText.getText().toString().trim();
        if (notes.length() > 500) {
            return new ValidationResult(false, "Special requests are too long (max 500 characters)");
        }

        return new ValidationResult(true, "");
    }

    /**
     * 顯示確認信息
     */
    private void showConfirmationMessage(String value, String notes) {
        StringBuilder toastText = new StringBuilder();
        toastText.append(quantity).append(" × ").append(menuItem.getName());

        if (primaryOption != null && value != null && !value.isEmpty() && !value.startsWith("Select")) {
            toastText.append(" (").append(primaryOption.getOptionName()).append(": ").append(value).append(")");
        }

        if (notes != null && !notes.isEmpty()) {
            toastText.append(" • ").append(notes);
        }

        Toast.makeText(this, toastText.toString(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Item added to cart: " + toastText);
    }

    /**
     * 驗證結果類
     */
    private static class ValidationResult {
        final boolean isValid;
        final String message;

        ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }
}
