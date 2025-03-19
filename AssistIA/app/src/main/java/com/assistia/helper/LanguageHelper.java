package com.assistia.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LanguageHelper {

    public static final int INDEX_LANGUAGE_ENGLISH = 1;
    public static final int INDEX_LANGUAGE_SPANISH = 2;// TODO: move somewhere else
    public static final String PREFIX_LANGUAGE_ENGLISH = "en";
    public static final String PREFIX_LANGUAGE_SPANISH = "es";
    public static final String COUNTRY_ENGLISH = "US";
    public static final String COUNTRY_SPANISH = "AR";

    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
