package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.BirthdayResponse;
import com.example.yummyrestaurant.models.UploadResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface CustomerUploadApi {

    @Multipart
    @POST("save_customerProfileImage.php")
    Call<UploadResponse> uploadImage(
            @Part MultipartBody.Part image,
            @Part("cemail") RequestBody email
    );

    @FormUrlEncoded
    @POST("update_birthday.php")
    Call<BirthdayResponse> updateBirthday(
            @Field("cemail") String email,
            @Field("cbirthday") String birthday
    );

}