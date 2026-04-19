package com.example.yummyrestaurant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public final class LanguageManager {

    public static final String PREFS_NAME = "AppSettingsPrefs";
    public static final String KEY_LANGUAGE = "app_language";

    private static final String LANG_EN = "en";
    private static final String LANG_ZH_CN = "zh-CN";
    private static final String LANG_ZH_TW = "zh-TW";

    private LanguageManager() {
    }

    public static String getCurrentLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_EN);
    }

    public static void setCurrentLanguage(Context context, String languageCode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, normalizeLanguageCode(languageCode))
                .apply();
    }

    public static Context wrapContext(Context context) {
        String code = getCurrentLanguage(context);
        Locale locale = toLocale(code);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        }

        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        return context;
    }

    public static Locale toLocale(String languageCode) {
        String normalized = normalizeLanguageCode(languageCode);
        switch (normalized) {
            case LANG_ZH_CN:
                return Locale.forLanguageTag("zh-CN");
            case LANG_ZH_TW:
                return Locale.forLanguageTag("zh-TW");
            case LANG_EN:
            default:
                return Locale.ENGLISH;
        }
    }

    public static String normalizeLanguageCode(String code) {
        if (code == null) return LANG_EN;
        String trimmed = code.trim();
        if (trimmed.equalsIgnoreCase("zh") || trimmed.equalsIgnoreCase("zh-CN")) {
            return LANG_ZH_CN;
        }
        if (trimmed.equalsIgnoreCase("zh-TW") || trimmed.equalsIgnoreCase("zh-HK")) {
            return LANG_ZH_TW;
        }
        return LANG_EN;
    }

    public static int getLanguageSelectionIndex(String languageCode) {
        String code = normalizeLanguageCode(languageCode);
        if (LANG_ZH_CN.equals(code)) return 1;
        if (LANG_ZH_TW.equals(code)) return 2;
        return 0;
    }

    public static String getLanguageCodeByIndex(int index) {
        switch (index) {
            case 1:
                return LANG_ZH_CN;
            case 2:
                return LANG_ZH_TW;
            case 0:
            default:
                return LANG_EN;
        }
    }
}
