package com.assistia.service;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContextWrapper;
import android.content.SharedPreferences;

import com.assistia.helper.LanguageHelper;

// TODO: create interface
public class SettingsService {
    private static final String languageSettingKey = "language";
    private static final String speakAsapSettingKey = "speak_asap";

    private final SharedPreferences sharedPreferences;

    public SettingsService(ContextWrapper contextWrapper) {
        this.sharedPreferences = contextWrapper.getSharedPreferences("Settings", MODE_PRIVATE);;
    }

    public int getLanguage() {
        return this.sharedPreferences.getInt(languageSettingKey, LanguageHelper.INDEX_LANGUAGE_ENGLISH);
    }

    public void setLanguage(int languageId) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putInt(languageSettingKey, languageId);
        editor.apply();
    }

    public boolean getSpeakAsap() {
        return this.sharedPreferences.getBoolean(speakAsapSettingKey, false);
    }

    public void setSpeakAsap(boolean speakAsap) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putBoolean(speakAsapSettingKey, speakAsap);
        editor.apply();
    }
}
