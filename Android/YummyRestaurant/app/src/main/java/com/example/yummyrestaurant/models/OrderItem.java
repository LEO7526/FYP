package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;

public class OrderItem {

    @SerializedName("item_id")
    private int itemId;

    @SerializedName("name")
    private String name;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("itemPrice")
    private double itemPrice;

    @SerializedName("itemCost")
    private double itemCost;

    public OrderItem(int itemId, String name, int quantity, double itemPrice, double itemCost) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
        this.itemCost = itemCost;
    }

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public double getItemCost() {
        return itemCost;
    }

    public void setItemCost(double itemCost) {
        this.itemCost = itemCost;
    }

    // Optional: calculate subtotal dynamically
    public double getTotalCost() {
        return itemPrice * quantity;
    }
}