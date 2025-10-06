package com.example.yummyrestaurant.utils;

import android.widget.TextView;

public class BadgeManager {

    private static TextView cartBadgeView;

    public static void registerBadgeView(TextView badgeView) {
        cartBadgeView = badgeView;
    }

    public static void updateCartBadge(int count) {
        if (cartBadgeView == null) return;

        if (count > 0) {
            String text = count > 99 ? "99+" : String.valueOf(count);
            cartBadgeView.setText(text);
            cartBadgeView.setVisibility(TextView.VISIBLE);
        } else {
            cartBadgeView.setVisibility(TextView.GONE);
        }
    }
}