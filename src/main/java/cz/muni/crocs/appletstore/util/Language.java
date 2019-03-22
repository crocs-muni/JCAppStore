package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Language {

    private ResourceBundle source;

    public Language(String locale) {
        this.source = ResourceBundle.getBundle("Lang", new Locale(locale));
    }

    public String get(String key) {
        return source.getString(key);
    }

}
