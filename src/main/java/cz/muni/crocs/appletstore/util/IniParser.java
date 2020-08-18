package cz.muni.crocs.appletstore.util;
import java.io.IOException;
import java.util.Set;

/**
 * Parser class that focuses only on one header inside INI file:
 * simplified, as we are concerted about one card only at time.
 * The header is implicitly used, given in constructor.
 *
 * @author Jiří Horák
 * @version 1.0
 */
public interface IniParser {

    String TAG_NAME = "name";
    String TAG_KEY = "key";
    String TAG_KEY_CHECK_VALUE = "kcv";
    String TAG_DIVERSIFIER = "diversifier";
    String TAG_AUTHENTICATED = "auth";
    String TAG_ATR = "atr";
    String TAG_CIN = "cin";
    String TAG_IIN = "iin";
    String TAG_CPLC = "cplc";
    String TAG_DATA = "card_data";
    String TAG_CAPABILITIES = "card_capabilities";
    String TAG_KEY_INFO = "key_info";
    String TAG_JCALGTEST_UPDATED = "jcalgtest_updated";

    /**
     * Get value of INI under header specified in constructor
     * @param key key to get
     * @return value assigned to the key given
     */
    String getValue(String key);

    /**
     * Add value of INI under header specified in constructor
     * @param key key to modify
     * @param value value to insert
     * @return this instance for builder pattern
     */
    IniParser addValue(String key, String value);

    /**
     * Add value of INI under header specified in constructor
     * @param key key to modify
     * @param value value to insert
     * @return this instance for builder pattern
     */
    IniParser addValue(String key, byte[] value);

    /**
     * Save all changes. Without calling this method, all changes
     * made using methods above are discarded.
     */
    void store() throws IOException;

    /**
     * Check header presence
     * @return true if header, given in constructor, present
     */
    boolean isHeaderPresent();

    /**
     * Get all keys from header given
     * @return set of keys
     */
    Set<String> keySet();

    /**
     * Change header
     * @param newHeader header to use
     * @return this instance for builder pattern
     */
    IniParser header(String newHeader);
}
