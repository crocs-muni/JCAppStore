package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.Config;

/**
 * SW constants from GPPro
 * @author Jiří Horák
 * @version 1.0
 */
public class SW {
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
            case NO_SPECIFIC_DIAGNOSIS: return Config.translation.get(183) + msg + Config.translation.get(11) + sw + "'.";
            case INVALID_INS: return Config.translation.get(154);
            case INVALID_CLA: return Config.translation.get(155);
            case INVALID_P1P2: return Config.translation.get(156);
            case WRONG_LENGTH_LC: return Config.translation.get(157);
            case MEMORY_FAIL: return Config.translation.get(158);
            case MEMORY_FULL: return Config.translation.get(159);
            case INCORRECT_DATA: return Config.translation.get(160);
            case SECURITY_STATUS_NOT_SATIFIED: return Config.translation.get(161);
            case CONDITIONS_OF_USE_NOT_SATISFIED: return Config.translation.get(162);
            case APP_OR_FILE_NOT_FOUND: return Config.translation.get(163);
            case REFERENCED_DATA_NOT_FOUND: return Config.translation.get(164);
            case CARD_LOCKED: return Config.translation.get(165);
            case FNC_NOT_SUPPORTED: return Config.translation.get(166);
            default: return msg;
        }
    }
}