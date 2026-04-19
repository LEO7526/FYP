package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;

public class CouponHistoryItem {

    @SerializedName("delta")
    private int delta;

    @SerializedName("resulting_points")
    private int resultingPoints;

    @SerializedName("action")
    private String action;

    @SerializedName("note")
    private String note;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("coupon_title")
    private String couponTitle;

    // Getters
    public int getDelta() {
        return delta;
    }

    public int getResulting_points() {
        return resultingPoints;
    }

    public String getAction() {
        return action;
    }

    public String getNote() {
        return note;
    }

    public String getCreated_at() {
        return createdAt;
    }

    public String getCouponTitle() {
        return couponTitle;
    }
}