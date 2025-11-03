package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Coupon {

    @SerializedName("coupon_id")
    private int couponId;

    // From coupon_translation
    private String title;
    private String description;

    // Multiple terms (from coupon_terms)
    private List<String> terms;

    @SerializedName("points_required")
    private int pointsRequired;

    @SerializedName("expiry_date")
    private String expiryDate;

    @SerializedName("discount_amount")
    private int discountAmount;   // in cents for cash, or percentage for percent

    @SerializedName("type")
    private String type;          // "cash", "percent", "free_item"

    @SerializedName("item_category")
    private String itemCategory;  // e.g. "drink" for free drink

    private int quantity;         // number of coupons owned (if applicable)

    // Empty constructor required for Retrofit/Gson
    public Coupon() {}

    // --- Getters ---
    public int getCouponId() { return couponId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getTerms() { return terms; }
    public int getPointsRequired() { return pointsRequired; }
    public String getExpiryDate() { return expiryDate; }
    public int getDiscountAmount() { return discountAmount; }
    public String getType() { return type; }
    public String getItemCategory() { return itemCategory; }
    public int getQuantity() { return quantity; }

    // --- Setters ---
    public void setCouponId(int couponId) { this.couponId = couponId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setTerms(List<String> terms) { this.terms = terms; }
    public void setPointsRequired(int pointsRequired) { this.pointsRequired = pointsRequired; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setDiscountAmount(int discountAmount) { this.discountAmount = discountAmount; }
    public void setType(String type) { this.type = type; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
