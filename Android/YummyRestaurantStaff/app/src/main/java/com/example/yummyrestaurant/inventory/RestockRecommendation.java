package com.example.yummyrestaurant.inventory;

import com.google.gson.annotations.SerializedName;

public class RestockRecommendation {
    @SerializedName("recommendation_id")
    public int recommendationId;

    @SerializedName("mid")
    public int materialId;

    @SerializedName("material_name")
    public String materialName;

    @SerializedName("unit")
    public String unit;

    @SerializedName("period_days")
    public int periodDays;

    @SerializedName("avg_daily_usage")
    public double avgDailyUsage;

    @SerializedName("projected_usage")
    public double projectedUsage;

    @SerializedName("current_qty")
    public double currentQty;

    @SerializedName("reorder_level")
    public double reorderLevel;

    @SerializedName("suggested_qty")
    public double suggestedQty;

    @SerializedName("status")
    public String status;
}
