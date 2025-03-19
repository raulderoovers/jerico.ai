package com.assistia.model;

public class LanguageInfo {

    private final int flagResource;
    // TODO: rename to something more meaningful
    private final String language;
    private final String country;
    private final String languageName;

    public int getFlagResource() {
        return this.flagResource;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getLanguageName() {
        return this.languageName;
    }

    public String getCountry() {
        return this.country;
    }

    public LanguageInfo(int flagResource, String language, String languageName, String country) {
        this.flagResource = flagResource;
        this.language = language;
        this.languageName = languageName;
        this.country = country;
    }
}
