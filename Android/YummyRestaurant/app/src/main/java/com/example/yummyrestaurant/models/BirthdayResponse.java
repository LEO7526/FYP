package com.example.yummyrestaurant.models;

public class BirthdayResponse {
    private boolean success;
    private String message;
    private String cbirthday; // optional: echo back the stored birthday

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getCbirthday() {
        return cbirthday;
    }
}