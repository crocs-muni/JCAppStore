package cz.muni.crocs.appletstore.util;

import java.util.ResourceBundle;

/**
 * Localized exceptions for better error message handling
 * also supports image for optional image inclusion (error pane with images that help understand to cause)
 */
public class LocalizedException extends Exception {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private String translated;
    private String image;
    private boolean isKey = true;

    /**
     * Create an exception
     * @param cause cause, translated string
     */
    public LocalizedException(String cause) {
        super(cause);
        this.translated = cause;
        isKey = false;
    }

    /**
     * Create an exception
     * @param cause exception to encapsulate
     */
    public LocalizedException(Throwable cause) {
        super(cause);
    }

    /**
     * Create an exception
     * @param cause cause, translated string
     * @param ex exception to encapsulate
     */
    public LocalizedException(String cause, Throwable ex) {
        super(cause, ex);
        this.translated = cause;
        isKey = false;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     */
    public LocalizedException(String cause, String translated) {
        super(cause);
        this.translated = translated;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     */
    public LocalizedException(Throwable cause, String translated) {
        super(cause);
        this.translated = translated;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param ex exception to encapsulate
     */
    public LocalizedException(String cause, String translated, Throwable ex) {
        super(cause, ex);
        this.translated = translated;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param image image to display along with an error
     */
    public LocalizedException(String cause, String translated, String image) {
        super(cause);
        this.translated = translated;
        this.image = image;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param image image to display along with an error
     */
    public LocalizedException(Throwable cause, String translated, String image) {
        super(cause);
        this.translated = translated;
        this.image = image;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param image image to display along with an error
     * @param ex exception to encapsulate
     */
    public LocalizedException(String cause, String translated, String image, Throwable ex) {
        super(cause, ex);
        this.translated = translated;
        this.image = image;
    }

    public static LocalizedException from(Exception e) {
        LocalizedException result = new LocalizedException(e.getMessage(), e.getLocalizedMessage(), e.getCause());
        result.isKey = false;
        return result;
    }

    public static LocalizedException from(LocalizedException e) {
        LocalizedException result = new LocalizedException(e.getMessage(), e.getLocalizedMessage(),
                e.getImageName(), e.getCause());
        result.isKey = false;
        return result;
    }

    /**
     * Set translation
     * @param key key for ResourceBundle
     */
    public void setTranslationKey(String key) {
        this.isKey = true;
        this.translated = key;
    }

    /**
     * Set translation
     * @param text localized string
     */
    public void setTranslation(String text) {
        this.isKey = false;
        this.translated = text;
    }

    /**
     * Set image name to display, relative to Config.IMAGE_DIR
     * @param image image name
     */
    public void setImageName(String image) {
        this.image = image;
    }

    /**
     * Get iamge name
     * @return image name to display or default image if missing
     */
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
