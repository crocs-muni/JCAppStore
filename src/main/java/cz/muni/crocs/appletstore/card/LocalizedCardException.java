package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.LocalizedException;

/**
 * Just to separate different exception source
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
}
