package com.example.yummyrestaurant.utils;

import android.util.Log;

import com.example.yummyrestaurant.models.CartItem;
import com.example.yummyrestaurant.models.Coupon;
import com.example.yummyrestaurant.models.MenuItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Coupon 驗證工具類
 * - 驗證優惠券是否適用於購物車
 * - 提供詳細的禁用原因，用於用戶提示
 */
public class CouponValidator {
    private static final String TAG = "CouponValidator";

    /**
     * 代表優惠券驗證結果的內部類
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String reason; // 如果無效，提供禁用原因

        public ValidationResult(boolean isValid, String reason) {
            this.isValid = isValid;
            this.reason = reason;
        }
    }

    /**
     * 驗證優惠券是否適用於購物車，並返回詳細的驗證結果
     * @param coupon 待驗證的優惠券
     * @param requestedQty 請求使用的數量
     * @return ValidationResult 對象，包含驗證結果和禁用原因
     */
    public static ValidationResult validateCouponWithReason(Coupon coupon, int requestedQty) {
        if (coupon == null) {
            Log.w(TAG, "Coupon is null");
            return new ValidationResult(false, "Invalid coupon");
        }

        // --- 優惠券過期檢查 ---
        if (isCouponExpired(coupon)) {
            Log.w(TAG, "Coupon expired");
            return new ValidationResult(false, "This coupon has expired");
        }

        String appliesTo = coupon.getAppliesTo();
        int cartTotalCents = CartManager.getTotalAmountInCents();
        int scopedSubtotalCents = 0;

        boolean hasItemRestrictions =
                coupon.getApplicableItems() != null && !coupon.getApplicableItems().isEmpty();
        boolean hasCategoryRestrictions =
                coupon.getApplicableCategories() != null && !coupon.getApplicableCategories().isEmpty();

        boolean itemMatch = false;
        boolean categoryMatch = false;

        // --- 計算適用範圍內的小計 ---
        if ("category".equalsIgnoreCase(appliesTo) && hasCategoryRestrictions) {
            for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
                CartItem cartItem = entry.getKey();
                Integer qty = entry.getValue();
                Integer catId = cartItem.getCategoryId();

                if (catId != null && coupon.getApplicableCategories().contains(catId)) {
                    categoryMatch = true;
                    scopedSubtotalCents += cartItem.getPriceInCents() * (qty != null ? qty : 1);
                }
            }
        } else if ("item".equalsIgnoreCase(appliesTo) && hasItemRestrictions) {
            for (Map.Entry<CartItem, Integer> entry : CartManager.getCartItems().entrySet()) {
                CartItem cartItem = entry.getKey();
                Integer qty = entry.getValue();
                MenuItem mItem = cartItem.getMenuItem();
                Integer itemId = (mItem != null ? mItem.getId() : null);

                if (itemId != null && coupon.getApplicableItems().contains(itemId)) {
                    itemMatch = true;
                    scopedSubtotalCents += cartItem.getPriceInCents() * (qty != null ? qty : 1);
                }
            }
        } else {
            scopedSubtotalCents = cartTotalCents;
        }

        // --- 檢查是否有匹配的商品/分類 ---
        if (hasItemRestrictions || hasCategoryRestrictions) {
            boolean hasAnyMatch = ("item".equalsIgnoreCase(appliesTo) && itemMatch) ||
                    ("category".equalsIgnoreCase(appliesTo) && categoryMatch) ||
                    (itemMatch || categoryMatch);

            if (!hasAnyMatch) {
                Log.w(TAG, "Invalid: no matching items/categories");
                return new ValidationResult(false, "This coupon only applies to specific items or categories not in your cart");
            }
        }

        // --- 最低消費檢查 ---
        Double minSpend = coupon.getMinSpend();
        if (minSpend != null) {
            int thresholdCents = (int) Math.round(minSpend * 100);
            if (scopedSubtotalCents < thresholdCents) {
                double minSpendFormatted = minSpend;
                Log.w(TAG, "Invalid: below min spend");
                return new ValidationResult(false,
                        String.format("Minimum order value of HK$%.2f required", minSpendFormatted));
            }
        }

        // --- 訂單類型驗證 ---
        String orderType = CartManager.getOrderType();
        boolean validForOrderType =
                ("dine_in".equalsIgnoreCase(orderType) && coupon.isValidDineIn()) ||
                        ("takeaway".equalsIgnoreCase(orderType) && coupon.isValidTakeaway()) ||
                        ("delivery".equalsIgnoreCase(orderType) && coupon.isValidDelivery());

        if (!validForOrderType) {
            String orderTypeDisplay = "Dine-in";
            if ("takeaway".equalsIgnoreCase(orderType)) orderTypeDisplay = "Takeaway";
            else if ("delivery".equalsIgnoreCase(orderType)) orderTypeDisplay = "Delivery";

            Log.w(TAG, "Invalid: coupon not valid for order type");
            return new ValidationResult(false,
                    String.format("This coupon is not valid for %s orders", orderTypeDisplay));
        }

        // --- 生日檢查 ---
        if (coupon.isBirthdayOnly() && !RoleManager.isTodayUserBirthday()) {
            Log.w(TAG, "Invalid: not user's birthday");
            return new ValidationResult(false, "This coupon is only available on your birthday");
        }

        // --- 優惠券疊加檢查 ---
        if (!coupon.isCombineWithOtherDiscounts() && CartManager.hasOtherDiscountsApplied()) {
            Log.w(TAG, "Invalid: other discounts already applied");
            return new ValidationResult(false, "Cannot combine with other applied discounts");
        }

        Log.d(TAG, "Coupon validation passed for couponId=" + coupon.getCouponId());
        return new ValidationResult(true, "");
    }

    /**
     * 原始布爾驗證方法（向後兼容）
     */
    public static boolean isCouponValidForCart(Coupon coupon, int requestedQty) {
        ValidationResult result = validateCouponWithReason(coupon, requestedQty);
        return result.isValid;
    }

    /**
     * 檢查優惠券是否已過期
     * @param coupon 待檢查的優惠券
     * @return true 如果優惠券已過期，false 否則
     */
    private static boolean isCouponExpired(Coupon coupon) {
        if (coupon == null || coupon.getExpiryDate() == null || coupon.getExpiryDate().isEmpty()) {
            // 如果沒有設置過期日期，則視為未過期
            return false;
        }

        try {
            // 假設日期格式為 "yyyy-MM-dd"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate expiryDate = LocalDate.parse(coupon.getExpiryDate(), formatter);
            LocalDate today = LocalDate.now();

            // 如果過期日期在今天之前，則為過期
            boolean isExpired = expiryDate.isBefore(today);
            Log.d(TAG, "Coupon " + coupon.getCouponId() + " expiry: " + coupon.getExpiryDate() +
                    ", today: " + today + ", expired: " + isExpired);
            return isExpired;
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse expiry date: " + coupon.getExpiryDate(), e);
            // 如果解析失敗，默認視為有效
            return false;
        }
    }
}