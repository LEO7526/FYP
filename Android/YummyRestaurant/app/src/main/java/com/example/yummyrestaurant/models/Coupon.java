package com.example.yummyrestaurant.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;

public class Coupon implements Parcelable {

    private boolean redeemable = true;

    public boolean isRedeemable() { return redeemable; }
    public void setRedeemable(boolean redeemable) { this.redeemable = redeemable; }

    @SerializedName(value = "quantity", alternate = {"redemption_count"})
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

//    @SerializedName("per_customer_per_day")
//    private Integer perCustomerPerDay;

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

    // Top-level flags (for API responses without nested rules)
    @SerializedName("valid_dine_in") private Boolean validDineIn;
    @SerializedName("valid_takeaway") private Boolean validTakeaway;
    @SerializedName("valid_delivery") private Boolean validDelivery;
    @SerializedName("combine_with_other_discounts") private Boolean combineRawTop;
    @SerializedName("birthday_only") private Boolean birthdayRawTop;

    // Top-level rule fields
    @SerializedName("applies_to") private String appliesToTop;
    @SerializedName("discount_type") private String discountTypeTop;
    @SerializedName("discount_value") private Double discountValueTop;
    @SerializedName("min_spend") private Double minSpendTop;
    public String getAppliesTo() {
        if (rules != null && !rules.getAppliesTo().isEmpty()) {
            return rules.getAppliesTo();
        }
        return appliesToTop != null ? appliesToTop : "";
    }

    public String getDiscountType() {
        if (rules != null && !rules.getDiscountType().isEmpty()) {
            return rules.getDiscountType();
        }
        return discountTypeTop != null ? discountTypeTop : (type != null ? type : "");
    }

    public double getDiscountValue() {
        if (rules != null) return rules.getDiscountValue();
        if (discountValueTop != null) return discountValueTop;
        return discountAmount;
    }

    public Double getMinSpend() {
        if (rules != null) return rules.getMinSpend();
        return minSpendTop;
    }

    public Coupon() {}

    // --- Nested Rules class ---
    public static class Rules {
        @SerializedName("applies_to") private String appliesTo;
        @SerializedName("discount_type") private String discountType;
        @SerializedName("discount_value") private double discountValue;
        @SerializedName("min_spend") private Double minSpend;
        @SerializedName("valid_dine_in") private int validDineInRaw;
        @SerializedName("valid_takeaway") private int validTakeawayRaw;
        @SerializedName("valid_delivery") private int validDeliveryRaw;
        @SerializedName("combine_with_other_discounts") private int combineRaw;
        @SerializedName("birthday_only") private int birthdayRaw;

        public String getAppliesTo() { return appliesTo != null ? appliesTo : ""; }
        public String getDiscountType() { return discountType != null ? discountType : ""; }
        public double getDiscountValue() { return discountValue; }
        public Double getMinSpend() { return minSpend; }
        public boolean isValidDineIn() { return validDineInRaw == 1; }
        public boolean isValidTakeaway() { return validTakeawayRaw == 1; }
        public boolean isValidDelivery() { return validDeliveryRaw == 1; }
        public boolean isCombineWithOtherDiscounts() { return combineRaw == 1; }
        public boolean isBirthdayOnly() { return birthdayRaw == 1; }
    }
    public int getCouponId() { return couponId; }
    public String getTitle() { return title != null ? title : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public List<String> getTerms() { return terms; }
    public int getPointsRequired() { return pointsRequired; }
    public String getExpiryDate() { return expiryDate != null ? expiryDate : ""; }
    public int getDiscountAmount() { return discountAmount; }
    public String getType() { return type != null ? type : ""; }
    public String getItemCategory() { return itemCategory != null ? itemCategory : ""; }
    public int getQuantity() { return Math.max(1, quantity); }
    public List<Integer> getApplicableItems() { return applicableItems; }
    public List<Integer> getApplicableCategories() { return applicableCategories; }

//    public void setPerCustomerPerDay(Integer value) { this.perCustomerPerDay = value; }
//    public Integer getPerCustomerPerDay() { return perCustomerPerDay; }

    public boolean isValidDineIn() {
        if (rules != null) return rules.isValidDineIn();
        return validDineIn != null && validDineIn;
    }
    public boolean isValidTakeaway() {
        if (rules != null) return rules.isValidTakeaway();
        return validTakeaway != null && validTakeaway;
    }
    public boolean isValidDelivery() {
        if (rules != null) return rules.isValidDelivery();
        return validDelivery != null && validDelivery;
    }
    public boolean isCombineWithOtherDiscounts() {
        if (rules != null) return rules.isCombineWithOtherDiscounts();
        return combineRawTop != null && combineRawTop;
    }
    public boolean isBirthdayOnly() {
        if (rules != null) return rules.isBirthdayOnly();
        return birthdayRawTop != null && birthdayRawTop;
    }
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

        // Read top-level rule fields
        appliesToTop = in.readString();
        discountTypeTop = in.readString();
        discountValueTop = (Double) in.readValue(Double.class.getClassLoader());
        minSpendTop = (Double) in.readValue(Double.class.getClassLoader());

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
            applicableItems = new ArrayList<>(itemsArray.length);
            for (int value : itemsArray) {
                applicableItems.add(value);
            }
        }

        int[] categoriesArray = in.createIntArray();
        if (categoriesArray != null) {
            applicableCategories = new ArrayList<>(categoriesArray.length);
            for (int value : categoriesArray) {
                applicableCategories.add(value);
            }
        }

        // Read top-level flags
        validDineIn = (Boolean) in.readValue(Boolean.class.getClassLoader());
        validTakeaway = (Boolean) in.readValue(Boolean.class.getClassLoader());
        validDelivery = (Boolean) in.readValue(Boolean.class.getClassLoader());
        combineRawTop = (Boolean) in.readValue(Boolean.class.getClassLoader());
        birthdayRawTop = (Boolean) in.readValue(Boolean.class.getClassLoader());
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

        // Write top-level rule fields
        dest.writeString(appliesToTop);
        dest.writeString(discountTypeTop);
        dest.writeValue(discountValueTop);
        dest.writeValue(minSpendTop);

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

        // Write applicable items/categories
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

        // Write top-level flags
        dest.writeValue(validDineIn);
        dest.writeValue(validTakeaway);
        dest.writeValue(validDelivery);
        dest.writeValue(combineRawTop);
        dest.writeValue(birthdayRawTop);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Coupon> CREATOR = new Creator<Coupon>() {
        @Override
        public Coupon createFromParcel(Parcel in) {
            return new Coupon(in);
        }

        @Override
        public Coupon[] newArray(int size) {
            return new Coupon[size];
        }
    };
}
