package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderPackageDish {
    @SerializedName("item_id")
    private int itemId;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private double price;

    @SerializedName("price_modifier")
    private double priceModifier;

    @SerializedName("customizations")
    private List<OrderItemCustomization> customizations;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceModifier() {
        return priceModifier;
    }

    public void setPriceModifier(double priceModifier) {
        this.priceModifier = priceModifier;
    }

    public List<OrderItemCustomization> getCustomizations() {
        return customizations;
    }

    public void setCustomizations(List<OrderItemCustomization> customizations) {
        this.customizations = customizations;
    }
}
