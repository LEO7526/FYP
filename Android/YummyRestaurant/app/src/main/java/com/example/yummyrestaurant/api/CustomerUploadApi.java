package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.UploadResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface CustomerUploadApi {

    @Multipart
    @POST("save_customerImage.php")
    Call<UploadResponse> uploadImage(
            @Part MultipartBody.Part image,
            @Part("cemail") RequestBody email
    );
}