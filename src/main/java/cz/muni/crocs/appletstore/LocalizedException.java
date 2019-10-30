package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizedException extends Exception {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private String translated;
    private boolean isKey = true;

    public LocalizedException(String cause) {
        super(cause);
        this.translated = cause;
        isKey = false;
    }

    public LocalizedException(Throwable cause) {
        super(cause);
    }

    public LocalizedException(String cause, Throwable ex) {
        super(cause, ex);
    }

    public LocalizedException(String cause, String translated) {
        super(cause);
        this.translated = translated;
    }

    public LocalizedException(Throwable cause, String translated) {
        super(cause);
        this.translated = translated;
    }

    public LocalizedException(String cause, String translated, Throwable ex) {
        super(cause, ex);
        this.translated = translated;
    }

    public void setTranslationKey(String key) {
        this.isKey = true;
        this.translated = key;
    }

    public void setTranslation(String text) {
        this.isKey = false;
        this.translated = text;
    }

    @Override
    public String getLocalizedMessage() {
        boolean verbose = OptionsFactory.getOptions().isVerbose();
        if (!isKey)
            return translated;
        if (translated != null) {
            if (verbose)
                return textSrc.getString(translated) + "<br>" + getMessage();
            return textSrc.getString(translated);
        }
        return (verbose) ? getMessage() : textSrc.getString("E_default_try_again");
    }
}
