package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * 代表菜品自訂選項（例如：辛辣度、配菜選擇等）
 */
public class CustomizationOption implements Serializable {

    @SerializedName("option_id")
    private int optionId;

    @SerializedName("item_id")
    private int itemId;

    @SerializedName("option_name")
    private String optionName; // 例如："Spice Level", "Side Dish"

    @SerializedName("choices")
    private List<OptionChoice> choices; // 可選的選項列表

    @SerializedName("max_selections")
    private int maxSelections; // 最多可選數量

    // Getters and Setters
    public int getOptionId() { return optionId; }
    public void setOptionId(int optionId) { this.optionId = optionId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getOptionName() { return optionName; }
    public void setOptionName(String optionName) { this.optionName = optionName; }

    public List<OptionChoice> getChoices() { return choices; }
    public void setChoices(List<OptionChoice> choices) { this.choices = choices; }

    public int getMaxSelections() { return maxSelections; }
    public void setMaxSelections(int maxSelections) { this.maxSelections = maxSelections; }

    // 內部類：選項的單個選擇
    public static class OptionChoice implements Serializable {
        @SerializedName("choice_id")
        private int choiceId;

        @SerializedName("choice_name")
        private String choiceName;

        @SerializedName("additional_cost")
        private double additionalCost; // 額外費用（如果有）

        public OptionChoice() {}

        public OptionChoice(int choiceId, String choiceName, double additionalCost) {
            this.choiceId = choiceId;
            this.choiceName = choiceName;
            this.additionalCost = additionalCost;
        }

        public int getChoiceId() { return choiceId; }
        public void setChoiceId(int choiceId) { this.choiceId = choiceId; }

        public String getChoiceName() { return choiceName; }
        public void setChoiceName(String choiceName) { this.choiceName = choiceName; }

        public double getAdditionalCost() { return additionalCost; }
        public void setAdditionalCost(double additionalCost) { this.additionalCost = additionalCost; }
    }
}
