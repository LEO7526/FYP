package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;

public class Coupon {

    @SerializedName("coupon_id")
    private int coupon_id;

    private String title;
    private String description;

    @SerializedName("points_required")
    private int points_required;

    @SerializedName("expiry_date")
    private String expiry_date;

    @SerializedName("discount_amount")
    private int discount_amount;   // in cents for cash, or percentage for percent

    @SerializedName("type")
    private String type;           // "cash", "percent", "free_item"

    @SerializedName("item_category")
    private String itemCategory;   // e.g. "drink" for free drink

    private int quantity;          // number of coupons owned

    // Empty constructor required for Retrofit/Gson
    public Coupon() {}

    // --- Getters ---
    public int getCoupon_id() { return coupon_id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getPoints_required() { return points_required; }
    public String getExpiry_date() { return expiry_date; }
    public int getDiscount_amount() { return discount_amount; }
    public String getType() { return type; }
    public String getItemCategory() { return itemCategory; }
    public int getQuantity() { return quantity; }

    // --- Setters ---
    public void setCoupon_id(int coupon_id) { this.coupon_id = coupon_id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPoints_required(int points_required) { this.points_required = points_required; }
    public void setExpiry_date(String expiry_date) { this.expiry_date = expiry_date; }
    public void setDiscount_amount(int discount_amount) { this.discount_amount = discount_amount; }
    public void setType(String type) { this.type = type; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}