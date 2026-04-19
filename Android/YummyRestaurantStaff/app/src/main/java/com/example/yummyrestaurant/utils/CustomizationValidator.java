package com.example.yummyrestaurant.utils;

import android.util.Log;

import com.example.yummyrestaurant.models.CustomizationOption;
import com.example.yummyrestaurant.models.OrderItemCustomization;
import java.util.List;

/**
 * 菜品自訂選項驗證工具類
 * 確保使用者選擇了所有必要的自訂選項
 */
public class CustomizationValidator {
    private static final String TAG = "CustomizationValidator";

    /**
     * 驗證結果類
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String message; // 如果無效，提供錯誤訊息

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }

    /**
     * 驗證所有自訂選項是否已被選擇
     * @param customizationOptions 自訂選項列表
     * @param selectedCustomizations 使用者已選擇的自訂詳情
     * @return ValidationResult
     */
    public static ValidationResult validateRequiredCustomizations(
            List<CustomizationOption> customizationOptions,
            List<OrderItemCustomization> selectedCustomizations) {

        if (customizationOptions == null || customizationOptions.isEmpty()) {
            Log.d(TAG, "No customization options");
            return new ValidationResult(true, "");
        }

        // 驗證所有選項都有選擇
        for (CustomizationOption option : customizationOptions) {
            boolean hasSelectedThisOption = false;

            // 檢查使用者是否選擇了此選項
            if (selectedCustomizations != null) {
                for (OrderItemCustomization selected : selectedCustomizations) {
                    if (selected.getOptionId() == option.getOptionId()) {
                        // 檢查是否真的有選擇內容
                        boolean hasContent = false;

                        if (selected.getSelectedChoices() != null && !selected.getSelectedChoices().isEmpty()) {
                            hasContent = true;
                        } else if (selected.getTextValue() != null && !selected.getTextValue().trim().isEmpty()) {
                            hasContent = true;
                        }

                        if (hasContent) {
                            hasSelectedThisOption = true;
                            break;
                        }
                    }
                }
            }

            if (!hasSelectedThisOption) {
                String errorMsg = String.format("Please select '%s'", option.getOptionName());
                Log.w(TAG, "Missing option: " + option.getOptionName());
                return new ValidationResult(false, errorMsg);
            }
        }

        Log.d(TAG, "All customizations validated successfully");
        return new ValidationResult(true, "");
    }

    /**
     * 驗證單個自訂選項是否符合其規則
     * @param option 自訂選項定義
     * @param selected 使用者的選擇
     * @return ValidationResult
     */
    public static ValidationResult validateSingleOption(
            CustomizationOption option,
            OrderItemCustomization selected) {

        if (selected == null) {
            return new ValidationResult(false, 
                String.format("'%s' is required", option.getOptionName()));
        }

        // 檢查多選限制
        int maxSelections = option.getMaxSelections();
        if (maxSelections > 0 && selected.getSelectedChoices() != null && 
            selected.getSelectedChoices().size() > maxSelections) {
            return new ValidationResult(false,
                String.format("You can select at most %d item(s) for '%s'",
                        maxSelections, option.getOptionName()));
        }

        Log.d(TAG, "Option validation passed for: " + option.getOptionName());
        return new ValidationResult(true, "");
    }

    /**
     * 驗證所有選擇的自訂選項是否符合規則
     * @param options 所有自訂選項定義
     * @param selections 使用者的所有選擇
     * @return ValidationResult
     */
    public static ValidationResult validateAllCustomizations(
            List<CustomizationOption> options,
            List<OrderItemCustomization> selections) {

        // 首先驗證必填項
        ValidationResult requiredCheck = validateRequiredCustomizations(options, selections);
        if (!requiredCheck.isValid) {
            return requiredCheck;
        }

        // 然後驗證每個選項
        if (selections != null) {
            for (OrderItemCustomization selected : selections) {
                CustomizationOption corresponding = findOptionById(options, selected.getOptionId());
                if (corresponding != null) {
                    ValidationResult singleCheck = validateSingleOption(corresponding, selected);
                    if (!singleCheck.isValid) {
                        return singleCheck;
                    }
                }
            }
        }

        return new ValidationResult(true, "");
    }

    /**
     * 根據 ID 查找自訂選項
     */
    private static CustomizationOption findOptionById(List<CustomizationOption> options, int optionId) {
        if (options == null) return null;
        for (CustomizationOption opt : options) {
            if (opt.getOptionId() == optionId) {
                return opt;
            }
        }
        return null;
    }
}
