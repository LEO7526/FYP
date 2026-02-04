package com.example.yummyrestaurant.inventory;

import com.google.gson.annotations.SerializedName;

public class ConsumptionLog {
    @SerializedName("log_id")
    public int logId;

    @SerializedName("log_date")
    public String logDate;

    @SerializedName("log_type")
    public String logType;

    @SerializedName("details")
    public String details;
}