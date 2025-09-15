package com.example.yummyrestaurant.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RegisterApi {
    @POST("register_user.php")
    @FormUrlEncoded
    Call<RegisterResponse> registerUser(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password
    );
}
