package com.example.yummyrestaurant.utils;

import android.util.Log;

import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.MenuItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Coupon validation helper.
 */
public class CouponValidator {
    private static final String TAG = "CouponValidator";

    /**
     * Validation result with an optional reason.
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String reason;

        public ValidationResult(boolean isValid, String reason) {
            this.isValid = isValid;
            this.reason = reason;
        }
    }

    /**
     * Validate whether a coupon can be applied to the current cart.
     */
    public static ValidationResult validateCouponWithReason(Coupon coupon, int requestedQty) {
        if (coupon == null) {
            Log.w(TAG, "Coupon is null");
            return new ValidationResult(false, "Invalid coupon");
        }

        if (isCouponExpired(coupon)) {
            Log.w(TAG, "Coupon expired");
            return new ValidationResult(false, "This coupon has expired");
        }

        int maxUsableQty = getMaxUsableQuantityForCart(coupon);
        if (requestedQty > maxUsableQty) {
            Log.w(TAG, "Invalid: requested quantity exceeds cart eligibility, requested="
                + requestedQty + ", maxUsable=" + maxUsableQty);
            return new ValidationResult(false,
                String.format("You can only use %d of this coupon for your current cart", maxUsableQty));
        }

        int cartTotalCents = CartManager.getTotalAmountInCents();
        int scopedSubtotalCents = 0;

        boolean hasItemRestrictions =
                coupon.getApplicableItems() != null && !coupon.getApplicableItems().isEmpty();
        boolean hasCategoryRestrictions =
                coupon.getApplicableCategories() != null && !coupon.getApplicableCategories().isEmpty();
        boolean itemMatch = false;
        boolean categoryMatch = false;

        for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
            CartItem cartItem = entry.getKey();
            Integer qty = entry.getValue();
            int quantity = qty != null ? qty : 1;

            Integer categoryId = cartItem.getCategoryId();
            MenuItem menuItem = cartItem.getMenuItem();
            Integer itemId = menuItem != null ? menuItem.getId() : null;

            boolean matchesCategory = hasCategoryRestrictions
                    && categoryId != null
                    && coupon.getApplicableCategories().contains(categoryId);
            boolean matchesItem = hasItemRestrictions
                    && itemId != null
                    && coupon.getApplicableItems().contains(itemId);

            if (matchesCategory) {
                categoryMatch = true;
            }
            if (matchesItem) {
                itemMatch = true;
            }
            if (matchesCategory || matchesItem) {
                scopedSubtotalCents += cartItem.getPriceInCents() * quantity;
            }
        }

        if (!hasCategoryRestrictions && !hasItemRestrictions) {
            scopedSubtotalCents = cartTotalCents;
        }

        boolean itemCategoryNameMatch = false;
        if ((hasItemRestrictions || hasCategoryRestrictions) && !itemMatch && !categoryMatch) {
            String couponItemCategory = coupon.getItemCategory();
            if (couponItemCategory != null && !couponItemCategory.isEmpty()) {
                for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
                    CartItem cartItem = entry.getKey();
                    String cartCategoryName = cartItem.getCategory();
                    if (cartCategoryName != null
                            && couponItemCategory.equalsIgnoreCase(cartCategoryName.trim())) {
                        Integer qty = entry.getValue();
                        int quantity = qty != null ? qty : 1;
                        itemCategoryNameMatch = true;
                        scopedSubtotalCents += cartItem.getPriceInCents() * quantity;
                    }
                }
                if (itemCategoryNameMatch) {
                    Log.d(TAG, "item_category fallback matched: " + couponItemCategory);
                }
            }
        }

        if (hasItemRestrictions || hasCategoryRestrictions) {
            if (!itemMatch && !categoryMatch && !itemCategoryNameMatch) {
                Log.w(TAG, "Invalid: no matching items or categories in cart");
                return new ValidationResult(false,
                        "This coupon only applies to specific items or categories not in your cart");
            }
        }

        Double minSpend = coupon.getMinSpend();
        if (minSpend != null) {
            int thresholdCents = (int) Math.round(minSpend * 100);
            if (scopedSubtotalCents < thresholdCents) {
                Log.w(TAG, "Invalid: below min spend");
                return new ValidationResult(false,
                        String.format("Minimum order value of HK$%.2f required", minSpend));
            }
        }

        String orderType = CartManager.getOrderType();
        boolean validForOrderType =
                ("dine_in".equalsIgnoreCase(orderType) && coupon.isValidDineIn())
                        || ("takeaway".equalsIgnoreCase(orderType) && coupon.isValidTakeaway())
                        || ("delivery".equalsIgnoreCase(orderType) && coupon.isValidDelivery());

        if (!validForOrderType) {
            String orderTypeDisplay = "Dine-in";
            if ("takeaway".equalsIgnoreCase(orderType)) {
                orderTypeDisplay = "Takeaway";
            } else if ("delivery".equalsIgnoreCase(orderType)) {
                orderTypeDisplay = "Delivery";
            }

            Log.w(TAG, "Invalid: coupon not valid for order type");
            return new ValidationResult(false,
                    String.format("This coupon is not valid for %s orders", orderTypeDisplay));
        }

        if (coupon.isBirthdayOnly() && !RoleManager.isTodayUserBirthday()) {
            Log.w(TAG, "Invalid: not user's birthday");
            return new ValidationResult(false, "This coupon is only available on your birthday");
        }

        if (!coupon.isCombineWithOtherDiscounts() && CartManager.hasOtherDiscountsApplied()) {
            Log.w(TAG, "Invalid: other discounts already applied");
            return new ValidationResult(false, "Cannot combine with other applied discounts");
        }

        Log.d(TAG, "Coupon validation passed for couponId=" + coupon.getCouponId());
        return new ValidationResult(true, "");
    }

    /**
     * Backward-compatible boolean validation API.
     */
    public static boolean isCouponValidForCart(Coupon coupon, int requestedQty) {
        ValidationResult result = validateCouponWithReason(coupon, requestedQty);
        return result.isValid;
    }

    /**
     * Return the maximum coupon quantity that can be used with the current cart.
     * This caps free-item and item/category-scoped coupons by the matching cart quantity.
     */
    public static int getMaxUsableQuantityForCart(Coupon coupon) {
        if (coupon == null) {
            return 0;
        }

        int ownedQuantity = Math.max(1, coupon.getQuantity());
        int eligibleQuantity = getEligibleCartQuantity(coupon);

        if (eligibleQuantity > 0) {
            return Math.min(ownedQuantity, eligibleQuantity);
        }

        return ownedQuantity;
    }

    private static int getEligibleCartQuantity(Coupon coupon) {
        boolean hasItemRestrictions =
                coupon.getApplicableItems() != null && !coupon.getApplicableItems().isEmpty();
        boolean hasCategoryRestrictions =
                coupon.getApplicableCategories() != null && !coupon.getApplicableCategories().isEmpty();

        int eligibleQuantity = 0;

        for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
            CartItem cartItem = entry.getKey();
            Integer qty = entry.getValue();
            int quantity = qty != null ? qty : 1;

            Integer categoryId = cartItem.getCategoryId();
            MenuItem menuItem = cartItem.getMenuItem();
            Integer itemId = menuItem != null ? menuItem.getId() : null;

            boolean matchesCategory = hasCategoryRestrictions
                    && categoryId != null
                    && coupon.getApplicableCategories().contains(categoryId);
            boolean matchesItem = hasItemRestrictions
                    && itemId != null
                    && coupon.getApplicableItems().contains(itemId);

            if (matchesCategory || matchesItem) {
                eligibleQuantity += quantity;
            }
        }

        if (eligibleQuantity > 0) {
            return eligibleQuantity;
        }

        if (hasItemRestrictions || hasCategoryRestrictions) {
            String couponItemCategory = coupon.getItemCategory();
            if (couponItemCategory != null && !couponItemCategory.isEmpty()) {
                for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
                    CartItem cartItem = entry.getKey();
                    String cartCategoryName = cartItem.getCategory();
                    if (cartCategoryName != null
                            && couponItemCategory.equalsIgnoreCase(cartCategoryName.trim())) {
                        Integer qty = entry.getValue();
                        int quantity = qty != null ? qty : 1;
                        eligibleQuantity += quantity;
                    }
                }
            }
        }

        return eligibleQuantity;
    }

    /**
     * Check whether a coupon is expired.
     */
    private static boolean isCouponExpired(Coupon coupon) {
        if (coupon == null || coupon.getExpiryDate() == null || coupon.getExpiryDate().isEmpty()) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate expiryDate = LocalDate.parse(coupon.getExpiryDate(), formatter);
            LocalDate today = LocalDate.now();

            boolean isExpired = expiryDate.isBefore(today);
            Log.d(TAG, "Coupon " + coupon.getCouponId() + " expiry: " + coupon.getExpiryDate()
                    + ", today: " + today + ", expired: " + isExpired);
            return isExpired;
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse expiry date: " + coupon.getExpiryDate(), e);
            return false;
        }
    }
}