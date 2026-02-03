package com.example.inventorymanager;

import com.google.gson.annotations.SerializedName;

// 這個類別代表一個可以製作的菜單項目
public class Recipe {
    @SerializedName("item_id")
    int itemId;

    @SerializedName("item_name")
    String itemName;
    
    // 可以再加上食譜描述等其他資訊
}