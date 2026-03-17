package com.example.yummyrestaurant.utils;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

public final class AnimationUtils {

    private AnimationUtils() {
    }

    public static void animateItemEntry(View view, int position) {
        if (view == null) return;
        view.setTranslationY(48f);
        view.setAlpha(0f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(280)
                .setStartDelay(Math.min(position * 40L, 280L))
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
