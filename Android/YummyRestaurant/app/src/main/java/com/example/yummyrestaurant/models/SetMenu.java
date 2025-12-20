package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SetMenu {
    private int id;
    private String name;

    @SerializedName("num_of_type")
    private int numOfType;

    private double discount;

    @SerializedName("image_url")
    private String imageUrl;

    private List<PackageType> types;

    public int getId() { return id; }
    public String getName() { return name; }
    public int getNumOfType() { return numOfType; }
    public double getDiscount() { return discount; }
    public String getImageUrl() { return imageUrl; }

    // Null-safe getter
    public List<PackageType> getTypes() {
        return types != null ? types : new ArrayList<>();
    }
}