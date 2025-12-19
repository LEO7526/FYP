package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderPackage {
    @SerializedName("package_id")
    private int packageId;

    @SerializedName("package_name")
    private String packageName;

    @SerializedName("package_price")
    private double packagePrice;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("note")
    private String note;

    @SerializedName("dishes")
    private List<OrderPackageDish> dishes;

    @SerializedName("packageCost")
    private double packageCost;

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public double getPackagePrice() {
        return packagePrice;
    }

    public void setPackagePrice(double packagePrice) {
        this.packagePrice = packagePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<OrderPackageDish> getDishes() {
        return dishes;
    }

    public void setDishes(List<OrderPackageDish> dishes) {
        this.dishes = dishes;
    }

    public double getPackageCost() {
        return packageCost;
    }

    public void setPackageCost(double packageCost) {
        this.packageCost = packageCost;
    }

    public int getDishCount() {
        return dishes != null ? dishes.size() : 0;
    }
}
