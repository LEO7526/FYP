package com.example.yummyrestaurant.api;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OrderApiService {
    @Headers("Content-Type: application/json")
    @POST("save_order.php")
    Call<ResponseBody> saveOrder(@Body Map<String, Object> data);
}