package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Localized exceptions for better error message handling
 * also supports image for optional image inclusion (error pane with images that help understand to cause)
 */
public class LocalizedException extends Exception {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private String translated;
    private String image;
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

    public LocalizedException(String cause, String translated, String image) {
        super(cause);
        this.translated = translated;
        this.image = image;
    }

    public LocalizedException(Throwable cause, String translated, String image) {
        super(cause);
        this.translated = translated;
        this.image = image;
    }

    public LocalizedException(String cause, String translated, String image, Throwable ex) {
        super(cause, ex);
        this.translated = translated;
        this.image = image;
    }

    public void setTranslationKey(String key) {
        this.isKey = true;
        this.translated = key;
    }

    public void setTranslation(String text) {
        this.isKey = false;
        this.translated = text;
    }

    public void setImageName(String image) {
        this.image = image;
    }

    public String getImageName() {
        return image == null || image.isEmpty() ? "announcement_white.png" : image;
    }

    @Override
    public String getLocalizedMessage() {
        boolean verbose = OptionsFactory.getOptions().is(Options.KEY_VERBOSE_MODE);
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
