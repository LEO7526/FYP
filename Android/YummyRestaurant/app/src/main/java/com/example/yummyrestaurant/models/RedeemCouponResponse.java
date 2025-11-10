package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;

public class RedeemCouponResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    @SerializedName("error_code")   // ✅ new field
    private String errorCode;

    @SerializedName("coupon_title")
    private String couponTitle;

    @SerializedName("points_before")
    private Integer pointsBefore;

    @SerializedName("points_after")
    private Integer pointsAfter;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public String getErrorCode() {   // ✅ getter for error_code
        return errorCode;
    }

    public String getCouponTitle() {
        return couponTitle;
    }

    public Integer getPointsBefore() {
        return pointsBefore;
    }

    public Integer getPointsAfter() {
        return pointsAfter;
    }
}