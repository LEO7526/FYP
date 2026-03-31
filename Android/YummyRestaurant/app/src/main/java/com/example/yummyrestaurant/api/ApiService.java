package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.Product;
import com.example.yummyrestaurant.models.CustomizationOptionsResponse;
import com.example.yummyrestaurant.models.CouponPointsResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("list_products.php")
    Call<List<Product>> getProducts();

    @GET("get_customization_options.php")
    Call<CustomizationOptionsResponse> getCustomizationOptions(@Query("item_id") int itemId,
                                                               @Query("lang") String language);

    @GET("get_customer_coupon_points.php")
    Call<CouponPointsResponse> getCouponPoints(@Query("cid") int cid);
}