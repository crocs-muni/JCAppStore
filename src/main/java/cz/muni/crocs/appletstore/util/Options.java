package cz.muni.crocs.appletstore.util;

import javax.swing.text.html.StyleSheet;
import java.awt.*;

public interface Options<ValueType> {

    /**
     * Key names for options
     */
    String KEY_LANGUAGE = "lang";
    String KEY_GITHUB_LATEST_VERSION = "github.latest.version";
    String KEY_BACKGROUND = "background";
    String KEY_HINT = "hint";
    String KEY_FONT = "font";
    String KEY_STYLESHEET = "stylesheet";

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
