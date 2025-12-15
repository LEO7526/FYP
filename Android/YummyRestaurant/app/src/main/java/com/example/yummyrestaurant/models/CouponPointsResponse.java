package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;

/**
 * 優惠券點數響應模型
 */
public class CouponPointsResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("coupon_points")
    private int couponPoints;

    @SerializedName("error")
    private String error;

    public CouponPointsResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCouponPoints() {
        return couponPoints;
    }

    public void setCouponPoints(int couponPoints) {
        this.couponPoints = couponPoints;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "CouponPointsResponse{" +
                "success=" + success +
                ", couponPoints=" + couponPoints +
                ", error='" + error + '\'' +
                '}';
    }
}
