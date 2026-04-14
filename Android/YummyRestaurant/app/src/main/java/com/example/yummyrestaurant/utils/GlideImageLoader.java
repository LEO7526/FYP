package com.example.yummyrestaurant.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.imageview.ShapeableImageView;
import android.util.Log;
import com.bumptech.glide.request.RequestOptions;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.utils.ImageUrlResolver;

/**
 * Centralized Glide image loading utility with enhanced diagnostics
 * Provides consistent image loading with detailed error logging
 */
public class GlideImageLoader {
    private static final String TAG = "GlideImageLoader";

    /**
     * Load image with error handling and diagnostics
     */
    public static void loadImage(Context context, String imageUrl, ShapeableImageView imageView) {
        loadImage(context, imageUrl, imageView, R.drawable.placeholder, R.drawable.error_image);
    }

    /**
     * Load image with custom placeholders
     */
    public static void loadImage(Context context, String imageUrl, ShapeableImageView imageView,
                                  int placeholderId, int errorId) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.w(TAG, "Image URL is null or empty");
            imageView.setImageResource(errorId);
            return;
        }

        Log.d(TAG, "Loading image from: " + imageUrl);

        Glide.with(context)
            .load(ImageUrlResolver.resolve(imageUrl))
                .apply(new RequestOptions()
                        .placeholder(placeholderId)
                        .error(errorId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .timeout(10000)) // 10 second timeout
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e,
                                                Object model, Target<Drawable> target, boolean isFirstResource) {
                        logLoadFailure(imageUrl, e);
                        return false; // Allow default error handling
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "Image loaded successfully from: " + imageUrl);
                        return false;
                    }
                })
                .into(imageView);
    }

    /**
     * Enhanced log failure with detailed diagnostics
     */
    private static void logLoadFailure(String imageUrl, @Nullable com.bumptech.glide.load.engine.GlideException exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("Glide image load failed\n")
                .append("  URL: ").append(imageUrl).append("\n")
                .append("  Throwable: ");

        if (exception != null) {
            sb.append(exception.getMessage()).append("\n");
            for (Throwable cause : exception.getRootCauses()) {
                sb.append("    Root cause: ").append(cause.getClass().getSimpleName())
                        .append(" - ").append(cause.getMessage()).append("\n");
            }
        } else {
            sb.append("Unknown error\n");
        }

        Log.e(TAG, sb.toString());

        // Log network-specific diagnostics
        if (imageUrl.contains("github")) {
            Log.w(TAG, "GitHub image load failed - possible network/DNS issue");
            Log.w(TAG, "Ensure: 1) Device has internet 2) GitHub is accessible 3) URL is valid");
        }
    }

    /**
     * Preload and cache image for later use
     */
    public static void preloadImage(Context context, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        Log.d(TAG, "Preloading image: " + imageUrl);
        Glide.with(context)
            .load(ImageUrlResolver.resolve(imageUrl))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload();
    }

    /**
     * Clear all Glide memory and disk caches
     */
    public static void clearCache(Context context) {
        Log.d(TAG, "Clearing Glide cache");
        new Thread(() -> Glide.get(context).clearDiskCache()).start();
        Glide.get(context).clearMemory();
    }

    /**
     * Get diagnostic info about current network/image loading state
     */
    public static String getDiagnosticInfo() {
        return "GlideImageLoader v1.0 - Image loading diagnostics enabled\n"
                + "Network Security Config: Configured\n"
                + "Timeout: 10 seconds\n"
                + "Cache Strategy: DiskCacheStrategy.ALL";
    }
}
