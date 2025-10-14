package com.example.yummyrestaurant.models;

public class GenericResponse {
    private boolean success;
    private String message; // optional, used when success = true
    private String error;   // optional, used when success = false
    private Integer remaining_points; // optional, for redeemCoupon.php

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public Integer getRemaining_points() {
        return remaining_points;
    }
}