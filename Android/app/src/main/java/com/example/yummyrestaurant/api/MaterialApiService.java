package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.inventory.ApiResponse;
import com.example.yummyrestaurant.inventory.Material;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MaterialApiService {
    @POST("add_material.php")
    Call<ApiResponse<Void>> addMaterial(@Body Material material);

    @GET("get_materials.php")
    Call<ApiResponse<List<Material>>> getMaterials();
}
