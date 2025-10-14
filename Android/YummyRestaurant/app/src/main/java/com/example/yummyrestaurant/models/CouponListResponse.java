package com.example.yummyrestaurant.models;

import java.util.List;

public class CouponListResponse {
    private boolean success;
    private List<Coupon> coupons;
    private String error; // optional

    public boolean isSuccess() {
        return success;
    }

    public List<Coupon> getCoupons() {
        return coupons;
    }

    public String getError() {
        return error;
    }
}