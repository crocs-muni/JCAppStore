package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.action.UnsafeCardOperation;

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
    private UnsafeCardOperation performer = null;
    private String performerMsg = null;
    private ErrDisplay displayStyle = ErrDisplay.NO_DISPLAY;

    /**
     * Create an exception
     * @param cause cause, translated string
     */
    public LocalizedException(String cause, ErrDisplay displayStyle) {
        super(cause);
        this.translated = cause;
        isKey = false;
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause exception to encapsulate
     */
    public LocalizedException(Throwable cause, ErrDisplay displayStyle) {
        super(cause);
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause cause, translated string
     * @param ex exception to encapsulate
     */
    public LocalizedException(String cause, Throwable ex, ErrDisplay displayStyle) {
        super(cause, ex);
        this.translated = cause;
        isKey = false;
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     */
    public LocalizedException(String cause, String translated, ErrDisplay displayStyle) {
        super(cause);
        this.translated = translated;
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     */
    public LocalizedException(Throwable cause, String translated, ErrDisplay displayStyle) {
        super(cause);
        this.translated = translated;
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param ex exception to encapsulate
     */
    public LocalizedException(String cause, String translated, Throwable ex, ErrDisplay displayStyle) {
        super(cause, ex);
        this.translated = translated;
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param image image to display along with an error
     */
    public LocalizedException(String cause, String translated, String image, ErrDisplay displayStyle) {
        super(cause);
        this.translated = translated;
        this.image = image;
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param image image to display along with an error
     */
    public LocalizedException(Throwable cause, String translated, String image, ErrDisplay displayStyle) {
        super(cause);
        this.translated = translated;
        this.image = image;
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param image image to display along with an error
     * @param ex exception to encapsulate
     */
    public LocalizedException(String cause, String translated, String image, Throwable ex, ErrDisplay displayStyle) {
        super(cause, ex);
        this.translated = translated;
        this.image = image;
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param ex exception to encapsulate
     * @param op operation callback to perform (to resolve the exception)
     * @param performerTranslatedMsg string message accompanying the operation - translated
     */
    public LocalizedException(String cause, String translated, Throwable ex, String imgName,
                              UnsafeCardOperation op, String performerTranslatedMsg, ErrDisplay displayStyle) {
        super(cause, ex);
        this.translated = translated;
        this.image = imgName;
        this.performer = op;
        this.performerMsg = performerTranslatedMsg;
        this.displayStyle = displayStyle;
    }

    /**
     * Create an exception
     * @param cause cause, english cause description
     * @param translated translated cause description
     * @param op operation callback to perform (to resolve the exception)
     * @param performerTranslatedMsg string message accompanying the operation - translated
     */
    public LocalizedException(String cause, String translated, String imgName,
                              UnsafeCardOperation op, String performerTranslatedMsg, ErrDisplay displayStyle) {
        super(cause);
        this.translated = translated;
        this.image = imgName;
        this.performer = op;
        this.performerMsg = performerTranslatedMsg;
        this.displayStyle = displayStyle;
    }

    public static LocalizedException from(Exception e, ErrDisplay displayStyle) {
        LocalizedException result = new LocalizedException(e.getMessage(), e.getLocalizedMessage(), e.getCause(), displayStyle);
        result.isKey = false;
        return result;
    }

    public static LocalizedException from(LocalizedException e, ErrDisplay displayStyle) {
        LocalizedException result = new LocalizedException(e.getMessage(), e.getLocalizedMessage(),
                e.getImageName(), e.getCause(), displayStyle);
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

    /**
     * Return unsafe operation to perform to handle the issue
     * @return unsafe callback (e.g. can throw many exceptions)
     */
    public UnsafeCardOperation getUnsafeOperation() {
        return performer;
    }

    /**
     * To erase operation - can prevent from recursion.
     */
    public void removeOperation() {
        this.performer = null;
    }

    /**
     * Return associated message with performer (e.g. retry)
     * @return performer message, localized
     */
    public String getOperationLocalizedMsg() {
        return performerMsg;
    }

    /**
     * If exception forwarded to user in GUI, should it be
     * used in (mostly generic cases) as full screen error or only a popup message?
     * @return true if full screen
     */
    public ErrDisplay getDisplayStyle() {
        return displayStyle;
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
