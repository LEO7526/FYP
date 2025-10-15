package com.example.yummyrestaurant.models;

import java.util.List;

public class CouponHistoryResponse {
    private boolean success;
    private List<CouponHistoryItem> history;
    private String error; // optional, in case backend sends error message

    public boolean isSuccess() {
        return success;
    }

    public List<CouponHistoryItem> getHistory() {
        return history;
    }

    public String getError() {
        return error;
    }
}