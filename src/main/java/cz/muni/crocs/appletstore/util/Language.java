package cz.muni.crocs.appletstore.util;

import java.util.Locale;

public interface Language {

    String getLocaleString();

    Locale get();

    boolean has(String locale);

    boolean has(Language other);
}
