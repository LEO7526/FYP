package com.example.yummyrestaurant.inventory;

import com.google.gson.annotations.SerializedName;

public class Material {
    public int mid;
    public String mname;
    public double mqty;
    public String unit;

    @SerializedName("reorderLevel")
    public double reorderLevel;
}