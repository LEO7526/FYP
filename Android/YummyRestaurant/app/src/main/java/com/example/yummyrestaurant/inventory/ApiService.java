package com.example.yummyrestaurant.inventory;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("get_materials.php")
    Call<ApiResponse<List<Material>>> getMaterials();

    @GET("get_consumption_history.php")
    Call<ApiResponse<List<ConsumptionLog>>> getConsumptionHistory();

    @POST("adjust_stock.php")
    Call<ApiResponse<Void>> adjustStock(@Body StockAdjustRequest request);

    @GET("get_recipes.php")
    Call<ApiResponse<List<Recipe>>> getRecipes();

    @POST("cook_recipe.php")
    Call<ApiResponse<Void>> cookRecipe(@Body CookRequest request);

    @GET("get_food_stock.php")
    Call<ApiResponse<List<FoodStock>>> getFoodStock();
}