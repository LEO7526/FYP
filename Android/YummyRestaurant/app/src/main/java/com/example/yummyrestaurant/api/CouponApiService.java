package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.CouponDetailResponse;
import com.example.yummyrestaurant.models.CouponHistoryResponse;
import com.example.yummyrestaurant.models.CouponListResponse;
import com.example.yummyrestaurant.models.CouponPointResponse;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.models.MyCouponListResponse;
import com.example.yummyrestaurant.models.RedeemCouponResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CouponApiService {

    // --- Coupon list with language ---
    @GET("getCoupons.php")
    Call<CouponListResponse> getCoupons(
            @Query("lang") String lang
    );

    // --- Coupon detail with language ---
    @GET("getCouponDetail.php")
    Call<CouponDetailResponse> getCouponDetail(
            @Query("coupon_id") int couponId,
            @Query("lang") String lang
    );

    // --- Get user points ---
    @GET("getCouponPoints.php")
    Call<CouponPointResponse> getCouponPoints(
            @Query("cid") int customerId
    );

    // --- Redeem coupon with quantity ---
    @FormUrlEncoded
    @POST("redeemCoupon.php")
    Call<RedeemCouponResponse> redeemCoupon(
            @Field("cid") int customerId,
            @Field("coupon_id") int couponId,
            @Field("quantity") int quantity
    );

    @GET("getCouponHistory.php")
    Call<CouponHistoryResponse> getCouponHistory(
            @Query("cid") int customerId,
            @Query("lang") String lang
    );

    // --- Use coupon (from MyCouponsActivity) ---
    @FormUrlEncoded
    @POST("useCoupon.php")
    Call<GenericResponse> useCoupon(
            @Field("cid") int customerId,
            @Field("coupon_id") int couponId,
            @Field("quantity") int quantity   // 👈 new
    );

    @GET("getMyCoupons.php")
    Call<MyCouponListResponse> getMyCoupons(
            @Query("cid") int customerId,
            @Query("lang") String lang   // optional if your PHP supports translations
    );



}
