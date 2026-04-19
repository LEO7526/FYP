package com.example.yummyrestaurant.models;

import java.util.List;

public class PackagesResponse {
    private boolean success;
    private List<SetMenu> data;

    public boolean isSuccess() {
        return success;
    }

    public List<SetMenu> getData() {
        return data;
    }
}