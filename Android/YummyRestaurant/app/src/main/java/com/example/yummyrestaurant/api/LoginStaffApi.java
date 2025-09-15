package com.example.yummyrestaurant.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LoginStaffApi {
    @FormUrlEncoded
    @POST("get_staff.php")
    Call<LoginResponse> loginUser(
        @Field("email") String email,
        @Field("password") String password
    );
}
