package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Customization;
import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.models.OrderItemCustomization;
import com.example.yummyrestaurant.utils.CartManager;
import com.example.yummyrestaurant.utils.CustomizationValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品自訂 Activity
 * - 顯示菜品的自訂選項（辛辣度、特殊備註等）
 * - 驗證所有必填自訂項都已選擇
 * - 將自訂項加入購物車
 */
public class CustomizeDishActivity_v2 extends AppCompatActivity {

    private static final String TAG = "CustomizeDishActivity";

    public static final String EXTRA_MENU_ITEM = "menuItem";
    public static final String EXTRA_QUANTITY = "quantity";

    private Spinner spiceSpinner;
    private EditText notesEditText;
    private Button saveBtn;

    private MenuItem menuItem;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_dish);

        spiceSpinner = findViewById(R.id.spiceSpinner);
        notesEditText = findViewById(R.id.notesEditText);
        saveBtn = findViewById(R.id.saveCustomizationBtn);

        // Load extras
        menuItem = (MenuItem) getIntent().getSerializableExtra(EXTRA_MENU_ITEM);
        quantity = getIntent().getIntExtra(EXTRA_QUANTITY, 1);

        // Safety check
        if (menuItem == null) {
            Toast.makeText(this, "No dish data provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup spinner
        String[] spiceLevels = {"Select spice level", "Mild", "Medium", "Hot", "Numbing"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spiceLevels);
        spiceSpinner.setAdapter(adapter);
        spiceSpinner.setSelection(0); // 預設選擇首項（提示文字）

        saveBtn.setOnClickListener(v -> validateAndSaveCustomization());
    }

    /**
     * 驗證自訂選項並添加到購物車
     */
    private void validateAndSaveCustomization() {
        // 步驟 1：驗證必填項
        ValidationResult validation = validateCustomizations();
        if (!validation.isValid) {
            Toast.makeText(this, validation.message, Toast.LENGTH_LONG).show();
            Log.w(TAG, "Validation failed: " + validation.message);
            return;
        }

        // 步驟 2：收集自訂選項
        String chosenSpiceLevel = spiceSpinner.getSelectedItem().toString();
        String notes = notesEditText.getText().toString().trim();
        List<OrderItemCustomization> customizationDetails = new ArrayList<>();

        // 如果選擇了辛辣度（不是預設文字），添加到自訂詳情
        if (!chosenSpiceLevel.equals("Select spice level") && !chosenSpiceLevel.isEmpty()) {
            OrderItemCustomization spiceDetail = new OrderItemCustomization(1, "Spice Level");
            spiceDetail.setTextValue(chosenSpiceLevel);
            customizationDetails.add(spiceDetail);
        }

        // 步驟 3：建立 Customization 物件
        Customization customization = new Customization(chosenSpiceLevel, notes);
        customization.setCustomizationDetails(customizationDetails);

        // 步驟 4：建立 CartItem 並添加到購物車
        CartItem cartItem = new CartItem(menuItem, customization);

        Integer qtyFromCart = CartManager.getItemQuantity(cartItem);
        int currentQty = (qtyFromCart != null) ? qtyFromCart : 0;
        CartManager.updateQuantity(cartItem, currentQty + quantity);

        // 步驟 5：顯示確認信息
        showConfirmationMessage(chosenSpiceLevel, notes);

        // 步驟 6：返回購物車
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 驗證自訂選項
     */
    private ValidationResult validateCustomizations() {
        String spiceLevel = spiceSpinner.getSelectedItem().toString();

        // 檢查是否選擇了辛辣度（不是預設文字）
        if (spiceLevel.equals("Select spice level")) {
            return new ValidationResult(false, "Please select a spice level");
        }

        // 驗證備註長度
        String notes = notesEditText.getText().toString().trim();
        if (notes.length() > 500) {
            return new ValidationResult(false, "Special requests are too long (max 500 characters)");
        }

        return new ValidationResult(true, "");
    }

    /**
     * 顯示確認信息
     */
    private void showConfirmationMessage(String spiceLevel, String notes) {
        StringBuilder toastText = new StringBuilder();
        toastText.append(quantity).append(" × ").append(menuItem.getName());

        if (spiceLevel != null && !spiceLevel.isEmpty() && !spiceLevel.equals("Select spice level")) {
            toastText.append(" (").append(spiceLevel).append(")");
        }

        if (notes != null && !notes.isEmpty()) {
            toastText.append(" • Notes: ").append(notes);
        }

        Toast.makeText(this, toastText.toString(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Item added to cart: " + toastText);
    }

    /**
     * 簡單的驗證結果類
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
