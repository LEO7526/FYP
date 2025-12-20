package com.example.yummyrestaurant.models;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 代表訂單中一個菜品項目的所有自訂選擇（v4.5版本 - 群組-值結構）
 * 用於儲存和傳送訂單時的自訂數據
 */
public class OrderItemCustomization implements Serializable {

    @SerializedName("option_id")
    private int optionId;

    // ✅ v4.5新增：群組ID和群組名稱
    @SerializedName("group_id")
    private int groupId;

    @SerializedName("group_name")
    private String groupName;

    // ✅ v4.5改變：使用value_ids（整數陣列）替代choice_names（字符串）
    @SerializedName("selected_value_ids")
    private List<Integer> selectedValueIds;

    // ✅ v4.5新增：選擇的值的名稱（用於顯示）
    @SerializedName("selected_values")
    private List<String> selectedValues;

    // ⚠️ 保留向後兼容：仍然支持choice_names用於API回應
    @SerializedName("choice_names")
    private String choiceNames;

    @SerializedName("text_value")
    private String textValue; // 用於文字備註

    // ⚠️ v4.5已移除：additional_cost（不再追蹤每個自訂選項的額外費用）
    @SerializedName("additional_cost")
    private double additionalCost;

    public OrderItemCustomization() {
        this.selectedValueIds = new ArrayList<>();
        this.selectedValues = new ArrayList<>();
    }

    public OrderItemCustomization(int optionId, String groupName) {
        this.optionId = optionId;
        this.groupName = groupName;
        this.selectedValueIds = new ArrayList<>();
        this.selectedValues = new ArrayList<>();
        this.additionalCost = 0;
    }

    // Getters and Setters
    public int getOptionId() { return optionId; }
    public void setOptionId(int optionId) { this.optionId = optionId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    // ✅ v4.5：value_ids getters/setters
    public List<Integer> getSelectedValueIds() { return selectedValueIds; }
    public void setSelectedValueIds(List<Integer> selectedValueIds) {
        this.selectedValueIds = selectedValueIds;
    }

    // ✅ v4.5：values getters/setters
    public List<String> getSelectedValues() { return selectedValues; }
    public void setSelectedValues(List<String> selectedValues) {
        this.selectedValues = selectedValues;
    }

    // ⚠️ 向後兼容：choice_names getters/setters
    public String getChoiceNames() { return choiceNames; }
    public void setChoiceNames(String choiceNames) { this.choiceNames = choiceNames; }

    /**
     * Get parsed choice names for display purposes
     * Handles JSON array format from API (e.g., ["Numbing"] or ["Mild","Medium"])
     * @return Cleaned string ready for display (e.g., "Numbing" or "Mild, Medium")
     */
    public String getChoiceNamesDisplay() {
        if (choiceNames == null || choiceNames.isEmpty()) {
            return "";
        }
        
        // Try to parse as JSON array first
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> choices = gson.fromJson(choiceNames, listType);
            if (choices != null && !choices.isEmpty()) {
                return String.join(", ", choices);
            }
        } catch (JsonSyntaxException e) {
            // Not valid JSON, treat as plain string
            // Remove any quotes that might be present
            return choiceNames.replace("\"", "");
        }
        
        return choiceNames;
    }


    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue; }

    public double getAdditionalCost() { return additionalCost; }
    public void setAdditionalCost(double additionalCost) { this.additionalCost = additionalCost; }

    /**
     * 添加一個值ID到該自訂選項
     */
    public void addValueId(int valueId) {
        if (selectedValueIds == null) {
            selectedValueIds = new ArrayList<>();
        }
        selectedValueIds.add(valueId);
    }

    /**
     * 添加一個值名稱到該自訂選項
     */
    public void addValue(String valueName) {
        if (selectedValues == null) {
            selectedValues = new ArrayList<>();
        }
        selectedValues.add(valueName);
    }

    /**
     * ⚠️ 向後兼容：getOptionName() - 返回groupName
     * (保持與舊代碼的相容性)
     */
    public String getOptionName() {
        return groupName;
    }

    /**
     * ⚠️ 向後兼容：getSelectedChoices() - 返回selectedValues或其他相關資料
     * (保持與舊代碼的相容性)
     */
    public List<String> getSelectedChoices() {
        // 優先返回selectedValues（v4.5）
        if (selectedValues != null && !selectedValues.isEmpty()) {
            return selectedValues;
        }
        // 備用：若只有choiceNames，轉換為List
        if (choiceNames != null && !choiceNames.isEmpty()) {
            List<String> result = new ArrayList<>();
            result.add(choiceNames);
            return result;
        }
        return new ArrayList<>();
    }

    /**
     * ⚠️ 向後兼容：setOptionName() - 設置groupName
     * (保持與舊代碼的相容性)
     */
    public void setOptionName(String optionName) {
        this.groupName = optionName;
    }

    /**
     * ⚠️ 向後兼容：setSelectedChoices() - 設置selectedValues
     * (保持與舊代碼的相容性)
     */
    public void setSelectedChoices(List<String> selectedChoices) {
        this.selectedValues = selectedChoices;
    }

    /**
     * 獲取該自訂的簡短顯示文字（v4.5更新）
     */
    public String getDisplayText() {
        if (textValue != null && !textValue.isEmpty()) {
            return optionName + ": " + textValue;
        } else if (selectedChoices != null && !selectedChoices.isEmpty()) {
            return optionName + ": " + String.join(", ", selectedChoices);
        } else if (choiceNames != null && !choiceNames.isEmpty()) {
            // Use the helper method to get cleaned display value
            return optionName + ": " + getChoiceNamesDisplay();
        }
        return groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItemCustomization)) return false;
        OrderItemCustomization that = (OrderItemCustomization) o;
        return optionId == that.optionId &&
                groupId == that.groupId &&
                Objects.equals(groupName, that.groupName) &&
                Objects.equals(selectedValueIds, that.selectedValueIds) &&
                Objects.equals(textValue, that.textValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, groupId, groupName, selectedValueIds, textValue);
    }

    @Override
    public String toString() {
        return "OrderItemCustomization{" +
                "optionId=" + optionId +
                ", groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", selectedValueIds=" + selectedValueIds +
                ", selectedValues=" + selectedValues +
                ", textValue='" + textValue + '\'' +
                ", additionalCost=" + additionalCost +
                '}';
    }
}
