package com.example.yummyrestaurant.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LoginCustomerApi {
    @FormUrlEncoded
    @POST("get_customer.php")
    Call<LoginResponse> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("updateProfileImage.php") // Replace with your actual endpoint
    Call<Void> updateProfileImage(
            @Field("email") String email,
            @Field("imageUrl") String imageUrl
    );


}
