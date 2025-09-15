package com.example.yummyrestaurant.api;

import com.example.yummyrestaurant.models.Product;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("list_products.php")
    Call<List<Product>> getProducts();
}