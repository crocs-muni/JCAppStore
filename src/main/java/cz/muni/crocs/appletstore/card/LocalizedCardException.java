package cz.muni.crocs.appletstore.card;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizedCardException extends Exception {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private String translated;

    public LocalizedCardException(String cause) {
        super(cause);
    }

    public LocalizedCardException(Throwable cause) {
        super(cause);
    }

    public LocalizedCardException(String cause, Throwable ex) {
        super(cause, ex);
    }

    public LocalizedCardException(String cause, String translated) {
        super(cause);
        this.translated = translated;
    }

    public LocalizedCardException(Throwable cause, String translated) {
        super(cause);
        this.translated = translated;
    }

    public LocalizedCardException(String cause, String translated, Throwable ex) {
        super(cause, ex);
        this.translated = translated;
    }

    @Override
    public String getLocalizedMessage() {
        if (translated != null)
            return textSrc.getString(translated) + "<br>" + getMessage();
        return getMessage();
    }

    @Override
    public String getMessage() {
        return textSrc.getString("W_no_translation") + super.getMessage();
    }
}
