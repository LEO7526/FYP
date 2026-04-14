package com.example.yummyrestaurant.utils;

import com.example.yummyrestaurant.BuildConfig;

public final class ImageUrlResolver {
    private static final String GITHUB_RAW_PREFIX = "https://raw.githubusercontent.com/LEO7526/FYP/main/Image/";
    private static final String LOCAL_IMAGE_PREFIX = "http://10.0.2.2/newFolder/Image/";

    private ImageUrlResolver() {
    }

    public static String resolve(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return imageUrl;
        }

        String trimmedUrl = imageUrl.trim();

        if (BuildConfig.DEBUG && trimmedUrl.startsWith(GITHUB_RAW_PREFIX)) {
            return LOCAL_IMAGE_PREFIX + trimmedUrl.substring(GITHUB_RAW_PREFIX.length());
        }

        return trimmedUrl;
    }
}