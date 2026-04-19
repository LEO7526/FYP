package com.example.yummyrestaurant.utils;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.example.yummyrestaurant.R;

public final class AnimationUtils {

    private AnimationUtils() {
    }

    public static void animateItemEntry(View view, int position) {
        if (view == null) return;
        view.animate().cancel();

        Object animatedTag = view.getTag(R.id.tag_item_entry_animated);
        if (Boolean.TRUE.equals(animatedTag)) {
            view.setTranslationY(0f);
            view.setAlpha(1f);
            return;
        }

        view.setTag(R.id.tag_item_entry_animated, true);
        view.setTranslationY(24f);
        view.setAlpha(0f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(200)
                .setStartDelay(Math.min(Math.max(position, 0) * 20L, 120L))
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
