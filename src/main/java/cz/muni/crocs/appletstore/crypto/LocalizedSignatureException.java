package cz.muni.crocs.appletstore.crypto;

import cz.muni.crocs.appletstore.util.LocalizedException;

/**
 * Just to separate different exception source
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalizedSignatureException extends LocalizedException {

    public LocalizedSignatureException(String cause) {
        super(cause);
    }

    public LocalizedSignatureException(Throwable cause) {
        super(cause);
    }

    public LocalizedSignatureException(String cause, Throwable ex) {
        super(cause, ex);
    }

    public LocalizedSignatureException(String cause, String translated) {
        super(cause, translated);
    }

    public LocalizedSignatureException(Throwable cause, String translated) {
        super(cause, translated);
    }

    public LocalizedSignatureException(String cause, String translated, Throwable ex) {
        super(cause, translated, ex);
    }
}
