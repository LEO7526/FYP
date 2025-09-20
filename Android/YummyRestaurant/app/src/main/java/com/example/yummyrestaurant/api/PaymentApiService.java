package com.example.yummyrestaurant.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PaymentApiService {
    @Headers("Content-Type: application/json")
    @POST("create_payment_intent.php") // Adjusted to match your PHP file name
    Call<PaymentIntentResponse> createPaymentIntent(@Body Map<String, Object> data);
}