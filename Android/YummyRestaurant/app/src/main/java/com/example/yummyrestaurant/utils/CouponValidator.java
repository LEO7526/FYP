package com.example.yummyrestaurant.utils;

import android.util.Log;
import android.widget.Toast;

import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Coupon;

public class CouponValidator {
    private static final String TAG = "CouponValidator";

    public static boolean isCouponValidForCart(Coupon coupon, int requestedQty) {
        if (coupon == null) {
            Log.w(TAG, "Coupon is null");
            return false;
        }

        int totalCents = CartManager.getTotalAmountInCents();

        // Minimum spend
        Double minSpend = coupon.getMinSpend();
        if (minSpend != null && totalCents < (int) Math.round(minSpend * 100)) {
            Log.w(TAG, "Invalid: below min spend");
            return false;
        }

        // Applies-to / order type
        String appliesTo = coupon.getAppliesTo();
        String orderType = CartManager.getOrderType();
        boolean appliesToAll = (appliesTo == null) || appliesTo.trim().isEmpty();
        if (!appliesToAll && !appliesTo.equalsIgnoreCase(orderType)) {
            Log.w(TAG, "Invalid: not valid for " + orderType);
            return false;
        }

        // Birthday-only
        if (coupon.isBirthdayOnly() && !RoleManager.isTodayUserBirthday()) {
            Log.w(TAG, "Invalid: not user's birthday");
            return false;
        }

        // Stacking
        if (!coupon.isCombineWithOtherDiscounts() && CartManager.hasOtherDiscountsApplied()) {
            Log.w(TAG, "Invalid: other discounts already applied");
            return false;
        }

        // Per-day limit
        Integer perDayLimit = coupon.getPerCustomerPerDay();
        if (perDayLimit != null && perDayLimit > 0 && requestedQty > perDayLimit) {
            Log.w(TAG, "Invalid: requested exceeds per_customer_per_day limit");
            return false;
        }

        // --- Applicable items/categories check ---
        boolean itemMatch = false;
        boolean categoryMatch = false;

        for (CartItem cartItem : CartManager.getCartItems().keySet()) {
            Integer itemId = cartItem.getMenuItemId();
            Integer catId = cartItem.getCategoryId();

            // Check specific applicable items
            if (coupon.getApplicableItems() != null && !coupon.getApplicableItems().isEmpty()) {
                if (itemId != null && coupon.getApplicableItems().contains(itemId)) {
                    itemMatch = true;
                }
            }

            // Check applicable categories
            if (coupon.getApplicableCategories() != null && !coupon.getApplicableCategories().isEmpty()) {
                if (catId != null && coupon.getApplicableCategories().contains(catId)) {
                    categoryMatch = true;
                }
            }
        }

        // If coupon has restrictions, require at least one match
        if ((coupon.getApplicableItems() != null && !coupon.getApplicableItems().isEmpty()) ||
                (coupon.getApplicableCategories() != null && !coupon.getApplicableCategories().isEmpty())) {

            if (!itemMatch && !categoryMatch) {
                Log.w(TAG, "Invalid: no matching items/categories in cart for couponId=" + coupon.getCouponId());
                return false;
            }
        }

        Log.d(TAG, "Coupon validation passed for couponId=" + coupon.getCouponId());
        return true;
    }


}
