package com.example.yummyrestaurant.inventory;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
        @POST("add_material.php")
        Call<ApiResponse<Void>> addMaterial(@Body Material material);
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

    @GET("get_restock_recommendations.php")
    Call<ApiResponse<List<RestockRecommendation>>> getRestockRecommendations(
            @Query("days") int days,
            @Query("refresh") int refresh,
            @Query("force_demo") int forceDemo
    );

    @POST("decide_restock_recommendation.php")
    Call<ApiResponse<Object>> decideRestockRecommendation(@Body RestockDecisionRequest request);
}