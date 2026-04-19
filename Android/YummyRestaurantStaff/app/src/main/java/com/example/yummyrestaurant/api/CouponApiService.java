package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.BirthdayResponse;
import com.example.yummyrestaurant.models.CouponDetailResponse;
import com.example.yummyrestaurant.models.CouponHistoryResponse;
import com.example.yummyrestaurant.models.CouponListResponse;
import com.example.yummyrestaurant.models.CouponPointResponse;
import com.example.yummyrestaurant.models.GenericResponse;
import com.example.yummyrestaurant.models.MyCouponListResponse;
import com.example.yummyrestaurant.models.RedeemCouponResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CouponApiService {

    // --- Get all active coupons (with translations) ---
    @GET("getCoupons.php")
    Call<CouponListResponse> getCoupons(
            @Query("lang") String lang
    );

    // --- Get detailed info for a single coupon ---
    @GET("getCouponDetail.php")
    Call<CouponDetailResponse> getCouponDetail(
            @Query("coupon_id") int couponId,
            @Query("lang") String lang
    );

    // --- Get current coupon points for a customer ---
    @GET("getCouponPoints.php")
    Call<CouponPointResponse> getCouponPoints(
            @Query("cid") int customerId
    );

    // --- Redeem a coupon (spend points to acquire) ---
    @FormUrlEncoded
    @POST("redeemCoupon.php")
    Call<RedeemCouponResponse> redeemCoupon(
            @Field("cid") int customerId,
            @Field("coupon_id") int couponId,
            @Field("quantity") int quantity
    );

    // --- Get coupon usage history for a customer ---
    @GET("getCouponHistory.php")
    Call<CouponHistoryResponse> getCouponHistory(
            @Query("cid") int customerId,
            @Query("lang") String lang
    );

    // --- Mark coupon as used (during checkout) ---
    @FormUrlEncoded
    @POST("useCoupon.php")
    Call<GenericResponse> useCoupons(
            @Field("cid") int customerId,
            @Field("order_total") int orderTotal,
            @Field("order_type") String orderType,
            @FieldMap Map<String, Integer> couponQuantities,
            @Field("eligible_item_ids[]") List<Integer> itemIds
    );





    // --- Get all coupons currently owned by a customer ---
    @GET("getMyCoupons.php")
    Call<MyCouponListResponse> getMyCoupons(
            @Query("cid") int customerId,
            @Query("lang") String lang
    );

    @GET("getBirthday.php")
    Call<BirthdayResponse> getBirthday(@Query("cid") int customerId);
}