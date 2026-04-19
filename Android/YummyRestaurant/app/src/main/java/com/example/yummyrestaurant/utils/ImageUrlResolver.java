package com.example.yummyrestaurant.utils;

import com.example.yummyrestaurant.BuildConfig;
import com.example.yummyrestaurant.api.ApiConfig;

public final class ImageUrlResolver {
    private static final String GITHUB_RAW_PREFIX = "https://raw.githubusercontent.com/LEO7526/FYP/main/Image/";
    private static final String PROJECT_API_SEGMENT = "/Database/projectapi/";
    private static final String IMAGE_SEGMENT = "/Image/";

    private ImageUrlResolver() {
    }

    public static String resolve(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return imageUrl;
        }

        String trimmedUrl = imageUrl.trim();

        if (BuildConfig.DEBUG && trimmedUrl.startsWith(GITHUB_RAW_PREFIX)) {
            String localImagePrefix = resolveLocalImagePrefix();
            return localImagePrefix + trimmedUrl.substring(GITHUB_RAW_PREFIX.length());
        }

        return trimmedUrl;
    }

    private static String resolveLocalImagePrefix() {
        String apiBaseUrl = ApiConfig.getBaseUrl();
        if (apiBaseUrl == null || apiBaseUrl.trim().isEmpty()) {
            return "http://10.0.2.2/newFolder/Image/";
        }

        String normalizedApiBase = apiBaseUrl.trim();
        String imageBase = normalizedApiBase.replace(PROJECT_API_SEGMENT, IMAGE_SEGMENT);

        if (!imageBase.endsWith("/")) {
            imageBase += "/";
        }

        return imageBase;
    }
}