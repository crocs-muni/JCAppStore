package cz.muni.crocs.appletstore.sources;

import javax.swing.text.html.StyleSheet;
import java.awt.*;

public interface Options<ValueType> {

    /**
     * Key names for options
     */
    static final String KEY_LANGUAGE = "lang";
    static final String KEY_GITHUB_LATEST_VERSION = "github.latest.version";
    static final String KEY_BACKGROUND = "background";
    static final String KEY_HINT = "hint";
    static final String KEY_FONT = "font";
    static final String KEY_STYLESHEET = "stylesheet";

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
     * Add or update value at name
     * @param name name of the option
     * @param value value of the option
     */
    void saveOption(String name, ValueType value);

    /**
     * Save the opts into file
     */
    void save();

    /**
     * Resets all options
     */
    void setDefaults();

    /**
     * Returns default font as specified in opts
     * @return default font for app
     */
    Font getDefaultFont();

    /**
     * Returns default styleSheet as specified in opts
     * @return default styleSheet for app
     */
    StyleSheet getDefaultStyleSheet();
}
