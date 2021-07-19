package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.action.UnsafeCardOperation;
import cz.muni.crocs.appletstore.util.ErrDisplay;
import cz.muni.crocs.appletstore.util.LocalizedException;
import pro.javacard.gp.GPException;

/**
 * Class to separate different exception types
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalizedCardException extends LocalizedException {

    public LocalizedCardException(String cause, ErrDisplay displayStyle) {
        super(cause, displayStyle);
    }

    public LocalizedCardException(Throwable cause, ErrDisplay displayStyle) {
        super(cause, displayStyle);
    }

    public LocalizedCardException(String cause, Throwable ex, ErrDisplay displayStyle) {
        super(cause, ex, displayStyle);
    }

    public LocalizedCardException(String cause, String translated, ErrDisplay displayStyle) {
        super(cause, translated, displayStyle);
    }

    public LocalizedCardException(Throwable cause, String translated, ErrDisplay displayStyle) {
        super(cause, translated, displayStyle);
    }

    public LocalizedCardException(String cause, String translated, Throwable ex, ErrDisplay displayStyle) {
        super(cause, translated, ex, displayStyle);
    }

    public LocalizedCardException(String cause, String translated, String image, ErrDisplay displayStyle) {
        super(cause, translated, image, displayStyle);
    }

    public LocalizedCardException(Throwable cause, String translated, String image, ErrDisplay displayStyle) {
        super(cause, translated, image, displayStyle);
    }

    public LocalizedCardException(String cause, String translated, String image, Throwable ex, ErrDisplay displayStyle) {
        super(cause, translated, image, ex, displayStyle);
    }

    public LocalizedCardException(String message, String translated, String imgName,
                                  UnsafeCardOperation unsafeOperation, String translatedOperationMsg, ErrDisplay displayStyle) {
        super(message, translated, imgName, unsafeOperation, translatedOperationMsg, displayStyle);
    }

    public LocalizedCardException(String message, String translated, Throwable e, String imgName,
                                  UnsafeCardOperation unsafeOperation, String translatedOperationMsg, ErrDisplay displayStyle) {
        super(message, translated, e, imgName, unsafeOperation, translatedOperationMsg, displayStyle);
    }
}
