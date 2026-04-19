package com.example.yummyrestaurant.models;

import java.util.List;

public class MyCouponListResponse {
    private boolean success;
    private String message;
    private List<Coupon> coupons;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Coupon> getCoupons() {
        return coupons;
    }
}