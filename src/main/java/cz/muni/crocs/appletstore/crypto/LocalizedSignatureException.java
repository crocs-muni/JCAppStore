package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.ErrDisplay;
import cz.muni.crocs.appletstore.util.LocalizedException;

/**
 * Just to separate different exception source
 * signature errors are included in GUI message windows so the display is implicitly handled
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalizedSignatureException extends LocalizedException {

    public LocalizedSignatureException(String cause) {
        super(cause, ErrDisplay.NO_DISPLAY);
    }

    public LocalizedSignatureException(Throwable cause) {
        super(cause, ErrDisplay.NO_DISPLAY);
    }

    public LocalizedSignatureException(String cause, Throwable ex) {
        super(cause, ex, ErrDisplay.NO_DISPLAY);
    }

    public LocalizedSignatureException(String cause, String translated) {
        super(cause, translated, ErrDisplay.NO_DISPLAY);
    }

    public LocalizedSignatureException(Throwable cause, String translated) {
        super(cause, translated, ErrDisplay.NO_DISPLAY);
    }

    public LocalizedSignatureException(String cause, String translated, Throwable ex) {
        super(cause, translated, ex, ErrDisplay.NO_DISPLAY);
    }
}
