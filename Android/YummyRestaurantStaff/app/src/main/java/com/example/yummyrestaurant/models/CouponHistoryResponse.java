package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CouponHistoryResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("history")
    private List<CouponHistoryItem> history;

    public boolean isSuccess() {
        return success;
    }

    public List<CouponHistoryItem> getHistory() {
        return history;
    }
}