package com.example.yummyrestaurant.models;

import java.util.List;

public class CouponDetailResponse {
    private boolean success;
    private Coupon coupon;
    private String error; // optional error message

    public boolean isSuccess() {
        return success;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public String getError() {
        return error;
    }

    // Nested Coupon class
    public static class Coupon {
        private int coupon_id;
        private String title;
        private String description;
        private int requiredPoints;
        private String type;             // "cash", "percent", "free_item"
        private int discountAmount;      // in cents or percentage
        private String itemCategory;     // e.g. "drink"
        private String expiry_date;      // formatted as yyyy-MM-dd
        private List<String> terms;      // 👈 bullet-point terms

        public int getCouponId() {
            return coupon_id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getRequiredPoints() {
            return requiredPoints;
        }

        public String getType() {
            return type;
        }

        public int getDiscountAmount() {
            return discountAmount;
        }

        public String getItemCategory() {
            return itemCategory;
        }

        public String getExpiryDate() {
            return expiry_date;
        }

        public List<String> getTerms() {
            return terms;
        }
    }
}
