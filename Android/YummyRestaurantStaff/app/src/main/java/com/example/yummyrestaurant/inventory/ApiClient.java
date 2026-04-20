package com.example.yummyrestaurant.inventory;

import com.example.yummyrestaurant.api.ApiConstants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static String lastBaseUrl = null;

    public static Retrofit getClient() {
        String currentBaseUrl = ApiConstants.baseUrl();
        if (retrofit == null || lastBaseUrl == null || !lastBaseUrl.equals(currentBaseUrl)) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            lastBaseUrl = currentBaseUrl;
        }
        return retrofit;
    }
}