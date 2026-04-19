package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class PackageType {
    private int id;
    private String name;

    @SerializedName("optional_quantity")
    private int optionalQuantity;

    @SerializedName("items")
    private List<MenuItem> items;

    public int getId() { return id; }
    public String getName() { return name; }
    public int getOptionalQuantity() { return optionalQuantity; }

    // Null-safe getter
    public List<MenuItem> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public List<MenuItem> getAvailableItems() {
        return getItems();
    }
}