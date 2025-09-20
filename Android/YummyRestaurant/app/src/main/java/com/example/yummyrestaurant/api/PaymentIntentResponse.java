package com.example.yummyrestaurant.api;

import com.google.gson.annotations.SerializedName;

public class PaymentIntentResponse {
    @SerializedName("clientSecret")
    private String clientSecret;

    public String getClientSecret() {
        return clientSecret;
    }
}