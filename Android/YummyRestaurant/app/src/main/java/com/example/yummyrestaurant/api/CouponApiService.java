package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.CouponListResponse;
import com.example.yummyrestaurant.models.CouponPointResponse;
import com.example.yummyrestaurant.models.CouponHistoryResponse;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.models.RedeemCouponResponse;
import com.example.yummyrestaurant.models.MyCouponListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Field;

public interface CouponApiService {
    @GET("getCouponPoints.php")
    Call<CouponPointResponse> getCouponPoints(@Query("cid") int customerId);

    @GET("getCoupons.php")
    Call<CouponListResponse> getCoupons();

    @FormUrlEncoded
    @POST("redeemCoupon.php")
    Call<RedeemCouponResponse> redeemCoupon(
            @Field("cid") int customerId,
            @Field("coupon_id") int couponId
    );

    @GET("getCouponHistory.php")
    Call<CouponHistoryResponse> getCouponHistory(@Query("cid") int customerId);

    // 👉 Add this new endpoint for redeemed but unused coupons
    @GET("getMyCoupons.php")
    Call<MyCouponListResponse> getMyCoupons(@Query("cid") int customerId);

    @FormUrlEncoded
    @POST("useCoupon.php")
    Call<GenericResponse> useCoupon(
            @Field("cid") int customerId,
            @Field("coupon_id") int couponId
    );
}