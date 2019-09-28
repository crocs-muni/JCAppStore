package cz.muni.crocs.appletstore.util;

import javax.swing.text.html.StyleSheet;
import java.awt.*;

/**
 * Options of the application
 * @param <ValueType> type of the option values stored inside.
 */
public interface Options<ValueType> {

    /**
     * Key names for options
     */
    String KEY_LANGUAGE = "lang";
    String KEY_GITHUB_LATEST_VERSION = "github.latest.version";
    String KEY_BACKGROUND = "background";
    String KEY_HINT = "hint";
    String KEY_STYLESHEET = "stylesheet";
    String KEY_FONT = "text";
    String KEY_TITLE_FONT = "title";
    String KEY_KEYBASE_LOCATION = "";

    /**
     * Get option for app
     * @param name name of the option - enum specified in this interface
     * @return value associated with the name
     */
    ValueType getOption(String name);

    /**
     * Add option to app opts
     * @param name name of the option
     * @param value value to add/update
     */
    void addOption(String name, ValueType value);

    /**
     * Save the opts into file
     */
    void save();

    /**
     * Resets all options
     */
    void setDefaults();

    /**
     * Returns font as specified in opts
     * @return font for app
     */
    Font getFont();

    /**
     * Returns font as specified in opts
     * @return font title for app
     */
    Font getTitleFont();

    /**
     * Returns font as specified in opts
     * @param size font size
     * @return font for app
     */
    Font getFont(float size);

    /**
     * Returns font as specified in opts
     * @param size font size
     * @return font title for app
     */
    Font getTitleFont(float size);

    /**
     * Returns font as specified in opts
     * @param size font size
     * @param style font style, one of Font.BOLD, Font.ITALIC...
     * @return font for app
     */
    Font getFont(int style, float size);

    /**
     * Returns font as specified in opts
     * @param size font size
     * @param style font style, one of Font.BOLD, Font.ITALIC...
     * @return font title for app
     */
    Font getTitleFont(int style, float size);

    /**
     * Returns default styleSheet as specified in opts
     * @return default styleSheet for app
     */
    StyleSheet getDefaultStyleSheet();
}
