package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.CouponListResponse;
import com.example.yummyrestaurant.models.CouponPointResponse;
import com.example.yummyrestaurant.models.GenericResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CouponApiService {
    @GET("getCouponPoints.php")
    Call<CouponPointResponse> getCouponPoints(@Query("cid") int customerId);

    @GET("getCoupons.php")
    Call<CouponListResponse> getCoupons();

    @POST("redeemCoupon.php")
    Call<GenericResponse> redeemCoupon(@Body Map<String,Object> payload);

}
