package cz.muni.crocs.appletstore.util;

import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Options of the application
 * @param <ValueType> type of the option values stored inside.
 */
public interface Options<ValueType> {

    /**
     * Key names for options
     */
    String KEY_SHOW_WELCOME = "welcome_screen";
    String KEY_LANGUAGE = "lang";
    String KEY_GITHUB_LATEST_VERSION = "github.latest.version";
    String KEY_BACKGROUND = "background";
    String KEY_HINT = "hint";
    String KEY_STYLESHEET = "stylesheet";
    String KEY_EXCLUSIVE_CARD_CONNECT = "exclusive_card";
    String KEY_FONT = "text";
    String KEY_TITLE_FONT = "title";
    String KEY_PGP_LOCATION = "gpg";
    String KEY_VERBOSE_MODE = "verbose";
    String KEY_SIMPLE_USE = "simple_usage";
    String KEY_KEEP_AUTO_INSTALLED = "jcmemory_keep";
    String KEY_WARN_FORCE_INSTALL = "warn_force_install";
    String KEY_LAST_SELECTION_LOCATION = "custom_applet_folder";
    String KEY_STORE_FINGERPRINT = "public_key_fingerprint";
    String KEY_JCALGTEST_CLIENT_PATH = "jcalgtest_client";
    String KEY_JAVA_EXECUTABLE = "java_executable";

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
     * Loads the options
     */
    void load();

    /**
     * Save the opts into file
     */
    void save();

    /**
     * Adds all missing options with default values
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

    /**
     * Set language as default
     */
    void setLanguageLocale(Locale language);

    /**
     * Get default language
     * @return default language
     */
    Locale getLanguageLocale();

    /**
     * Set language as default
     */
    void setLanguage(Language language);

    /**
     * Get default language
     * @return default language
     */
    Language getLanguage();

    /**
     * Check for boolean condition
     * @return true if key evaluated to true
     */
    boolean is(String key);
}
