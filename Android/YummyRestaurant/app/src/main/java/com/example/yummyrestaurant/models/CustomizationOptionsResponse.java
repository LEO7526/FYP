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
     * 自訂選項詳細資訊（包括選擇項）
     */
    public static class CustomizationOptionDetail implements Serializable {
        @SerializedName("option_id")
        private int option_id;
        
        @SerializedName("item_id")
        private int item_id;
        
        @SerializedName("option_name")
        private String option_name;
        
        @SerializedName("option_type")
        private String option_type; // single_choice, multi_choice, text_note, quantity
        
        @SerializedName("is_required")
        private int is_required;
        
        @SerializedName("max_selections")
        private int max_selections;
        
        @SerializedName("choices")
        private List<ChoiceItem> choices;

        public CustomizationOptionDetail() {}

        public int getOptionId() { return option_id; }
        public void setOptionId(int id) { this.option_id = id; }

        public int getItemId() { return item_id; }
        public void setItemId(int id) { this.item_id = id; }

        public String getOptionName() { return option_name; }
        public void setOptionName(String name) { this.option_name = name; }

        public String getOptionType() { return option_type; }
        public void setOptionType(String type) { this.option_type = type; }

        public int isRequired() { return is_required; }
        public void setRequired(int required) { this.is_required = required; }

        public int getMaxSelections() { return max_selections; }
        public void setMaxSelections(int max) { this.max_selections = max; }

        public List<ChoiceItem> getChoices() { return choices; }
        public void setChoices(List<ChoiceItem> choices) { this.choices = choices; }

        @Override
        public String toString() {
            return "CustomizationOptionDetail{" +
                    "option_id=" + option_id +
                    ", option_name='" + option_name + '\'' +
                    ", option_type='" + option_type + '\'' +
                    ", is_required=" + is_required +
                    ", choices=" + choices +
                    '}';
        }
    }

    /**
     * 自訂選項的具體選擇項
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
