package com.example.yummyrestaurant.inventory;

import com.google.gson.annotations.SerializedName;

public class FoodStock {
    @SerializedName("item_id")
    public int itemId;

    @SerializedName("item_name")
    public String itemName;

    @SerializedName("producible_qty")
    public int producibleQty;

    @SerializedName("min_producible_qty")
    public int minProducibleQty;
}