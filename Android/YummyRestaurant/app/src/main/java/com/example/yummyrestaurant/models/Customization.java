package com.example.yummyrestaurant.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 代表菜品的自訂配置
 * 包含辛辣度、備註以及詳細的自訂選項
 */
public class Customization {
    private String spiceLevel;
    private String extraNotes;
    private List<OrderItemCustomization> customizationDetails; // 詳細自訂選項

    public Customization(String spiceLevel, String extraNotes) {
        this.spiceLevel = spiceLevel;
        this.extraNotes = extraNotes;
        this.customizationDetails = new ArrayList<>();
    }

    public String getSpiceLevel() { return spiceLevel; }
    public String getExtraNotes() { return extraNotes; }
    
    public List<OrderItemCustomization> getCustomizationDetails() { 
        return customizationDetails; 
    }
    
    public void setCustomizationDetails(List<OrderItemCustomization> details) {
        this.customizationDetails = details;
    }

    /**
     * 添加一個自訂選項到列表
     */
    public void addCustomizationDetail(OrderItemCustomization detail) {
        if (customizationDetails == null) {
            customizationDetails = new ArrayList<>();
        }
        customizationDetails.add(detail);
    }

    /**
     * 計算所有自訂選項的額外費用總和
     */
    public double getTotalAdditionalCost() {
        if (customizationDetails == null || customizationDetails.isEmpty()) {
            return 0;
        }
        return customizationDetails.stream()
                .mapToDouble(OrderItemCustomization::getAdditionalCost)
                .sum();
    }

    /**
     * 驗證是否所有必填的自訂選項都已填充
     */
    public boolean validateCustomizations(List<CustomizationOption> requiredOptions) {
        if (requiredOptions == null || requiredOptions.isEmpty()) {
            return true; // 沒有必填項
        }

        for (CustomizationOption option : requiredOptions) {
            if (!option.isRequired()) {
                continue; // 跳過非必填項
            }

            boolean found = false;
            if (customizationDetails != null) {
                for (OrderItemCustomization detail : customizationDetails) {
                    if (detail.getOptionId() == option.getOptionId()) {
                        // 檢查是否已選擇
                        if ((detail.getSelectedChoices() != null && !detail.getSelectedChoices().isEmpty()) ||
                                (detail.getTextValue() != null && !detail.getTextValue().isEmpty())) {
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (!found) {
                return false; // 必填項未填
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customization)) return false;
        Customization that = (Customization) o;
        return Objects.equals(spiceLevel, that.spiceLevel) &&
                Objects.equals(extraNotes, that.extraNotes) &&
                Objects.equals(customizationDetails, that.customizationDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spiceLevel, extraNotes, customizationDetails);
    }

    @Override
    public String toString() {
        return "Customization{spiceLevel=" + spiceLevel + 
               ", extraNotes=" + extraNotes + 
               ", details=" + (customizationDetails != null ? customizationDetails.size() : 0) + "}";
    }
}