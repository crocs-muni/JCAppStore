package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.*;
import java.util.*;

import static cz.muni.crocs.appletstore.Config.S;

/**
 * JCAppStore options implementation
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class OptionsImpl implements Options<String> {

    private static final Logger logger = LoggerFactory.getLogger(OptionsImpl.class);

    private final String HEADER = "JCAppStore";

    private HashMap<String, String> options;
    private StyleSheet sheet;
    private Language language;
    private Font text;
    private Font title;

    /**
     * Creates a new options instance from default options file
     */
    OptionsImpl() {
        this.options = new HashMap<>();
        load();
        setDefaults();
        setup();
    }

    @Override
    public void setDefaults() {
        addIfMissing(Options.KEY_SHOW_WELCOME, "true");
        addIfMissing(Options.KEY_LANGUAGE, Locale.getDefault().toString());
        addIfMissing(Options.KEY_BACKGROUND, Config.IMAGE_DIR + "bg.jpg");
        addIfMissing(Options.KEY_GITHUB_LATEST_VERSION, "none");
        addIfMissing(Options.KEY_HINT, "true");
        addIfMissing(Options.KEY_STYLESHEET, "src"+S+"main"+S+"resources"+S+"css"+S+"default.css");
        addIfMissing(Options.KEY_FONT, "src"+S+"main"+S+"resources"+S+"fonts"+S+"text.ttf");
        addIfMissing(Options.KEY_TITLE_FONT, "src"+S+"main"+S+"resources"+S+"fonts"+S+"title.ttf");
        addIfMissing(Options.KEY_PGP_LOCATION, "");
        addIfMissing(Options.KEY_SIMPLE_USE, "true");
        addIfMissing(Options.KEY_VERBOSE_MODE, "false");
        addIfMissing(Options.KEY_KEEP_JCMEMORY, "true");
        addIfMissing(Options.KEY_EXCLUSIVE_CARD_CONNECT, "false");
        addIfMissing(Options.KEY_WARN_FORCE_INSTALL, "true");
        addIfMissing(Options.KEY_LAST_SELECTION_LOCATION, Config.APP_LOCAL_DIR.getAbsolutePath());
        addIfMissing(Options.KEY_STORE_FINGERPRINT, "AE14854BECCAC4CC0BC695E83D6FE2832EDFE9C9");
        addIfMissing(Options.KEY_JCALGTEST_CLIENT_PATH, Config.RESOURCES_DIR + "host" + Config.S + "JCAlgTest_1.7.9.jar");
        addIfMissing(Options.KEY_JAVA_EXECUTABLE, "java");
    }

    @Override
    public Font getFont() {
        return text;
    }

    @Override
    public Font getTitleFont() {
        return title;
    }

    @Override
    public Font getFont(float size) {
        return getFont().deriveFont(size);
    }

    @Override
    public Font getTitleFont(float size) {
        return getTitleFont().deriveFont(size);
    }

    @Override
    public Font getFont(int style, float size) {
        return getFont().deriveFont(style, size);
    }

    @Override
    public Font getTitleFont(int style, float size) {
        return getTitleFont().deriveFont(style, size);
    }

    @Override
    public StyleSheet getDefaultStyleSheet() {
        return sheet;
    }

    @Override
    public void setLanguageLocale(Locale language) {
        setLanguage(LanguageImpl.from(language));
    }

    @Override
    public Locale getLanguageLocale() {
        return language.get();
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
        addOption(KEY_LANGUAGE, language.getLocaleString());
        ResourceBundle.clearCache();
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public boolean is(String key) {
        return getOption(key).trim().toLowerCase().equals("true");
    }

    @Override
    public String getOption(String name) {
        if (options.containsKey(name))
            return options.get(name);
        return "";
    }

    @Override
    public void addOption(String name, String value) {
        options.put(name, value);
    }

    @Override
    public void load() {
        File file = new File(Config.OPTIONS_FILE);

        try {
            if (!file.createNewFile()) {
                IniParser parser = new IniParserImpl(file, HEADER, "");
                Set<String> keyset = parser.keySet();
                if (keyset == null)
                    return;
                for (String key : keyset) {
                    options.put(key, parser.getValue(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Failed to read app options.", e);
        }
    }

    @Override
    public void save() {
        File file = new File(Config.OPTIONS_FILE);
        try {
            if (!file.exists()) file.createNewFile();
            IniParser parser = new IniParserImpl(file, HEADER, "");
            options.forEach(parser::addValue);
            parser.store();
        } catch (IOException e) {
            logger.error("Failed to save the file options.", e);
            e.printStackTrace();
        }
    }

    private void setup() {
        loadLanguage();
        setStyles();
        loadFonts();
    }

    private void addIfMissing(String key, String value) {
        if (!options.containsKey(key)) {
            options.put(key, value);
        }
    }

    private void loadLanguage() {
        language = LanguageImpl.from(getOption(KEY_LANGUAGE));
    }

    private void setStyles() {
        sheet = new StyleSheet();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(options.get(Options.KEY_STYLESHEET))))) {
            sheet.loadRules(br, null);

        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Could not load css styles", e);
            sheet.addRule("body {\n" +
                    "    font-size: 11px;\n" +
                    "}\n" +
                    ".code {\n" +
                    "    background: #F4F4F4;\n" +
                    "    border-radius: 5px;\n" +
                    "    border-left: 3px solid #f36d33;\n" +
                    "    color: #676767;\n" +
                    "    page-break-inside: avoid;\n" +
                    "    font-family: monospace;\n" +
                    "    font-size: 10px;\n" +
                    "    line-height: 1.6;\n" +
                    "    margin: 15px 5px;\n" +
                    "    max-width: 550px;\n" +
                    "    overflow: auto;\n" +
                    "    padding: 4px;\n" +
                    "    display: block;\n" +
                    "    word-wrap: break-word;\n" +
                    "}");
        }
    }

    private void loadFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        text = getCustomFont(options.get(Options.KEY_FONT));
        ge.registerFont(text);
        title = getCustomFont(options.get(Options.KEY_TITLE_FONT));
        ge.registerFont(title);
    }

    private Font getCustomFont(String path) {
        if (path == null || path.isEmpty()) {
            return getDefaultFont();
        }
        return getCustomFont(new File(path));
    }

    private Font getCustomFont(File fontFile) {
        if (fontFile == null || !fontFile.exists()) return getDefaultFont();
        try {
            return Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(14f);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            logger.warn("Failed to create font from the presetns", e);
            return getDefaultFont();
        }
    }

    private Font getDefaultFont() {
        return new Font("Courier", Font.PLAIN, 14);
    }
}
