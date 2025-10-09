package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.MenuResponse;
import com.example.yummyrestaurant.models.PackagesResponse;
import com.example.yummyrestaurant.models.SetMenu;
import com.example.yummyrestaurant.models.SetMenuResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MenuApi {

    // Existing API: still used by activities that need the full menu
    @GET("get_menuItems.php")
    Call<MenuResponse> getMenuItems(@Query("lang") String language);

    // 🔹 New API: fetch all set menus (Double Set, Business Set, etc.)
    @GET("get_packages.php")
    Call<PackagesResponse> getPackages();


    // 🔹 New API: fetch a specific set menu with its types and dishes
    @GET("get_package.php")
    Call<SetMenuResponse> getSetMenu(@Query("id") int id, @Query("lang") String lang);
}