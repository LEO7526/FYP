package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.MenuItem;
import com.example.yummyrestaurant.models.MenuResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MenuApi {
    @GET("get_menuItems.php") // Adjust this to match your actual endpoint
    Call<MenuResponse> getMenuItems(@Query("lang") String language);
}