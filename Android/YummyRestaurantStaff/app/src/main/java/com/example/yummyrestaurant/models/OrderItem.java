package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("customizations")
    private List<OrderItemCustomization> customizations;

    // 🔥 NEW: Package support fields
    @SerializedName("isPackage")
    private boolean isPackage;

    @SerializedName("packageId")
    private int packageId;

    @SerializedName("packageItems")
    private List<OrderItem> packageItems;

    @SerializedName("isPackageItem")
    private boolean isPackageItem;

    @SerializedName("parentPackageId")
    private int parentPackageId;

    public OrderItem(int itemId, String name, int quantity, double itemPrice, double itemCost) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
        this.itemCost = itemCost;
        this.isPackage = false;
        this.isPackageItem = false;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<OrderItemCustomization> getCustomizations() {
        return customizations;
    }

    public void setCustomizations(List<OrderItemCustomization> customizations) {
        this.customizations = customizations;
    }

    // 🔥 NEW: Package support getters and setters
    public boolean isPackage() {
        return isPackage;
    }

    public void setPackage(boolean isPackage) {
        this.isPackage = isPackage;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public List<OrderItem> getPackageItems() {
        return packageItems;
    }

    public void setPackageItems(List<OrderItem> packageItems) {
        this.packageItems = packageItems;
    }

    public boolean isPackageItem() {
        return isPackageItem;
    }

    public void setPackageItem(boolean isPackageItem) {
        this.isPackageItem = isPackageItem;
    }

    public int getParentPackageId() {
        return parentPackageId;
    }

    public void setParentPackageId(int parentPackageId) {
        this.parentPackageId = parentPackageId;
    }

    // Optional: calculate subtotal dynamically
    public double getTotalCost() {
        return itemPrice * quantity;
    }
}