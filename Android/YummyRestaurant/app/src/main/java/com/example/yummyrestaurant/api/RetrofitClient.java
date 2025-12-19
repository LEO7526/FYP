package com.example.yummyrestaurant.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static synchronized Retrofit getClient(Context context) {
        String baseUrl = ApiConfig.getBaseUrl(context);

        if (retrofit == null || !retrofit.baseUrl().toString().equals(baseUrl)) {
            Log.d("RetrofitClient", "Building Retrofit with baseUrl: " + baseUrl);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                    message -> Log.d("HTTP", message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Create Gson instance with lenient mode to handle malformed JSON
            // 🔴 CRITICAL: Must include serializeNulls() to ensure all fields are serialized
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .serializeNulls()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    // 🔑 Call this when environment changes
    public static synchronized void reset() {
        retrofit = null;
    }
}