package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.api.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LoginApi {
    @FormUrlEncoded
    @POST("login_staffAndcustomer.php")
    Call<LoginResponse> loginUser(
        @Field("email") String email,
        @Field("password") String password,
        @Field("role") String role 
    );
}
