package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * 代表菜品自訂選項（v4.5版本 - 基於群組的階層結構）
 * 例如：辛辣度、配菜選擇等
 */
public class CustomizationOption implements Serializable {

    @SerializedName("option_id")
    private int optionId;

    @SerializedName("item_id")
    private int itemId;

    // ✅ v4.5新增：群組ID和群組名稱
    @SerializedName("group_id")
    private int groupId;

    @SerializedName("group_name")
    private String groupName; // 例如："Spice Level", "Sugar Level"

    @SerializedName("group_type")
    private String groupType; // 例如："spice", "sugar", "ice", "milk", "topping", "other"

    // ✅ v4.5：替代choices，使用values（基於value_id而非choice_id）
    @SerializedName("values")
    private List<OptionValue> values; // 可選的值列表

    @SerializedName("max_selections")
    private int maxSelections; // 最多可選數量

    @SerializedName("is_required")
    private int isRequired; // 0 = 可選, 1 = 必填

    // Getters and Setters
    public int getOptionId() { return optionId; }
    public void setOptionId(int optionId) { this.optionId = optionId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupType() { return groupType; }
    public void setGroupType(String groupType) { this.groupType = groupType; }

    public List<OptionValue> getValues() { return values; }
    public void setValues(List<OptionValue> values) { this.values = values; }

    public int getMaxSelections() { return maxSelections; }
    public void setMaxSelections(int maxSelections) { this.maxSelections = maxSelections; }

    public int getIsRequired() { return isRequired; }
    public void setIsRequired(int isRequired) { this.isRequired = isRequired; }

    /**
     * ✅ 向後兼容：getOptionName() - 返回groupName
     * (保持與舊代碼的相容性)
     */
    public String getOptionName() {
        return groupName;
    }

    /**
     * ✅ 向後兼容：setOptionName() - 設置groupName
     */
    public void setOptionName(String optionName) {
        this.groupName = optionName;
    }

    // ✅ v4.5新增：內部類代表單個值（替代OptionChoice）
    public static class OptionValue implements Serializable {
        @SerializedName("value_id")
        private int valueId;

        @SerializedName("value_name")
        private String valueName;

        @SerializedName("display_order")
        private int displayOrder;

        public OptionValue() {}

        public OptionValue(int valueId, String valueName, int displayOrder) {
            this.valueId = valueId;
            this.valueName = valueName;
            this.displayOrder = displayOrder;
        }

        public int getValueId() { return valueId; }
        public void setValueId(int valueId) { this.valueId = valueId; }

        public String getValueName() { return valueName; }
        public void setValueName(String valueName) { this.valueName = valueName; }

        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    }
}
