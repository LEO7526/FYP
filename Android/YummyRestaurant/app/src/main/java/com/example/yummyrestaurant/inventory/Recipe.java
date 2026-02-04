package com.example.yummyrestaurant.inventory;

import com.google.gson.annotations.SerializedName;

public class Recipe {
    @SerializedName("item_id")
    public int itemId;

    @SerializedName("item_name")
    public String itemName;
}