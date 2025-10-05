package com.example.yummyrestaurant.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PaymentApiService {
    @POST("payMoneyUrl.php")
    Call<PaymentUrlResponse> getPayDollarUrl(@Body Map<String, Object> payload);
}