package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

public class Coupon implements Parcelable {

    // Quantity is not from backend, managed locally
    private int quantity;

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    @SerializedName("coupon_id")
    private int couponId;

    private String title;
    private String description;

    private List<String> terms;

    @SerializedName(value = "points_required", alternate = {"requiredPoints"})
    private int pointsRequired;

    @SerializedName("expiry_date")
    private String expiryDate;

    @SerializedName(value = "discount_amount", alternate = {"discountAmount"})
    private int discountAmount;

    @SerializedName("type")
    private String type;

    @SerializedName(value = "item_category", alternate = {"itemCategory"})
    private String itemCategory;

    // Nested rules object
    @SerializedName("rules")
    private Rules rules;

    @SerializedName("applicable_items")
    private List<Integer> applicableItems;

    @SerializedName("applicable_categories")
    private List<Integer> applicableCategories;

    public Coupon() {}

    // --- Nested Rules class ---
    public static class Rules {
        @SerializedName("applies_to")
        private String appliesTo;

        @SerializedName("discount_type")
        private String discountType;

        @SerializedName("discount_value")
        private double discountValue;

        @SerializedName("min_spend")
        private Double minSpend;

        @SerializedName("valid_dine_in")
        private int validDineInRaw;

        @SerializedName("valid_takeaway")
        private int validTakeawayRaw;

        @SerializedName("valid_delivery")
        private int validDeliveryRaw;

        @SerializedName("combine_with_other_discounts")
        private int combineRaw;

        @SerializedName("birthday_only")
        private int birthdayRaw;

        // Getters
        public String getAppliesTo() { return appliesTo; }
        public String getDiscountType() { return discountType; }
        public double getDiscountValue() { return discountValue; }
        public Double getMinSpend() { return minSpend; }
        public boolean isValidDineIn() { return validDineInRaw == 1; }
        public boolean isValidTakeaway() { return validTakeawayRaw == 1; }
        public boolean isValidDelivery() { return validDeliveryRaw == 1; }
        public boolean isCombineWithOtherDiscounts() { return combineRaw == 1; }
        public boolean isBirthdayOnly() { return birthdayRaw == 1; }
    }

    // --- Getters (delegating to rules) ---
    public int getCouponId() { return couponId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getTerms() { return terms; }
    public int getPointsRequired() { return pointsRequired; }
    public String getExpiryDate() { return expiryDate; }
    public int getDiscountAmount() { return discountAmount; }
    public String getType() { return type; }
    public String getItemCategory() { return itemCategory; }
    public int getQuantity() {
        return Math.max(1, quantity);
    }
    public List<Integer> getApplicableItems() { return applicableItems; }
    public List<Integer> getApplicableCategories() { return applicableCategories; }

    public String getAppliesTo() { return rules != null ? rules.getAppliesTo() : null; }
    public String getDiscountType() { return rules != null ? rules.getDiscountType() : null; }
    public double getDiscountValue() { return rules != null ? rules.getDiscountValue() : 0; }
    public Double getMinSpend() { return rules != null ? rules.getMinSpend() : null; }
    public boolean isValidDineIn() { return rules != null && rules.isValidDineIn(); }
    public boolean isValidTakeaway() { return rules != null && rules.isValidTakeaway(); }
    public boolean isValidDelivery() { return rules != null && rules.isValidDelivery(); }
    public boolean isCombineWithOtherDiscounts() { return rules != null && rules.isCombineWithOtherDiscounts(); }
    public boolean isBirthdayOnly() { return rules != null && rules.isBirthdayOnly(); }

    // --- Parcelable implementation ---
    protected Coupon(Parcel in) {
        couponId = in.readInt();
        title = in.readString();
        description = in.readString();
        terms = in.createStringArrayList();
        pointsRequired = in.readInt();
        expiryDate = in.readString();
        discountAmount = in.readInt();
        type = in.readString();
        itemCategory = in.readString();
        quantity = in.readInt();

        // Read Rules
        if (in.readByte() == 1) {
            rules = new Rules();
            rules.appliesTo = in.readString();
            rules.discountType = in.readString();
            rules.discountValue = in.readDouble();
            if (in.readByte() == 1) {
                rules.minSpend = in.readDouble();
            } else {
                rules.minSpend = null;
            }
            rules.validDineInRaw = in.readInt();
            rules.validTakeawayRaw = in.readInt();
            rules.validDeliveryRaw = in.readInt();
            rules.combineRaw = in.readInt();
            rules.birthdayRaw = in.readInt();
        }

        int[] itemsArray = in.createIntArray();
        if (itemsArray != null) {
            applicableItems = new java.util.ArrayList<>(itemsArray.length);
            for (int value : itemsArray) {
                applicableItems.add(value);
            }
        }

        int[] categoriesArray = in.createIntArray();
        if (categoriesArray != null) {
            applicableCategories = new java.util.ArrayList<>(categoriesArray.length);
            for (int value : categoriesArray) {
                applicableCategories.add(value);
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(couponId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeStringList(terms);
        dest.writeInt(pointsRequired);
        dest.writeString(expiryDate);
        dest.writeInt(discountAmount);
        dest.writeString(type);
        dest.writeString(itemCategory);
        dest.writeInt(quantity);

        // Write Rules
        if (rules != null) {
            dest.writeByte((byte) 1);
            dest.writeString(rules.appliesTo);
            dest.writeString(rules.discountType);
            dest.writeDouble(rules.discountValue);
            if (rules.minSpend != null) {
                dest.writeByte((byte) 1);
                dest.writeDouble(rules.minSpend);
            } else {
                dest.writeByte((byte) 0);
            }
            dest.writeInt(rules.validDineInRaw);
            dest.writeInt(rules.validTakeawayRaw);
            dest.writeInt(rules.validDeliveryRaw);
            dest.writeInt(rules.combineRaw);
            dest.writeInt(rules.birthdayRaw);
        } else {
            dest.writeByte((byte) 0);
        }

        if (applicableItems != null) {
            int[] arr = new int[applicableItems.size()];
            for (int i = 0; i < applicableItems.size(); i++) {
                arr[i] = applicableItems.get(i);
            }
            dest.writeIntArray(arr);
        } else {
            dest.writeIntArray(null);
        }

        if (applicableCategories != null) {
            int[] arr = new int[applicableCategories.size()];
            for (int i = 0; i < applicableCategories.size(); i++) {
                arr[i] = applicableCategories.get(i);
            }
            dest.writeIntArray(arr);
        } else {
            dest.writeIntArray(null);
        }
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Coupon> CREATOR = new Creator<Coupon>() {
        @Override
        public Coupon createFromParcel(Parcel in) { return new Coupon(in); }
        @Override
        public Coupon[] newArray(int size) { return new Coupon[size]; }
    };
}