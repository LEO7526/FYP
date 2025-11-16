package com.example.yummyrestaurant.utils;

import android.util.Log;

import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.Map;

public class CouponValidator {
    private static final String TAG = "CouponValidator";

    public static boolean isCouponValidForCart(Coupon coupon, int requestedQty) {
        if (coupon == null) {
            Log.w(TAG, "Coupon is null");
            return false;
        }

        String appliesTo = coupon.getAppliesTo(); // "whole_order", "category", "item", "package"
        int cartTotalCents = CartManager.getTotalAmountInCents();
        int scopedSubtotalCents = 0;

        boolean hasItemRestrictions =
                coupon.getApplicableItems() != null && !coupon.getApplicableItems().isEmpty();
        boolean hasCategoryRestrictions =
                coupon.getApplicableCategories() != null && !coupon.getApplicableCategories().isEmpty();

        boolean itemMatch = false;
        boolean categoryMatch = false;

        // --- Category scope ---
        if ("category".equalsIgnoreCase(appliesTo) && hasCategoryRestrictions) {
            for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
                CartItem cartItem = entry.getKey();
                Integer qty = entry.getValue();
                Integer catId = cartItem.getCategoryId();

                Log.d(TAG, "Cart categoryId=" + catId +
                        " applicableCategories=" + coupon.getApplicableCategories());

                if (catId != null && coupon.getApplicableCategories().contains(catId)) {
                    categoryMatch = true;
                    scopedSubtotalCents += cartItem.getPriceInCents() * (qty != null ? qty : 1);
                }
            }
            Log.d(TAG, "Scoped subtotal (category) = " + scopedSubtotalCents + " cents, match=" + categoryMatch);

            // --- Item scope ---
        } else if ("item".equalsIgnoreCase(appliesTo) && hasItemRestrictions) {
            Log.d(TAG, "Coupon applicable items raw=" + coupon.getApplicableItems());
            for (Object obj : coupon.getApplicableItems()) {
                Log.d(TAG, "Applicable item element: value=" + obj +
                        ", class=" + (obj != null ? obj.getClass().getName() : "null"));
            }

            for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
                CartItem cartItem = entry.getKey();
                Integer qty = entry.getValue();
                MenuItem mItem = cartItem.getMenuItem();
                Integer itemId = (mItem != null ? mItem.getId() : null);

                Log.d(TAG, "Checking cart item: id=" + itemId +
                        " (class=" + (itemId != null ? itemId.getClass().getName() : "null") + ")" +
                        ", name=" + (mItem != null ? mItem.getName() : "null") +
                        ", price=" + cartItem.getPriceInCents() +
                        ", qty=" + qty);

                boolean contains = (itemId != null && coupon.getApplicableItems().contains(itemId));
                Log.d(TAG, "contains() check: itemId=" + itemId + " -> " + contains);

                if (contains) {
                    itemMatch = true;
                    scopedSubtotalCents += cartItem.getPriceInCents() * (qty != null ? qty : 1);
                    Log.d(TAG, "Matched applicable itemId=" + itemId +
                            ", subtotal now=" + scopedSubtotalCents);
                }
            }
            Log.d(TAG, "Scoped subtotal (item) = " + scopedSubtotalCents + " cents, match=" + itemMatch);

            // --- Whole order scope ---
        } else {
            scopedSubtotalCents = cartTotalCents;
            Log.d(TAG, "Scoped subtotal (whole_order/default) = " + scopedSubtotalCents + " cents");
        }

        // --- Require at least one match if restrictions exist ---
        if (hasItemRestrictions || hasCategoryRestrictions) {
            boolean hasAnyMatch;
            if ("item".equalsIgnoreCase(appliesTo)) {
                hasAnyMatch = itemMatch;
            } else if ("category".equalsIgnoreCase(appliesTo)) {
                hasAnyMatch = categoryMatch;
            } else {
                hasAnyMatch = itemMatch || categoryMatch;
            }

            if (!hasAnyMatch) {
                Log.w(TAG, "Invalid: no matching items/categories in cart for couponId=" + coupon.getCouponId());
                return false;
            }
        }

        // --- Minimum spend ---
        Double minSpend = coupon.getMinSpend();
        if (minSpend != null) {
            int thresholdCents = (int) Math.round(minSpend * 100);
            if (scopedSubtotalCents < thresholdCents) {
                Log.w(TAG, "Invalid: below min spend (scope subtotal=" + scopedSubtotalCents +
                        " < threshold=" + thresholdCents + ")");
                return false;
            }
        }

        // --- Order type validity ---
        String orderType = CartManager.getOrderType();
        boolean validForOrderType =
                ("dine_in".equalsIgnoreCase(orderType) && coupon.isValidDineIn()) ||
                        ("takeaway".equalsIgnoreCase(orderType) && coupon.isValidTakeaway()) ||
                        ("delivery".equalsIgnoreCase(orderType) && coupon.isValidDelivery());

        Log.d(TAG, "OrderType=" + orderType +
                " flags: dineIn=" + coupon.isValidDineIn() +
                ", takeaway=" + coupon.isValidTakeaway() +
                ", delivery=" + coupon.isValidDelivery());

        if (!validForOrderType) {
            Log.w(TAG, "Invalid: coupon not valid for order type=" + orderType);
            return false;
        }

        // --- Birthday-only ---
        if (coupon.isBirthdayOnly() && !RoleManager.isTodayUserBirthday()) {
            Log.w(TAG, "Invalid: not user's birthday");
            return false;
        }

        // --- Stacking ---
        if (!coupon.isCombineWithOtherDiscounts() && CartManager.hasOtherDiscountsApplied()) {
            Log.w(TAG, "Invalid: other discounts already applied");
            return false;
        }

        // --- Per-day limit ---
        Integer perDayLimit = coupon.getPerCustomerPerDay();
        if (perDayLimit != null && perDayLimit > 0 && requestedQty > perDayLimit) {
            Log.w(TAG, "Invalid: requested exceeds per_customer_per_day limit");
            return false;
        }

        Log.d(TAG, "Coupon validation passed for couponId=" + coupon.getCouponId());
        return true;
    }
}
