package cz.muni.crocs.appletstore.util;

import java.util.Arrays;
import java.util.Locale;

public enum LanguageImpl implements Language {

    ENGLISH("en", "English"),
    CZECH("cs", "Česky");

    private LanguageImpl(String locale, String name) {
        this.locale = locale;
        this.name = name;
    }

    public static Language DEFAULT = ENGLISH;
    private String locale;
    private String name;

    @Override
    public String getLocaleString() {
        return locale;
    }

    @Override
    public Locale get() {
        return Locale.forLanguageTag(locale);
    }

    public static Language from(Locale locale) {
        return from(locale.toString());
    }

    public static Language from(String locale) {
        if (locale == null || locale.length() < 2) return DEFAULT;
        else if (locale.length() > 2) locale = locale.substring(0, 2);
        for (LanguageImpl l : LanguageImpl.values()) {
            if (l.locale.equals(locale)) {
                return l;
            }
        }
        return DEFAULT;
    }

    @Override
    public boolean has(String locale) {
        return has(from(locale));
    }

    @Override
    public boolean has(Language other) {
        return (Arrays.asList(LanguageImpl.values()).contains(other));
    }

    @Override
    public String toString() {
        return name;
    }
}
