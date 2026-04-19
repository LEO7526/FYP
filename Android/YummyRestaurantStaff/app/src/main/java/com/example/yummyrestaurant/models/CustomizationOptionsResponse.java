package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 表示從 API 返回的自訂選項響應
 */
public class CustomizationOptionsResponse implements Serializable {
    private boolean success;
    private List<CustomizationOptionDetail> options;
    private String error;

    public CustomizationOptionsResponse() {}

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<CustomizationOptionDetail> getOptions() { return options; }
    public void setOptions(List<CustomizationOptionDetail> options) { this.options = options; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    @Override
    public String toString() {
        return "CustomizationOptionsResponse{" +
                "success=" + success +
                ", options=" + options +
                ", error='" + error + '\'' +
                '}';
    }

    /**
     * 自訂選項詳細資訊（v4.5版本 - 群組-值結構）
     */
    public static class CustomizationOptionDetail implements Serializable {
        @SerializedName("option_id")
        private int option_id;
        
        @SerializedName("item_id")
        private int item_id;
        
        @SerializedName("group_id")
        private int group_id;
        
        @SerializedName("group_name")
        private String group_name;
        
        @SerializedName("group_type")
        private String group_type;
        
        @SerializedName("max_selections")
        private int max_selections;
        
        @SerializedName("is_required")
        private int is_required;  // ✅ 必填項標記
        
        @SerializedName("values")
        private List<ValueItem> values;  // ✅ v4.5：使用values替代choices

        public CustomizationOptionDetail() {}

        public int getOptionId() { return option_id; }
        public void setOptionId(int id) { this.option_id = id; }

        public int getItemId() { return item_id; }
        public void setItemId(int id) { this.item_id = id; }

        public int getGroupId() { return group_id; }
        public void setGroupId(int id) { this.group_id = id; }

        public String getGroupName() { return group_name; }
        public void setGroupName(String name) { this.group_name = name; }

        public String getGroupType() { return group_type; }
        public void setGroupType(String type) { this.group_type = type; }

        public int getMaxSelections() { return max_selections; }
        public void setMaxSelections(int max) { this.max_selections = max; }

        public int isRequired() { return is_required; }
        public void setRequired(int required) { this.is_required = required; }

        public List<ValueItem> getValues() { return values; }
        public void setValues(List<ValueItem> values) { this.values = values; }

        // ⚠️ 向後兼容：支持舊版本的choices欄位
        @SerializedName("choices")
        private List<ChoiceItem> choices;
        public List<ChoiceItem> getChoices() { return choices; }
        public void setChoices(List<ChoiceItem> choices) { this.choices = choices; }

        // ✅ 獲取選項名稱（相容舊版本的option_name，新版本使用group_name）
        @SerializedName("option_name")
        private String option_name;
        public String getOptionName() { return group_name != null ? group_name : option_name; }
        public void setOptionName(String name) { this.option_name = name; }

        @Override
        public String toString() {
            return "CustomizationOptionDetail{" +
                    "option_id=" + option_id +
                    ", item_id=" + item_id +
                    ", group_id=" + group_id +
                    ", group_name='" + group_name + '\'' +
                    ", group_type='" + group_type + '\'' +
                    ", max_selections=" + max_selections +
                    ", is_required=" + is_required +
                    ", values=" + values +
                    '}';
        }
    }

    /**
     * 自訂值項（v4.5版本 - 替代ChoiceItem）
     */
    public static class ValueItem implements Serializable {
        @SerializedName("value_id")
        private int value_id;
        
        @SerializedName("value_name")
        private String value_name;
        
        @SerializedName("display_order")
        private int display_order;

        public ValueItem() {}

        public int getValueId() { return value_id; }
        public void setValueId(int id) { this.value_id = id; }

        public String getValueName() { return value_name; }
        public void setValueName(String name) { this.value_name = name; }

        public int getDisplayOrder() { return display_order; }
        public void setDisplayOrder(int order) { this.display_order = order; }

        @Override
        public String toString() {
            return "ValueItem{" +
                    "value_id=" + value_id +
                    ", value_name='" + value_name + '\'' +
                    ", display_order=" + display_order +
                    '}';
        }
    }

    /**
     * 自訂選項的具體選擇項（向後兼容舊版本）
     */
    public static class ChoiceItem implements Serializable {
        @SerializedName("choice_id")
        private int choice_id;
        
        @SerializedName("choice_name")
        private String choice_name;
        
        @SerializedName("additional_cost")
        private double additional_cost;

        public ChoiceItem() {}

        public int getChoiceId() { return choice_id; }
        public void setChoiceId(int id) { this.choice_id = id; }

        public String getChoiceName() { return choice_name; }
        public void setChoiceName(String name) { this.choice_name = name; }

        public double getAdditionalCost() { return additional_cost; }
        public void setAdditionalCost(double cost) { this.additional_cost = cost; }

        @Override
        public String toString() {
            return "ChoiceItem{" +
                    "choice_id=" + choice_id +
                    ", choice_name='" + choice_name + '\'' +
                    ", additional_cost=" + additional_cost +
                    '}';
        }
    }
}
