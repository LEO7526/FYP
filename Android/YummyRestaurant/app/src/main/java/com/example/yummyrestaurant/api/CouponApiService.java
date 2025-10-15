package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.CouponListResponse;
import com.example.yummyrestaurant.models.CouponPointResponse;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.models.CouponHistoryResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CouponApiService {

    // Fetch current points for a customer
    @GET("getCouponPoints.php")
    Call<CouponPointResponse> getCouponPoints(@Query("cid") int customerId);

    // Fetch all available coupons
    @GET("getCoupons.php")
    Call<CouponListResponse> getCoupons();

    // Redeem a coupon (deduct points, record redemption)
    @POST("redeemCoupon.php")
    Call<GenericResponse> redeemCoupon(@Body Map<String, Object> payload);

    // ✅ Fetch coupon history for a customer
    @GET("getCouponHistory.php")
    Call<CouponHistoryResponse> getCouponHistory(@Query("cid") int customerId);
}