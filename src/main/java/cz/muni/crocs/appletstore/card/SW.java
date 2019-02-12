package cz.muni.crocs.appletstore.card;

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
}