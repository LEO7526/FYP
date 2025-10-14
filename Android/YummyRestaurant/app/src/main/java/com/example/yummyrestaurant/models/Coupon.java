package com.example.yummyrestaurant.models;

public class Coupon {
    private int coupon_id;
    private String title;
    private String description;
    private int points_required;
    private String expiry_date;

    // Empty constructor required for Retrofit/Gson
    public Coupon() {}

    public int getCoupon_id() {
        return coupon_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints_required() {
        return points_required;
    }

    public String getExpiry_date() {
        return expiry_date;
    }
}