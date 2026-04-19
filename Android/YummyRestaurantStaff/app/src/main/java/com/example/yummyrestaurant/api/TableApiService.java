package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.TableOrder;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TableApiService {
    @GET("get_tableOrders.php")
    Call<List<TableOrder>> getAllTableOrders();
}