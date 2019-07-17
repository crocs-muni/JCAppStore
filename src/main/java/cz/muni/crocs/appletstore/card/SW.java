package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.Sources;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * SW constants from GPPro
 * @author Jiří Horák
 * @version 1.0
 */
public class SW {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public static final short NO_SPECIFIC_DIAGNOSIS = 0x6400;

    public static final short INVALID_INS = 0x6D00;
    public static final short INVALID_CLA = 0x6E00;
    public static final short INVALID_P1P2 = 0x6A86;
    public static final short WRONG_LENGTH_LC = 0x6700;

    public static final short MEMORY_FAIL = 0x6581;
    public static final short MEMORY_FULL = 0x6A84;
    public static final short INCORRECT_DATA = 0x6A80;

    public static final short SECURE_MSG_NOT_SUPPORTED = 0x6882;
    public static final short SECURITY_STATUS_NOT_SATIFIED = 0x6982;
    public static final short CONDITIONS_OF_USE_NOT_SATISFIED = 0x6985;
    public static final short APP_OR_FILE_NOT_FOUND = 0x6A82;
    public static final short REFERENCED_DATA_NOT_FOUND = 0x6A88;

    public static final short CARD_LOCKED = 0x6283;
    //function not supported -> card is locked
    public static final short FNC_NOT_SUPPORTED = 0x6A81;


    public static String getErrorCause(int sw, String msg) {
        switch (sw) {
            case NO_SPECIFIC_DIAGNOSIS: return textSrc.getString("E_generic") + msg + textSrc.getString("search_SW") + sw + "'.";
            case INVALID_INS: return textSrc.getString("E_invalid_INS");
            case INVALID_CLA: return textSrc.getString("E_invalid_CLA");
            case INVALID_P1P2: return textSrc.getString("E_invalid_P1P2");
            case WRONG_LENGTH_LC: return textSrc.getString("E_invalid_LC");
            case MEMORY_FAIL: return textSrc.getString("E_invalid_memory");
            case MEMORY_FULL: return textSrc.getString("E_full_memory");
            case INCORRECT_DATA: return textSrc.getString("E_invalid_data");
            case SECURITY_STATUS_NOT_SATIFIED: return textSrc.getString("E_security_failure");
            case CONDITIONS_OF_USE_NOT_SATISFIED: return textSrc.getString("E_use_failure");
            case APP_OR_FILE_NOT_FOUND: return textSrc.getString("E_pkg_not_found");
            case REFERENCED_DATA_NOT_FOUND: return textSrc.getString("E_data_not_found");
            case CARD_LOCKED: return textSrc.getString("E_action_denied");
            case FNC_NOT_SUPPORTED: return textSrc.getString("E_action_not_supported");
            default: return msg;
        }
    }
}