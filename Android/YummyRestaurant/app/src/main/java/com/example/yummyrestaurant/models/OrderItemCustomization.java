package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 代表訂單中一個菜品項目的所有自訂選擇
 * 用於儲存和傳送訂單時的自訂數據
 */
public class OrderItemCustomization implements Serializable {

    @SerializedName("option_id")
    private int optionId;

    @SerializedName("option_name")
    private String optionName;

    // ✅ 改變：支持List<String>以簡化序列化
    @SerializedName("selected_choices")
    private List<String> selectedChoices;

    // ✅ 新增：support choice_names from API (comma-separated string)
    @SerializedName("choice_names")
    private String choiceNames;

    @SerializedName("text_value")
    private String textValue; // 用於文字備註

    @SerializedName("additional_cost")
    private double additionalCost; // 此自訂選項的額外費用

    public OrderItemCustomization() {
        this.selectedChoices = new ArrayList<>();
    }

    public OrderItemCustomization(int optionId, String optionName) {
        this.optionId = optionId;
        this.optionName = optionName;
        this.selectedChoices = new ArrayList<>();
        this.additionalCost = 0;
    }

    // Getters and Setters
    public int getOptionId() { return optionId; }
    public void setOptionId(int optionId) { this.optionId = optionId; }

    public String getOptionName() { return optionName; }
    public void setOptionName(String optionName) { this.optionName = optionName; }

    // ✅ 改變：使用List<String>
    public List<String> getSelectedChoices() { return selectedChoices; }
    public void setSelectedChoices(List<String> selectedChoices) {
        this.selectedChoices = selectedChoices;
    }

    // ✅ 新增：choiceNames getter/setter for API response
    public String getChoiceNames() { return choiceNames; }
    public void setChoiceNames(String choiceNames) { this.choiceNames = choiceNames; }

    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue; }

    public double getAdditionalCost() { return additionalCost; }
    public void setAdditionalCost(double additionalCost) { this.additionalCost = additionalCost; }

    /**
     * 添加一個選擇到該自訂選項（新方法用於List）
     */
    public void addChoice(String choiceName) {
        if (selectedChoices == null) {
            selectedChoices = new ArrayList<>();
        }
        selectedChoices.add(choiceName);
    }

    /**
     * 獲取該自訂的簡短顯示文字
     */
    public String getDisplayText() {
        if (textValue != null && !textValue.isEmpty()) {
            return optionName + ": " + textValue;
        } else if (selectedChoices != null && !selectedChoices.isEmpty()) {
            return optionName + ": " + String.join(", ", selectedChoices);
        } else if (choiceNames != null && !choiceNames.isEmpty()) {
            // Handle JSON string from API (e.g., ["Numbing"] or "Numbing")
            String displayValue = choiceNames;
            // Remove JSON array brackets if present
            if (displayValue.startsWith("[") && displayValue.endsWith("]")) {
                displayValue = displayValue.substring(1, displayValue.length() - 1);
            }
            // Remove quotes if present
            displayValue = displayValue.replaceAll("\"", "");
            return optionName + ": " + displayValue;
        }
        return optionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItemCustomization)) return false;
        OrderItemCustomization that = (OrderItemCustomization) o;
        return optionId == that.optionId &&
                Objects.equals(optionName, that.optionName) &&
                Objects.equals(selectedChoices, that.selectedChoices) &&
                Objects.equals(textValue, that.textValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, optionName, selectedChoices, textValue);
    }

    @Override
    public String toString() {
        return "OrderItemCustomization{" +
                "optionId=" + optionId +
                ", optionName='" + optionName + '\'' +
                ", selectedChoices=" + selectedChoices +
                ", textValue='" + textValue + '\'' +
                ", additionalCost=" + additionalCost +
                '}';
    }
}
