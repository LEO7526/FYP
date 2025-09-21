package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.OrderItem;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OrderApiService {
    @Headers("Content-Type: application/json")
    @POST("save_order.php")
    Call<ResponseBody> saveOrder(@Body Map<String, Object> data);

    @GET("get_orderItems.php")
    Call<List<OrderItem>> getOrderItems(@Query("order_id") int orderId);

}