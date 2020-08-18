package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.util.LocalizedException;

/**
 * Class to separate different exception types
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalizedCardException extends LocalizedException {

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
        super(cause, translated);
    }

    public LocalizedCardException(Throwable cause, String translated) {
        super(cause, translated);
    }

    public LocalizedCardException(String cause, String translated, Throwable ex) {
        super(cause, translated, ex);
    }

    public LocalizedCardException(String cause, String translated, String image) {
        super(cause, translated, image);
    }

    public LocalizedCardException(Throwable cause, String translated, String image) {
        super(cause, translated, image);
    }

    public LocalizedCardException(String cause, String translated, String image, Throwable ex) {
        super(cause, translated, image, ex);
    }
}
