package com.example.yummyrestaurant.api;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_Simulator_URL = "http://10.0.2.2/NewFolder/Database/projectapi/";
    private static final String BASE_Phone_URL = "http://192.168.0.120/NewFolder/Database/projectapi/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                    message -> Log.d("HTTP", message)
            );
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Build OkHttp client with logging
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Build Retrofit with the client
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_Simulator_URL) // switch to BASE_Phone_URL if testing on device
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static String getBASE_Simulator_URL() {
        return BASE_Simulator_URL;
    }

    public static String getBASE_Phone_URL() {
        return BASE_Phone_URL;
    }
}