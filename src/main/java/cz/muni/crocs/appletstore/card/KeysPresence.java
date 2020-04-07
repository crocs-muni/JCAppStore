package cz.muni.crocs.appletstore.card;

/**
 * Enum used to deduce sensitive data presence
 * used when uninstalling applet - shows warning about data loss
 *
 * @author Jiří Horák
 * @version 1.0
 */
public enum KeysPresence {
    PRESENT, NO_KEYS, UNKNOWN
}
