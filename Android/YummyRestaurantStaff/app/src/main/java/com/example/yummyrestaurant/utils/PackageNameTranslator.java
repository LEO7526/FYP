package com.example.yummyrestaurant.utils;

import android.content.Context;

public final class PackageNameTranslator {
    private PackageNameTranslator() {
    }

    public static String translate(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return packageName;
        }

        String normalized = normalize(packageName);
        String languageCode = LanguageManager.normalizeLanguageCode(LanguageManager.getCurrentLanguage(context));

        switch (normalized) {
            case "double set":
                if ("zh-CN".equals(languageCode)) return "双人套餐";
                if ("zh-TW".equals(languageCode)) return "雙人套餐";
                return packageName;
            case "four person set":
                if ("zh-CN".equals(languageCode)) return "四人套餐";
                if ("zh-TW".equals(languageCode)) return "四人套餐";
                return packageName;
            case "business set":
                if ("zh-CN".equals(languageCode)) return "商务套餐";
                if ("zh-TW".equals(languageCode)) return "商務套餐";
                return packageName;
            default:
                return packageName;
        }
    }

    public static String canonicalize(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return packageName;
        }

        switch (normalize(packageName)) {
            case "双人套餐":
            case "雙人套餐":
                return "double set";
            case "四人套餐":
                return "four person set";
            case "商务套餐":
            case "商務套餐":
                return "business set";
            default:
                return normalize(packageName);
        }
    }

    public static boolean matches(Context context, String left, String right) {
        if (left == null || right == null) {
            return false;
        }

        String leftCanonical = canonicalize(left);
        String rightCanonical = canonicalize(right);
        if (leftCanonical.equalsIgnoreCase(rightCanonical)) {
            return true;
        }

        String leftTranslated = normalize(translate(context, left));
        String rightTranslated = normalize(translate(context, right));
        return leftTranslated.equalsIgnoreCase(rightTranslated);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}