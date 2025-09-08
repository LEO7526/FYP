package com.example.yummyrestaurant.api;

import retrofit2.Call;
import retrofit2.http.POST;

public interface PaymentApiService {
    @POST("/create-payment-intent")
    Call<PaymentIntentResponse> createPaymentIntent();
}
