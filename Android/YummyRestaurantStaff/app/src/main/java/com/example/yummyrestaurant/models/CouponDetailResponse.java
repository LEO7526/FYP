package com.example.yummyrestaurant.models;

public class CouponDetailResponse {
    private boolean success;
    private Coupon coupon;
    private String error; // optional error message

    public boolean isSuccess() {
        return success;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public String getError() {
        return error;
    }
}