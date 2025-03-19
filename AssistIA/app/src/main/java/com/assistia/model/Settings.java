package com.assistia.model;

import com.assistia.R;
import com.assistia.helper.LanguageHelper;

import java.util.Map;

public class Settings {
    public static final Map<Integer, LanguageInfo> Languages = Map.of(
        0, new LanguageInfo(R.drawable.flag_usa, LanguageHelper.PREFIX_LANGUAGE_ENGLISH, "English", "US"),
        1, new LanguageInfo(R.drawable.flag_argentina, LanguageHelper.PREFIX_LANGUAGE_SPANISH, "Spanish", "AR")
    );

    public static LanguageInfo LanguageInfo;
    public static boolean SpeakAsap;
}
