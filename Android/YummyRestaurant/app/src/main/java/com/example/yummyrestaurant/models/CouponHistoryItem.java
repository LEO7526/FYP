package com.example.yummyrestaurant.models;

public class CouponHistoryItem {
    private int delta;                // change in points (+/-)
    private int resulting_points;     // balance after change
    private String action;            // e.g. "Earned", "Redeemed"
    private String note;              // optional description
    private String created_at;        // timestamp

    public int getDelta() {
        return delta;
    }

    public int getResulting_points() {
        return resulting_points;
    }

    public String getAction() {
        return action;
    }

    public String getNote() {
        return note;
    }

    public String getCreated_at() {
        return created_at;
    }
}