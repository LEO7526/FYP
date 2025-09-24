package com.example.yummyrestaurant.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_Simulator_URL = "http://10.0.2.2/NewFolder/Database/projectapi/";

    private static final String BASE_Phone_URL = "http://192.168.0.120/NewFolder/Database/projectapi/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_Simulator_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static String getBASE_Simulator_URL() {
        return BASE_Simulator_URL;
    }

    public static String getBASE_Phone_URL(){
        return BASE_Phone_URL;
    }




}
