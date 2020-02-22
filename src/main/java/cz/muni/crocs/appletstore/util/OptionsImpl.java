package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.*;
import java.util.*;

import static cz.muni.crocs.appletstore.Config.S;

public class OptionsImpl implements Options<String> {
    private static final Logger logger = LoggerFactory.getLogger(OptionsImpl.class);

    private final String HEADER = "JCAppStore";

    private HashMap<String, String> options;
    private StyleSheet sheet;
    private Language language;
    private Font text;
    private Font title;

    OptionsImpl() {
        getFileOptions();
        if (options.isEmpty())
            setDefaults();
        setup();
    }

    OptionsImpl(HashMap<String, String> options) {
        this.options = options;
        if (options.isEmpty())
            setDefaults();
        setup();
    }

    private void setup() {
        loadLanguage();
        setStyles();
        loadFonts();
    }

    @Override
    public void setDefaults() {
        options.clear();
        options.put(Options.KEY_SHOW_WELCOME, "true");
        options.put(Options.KEY_LANGUAGE, Locale.getDefault().toString());
        options.put(Options.KEY_BACKGROUND, Config.IMAGE_DIR + "bg.jpg");
        options.put(Options.KEY_GITHUB_LATEST_VERSION, "none");
        options.put(Options.KEY_HINT, "true");
        options.put(Options.KEY_STYLESHEET, "src"+S+"main"+S+"resources"+S+"css"+S+"default.css");
        options.put(Options.KEY_FONT, "src"+S+"main"+S+"resources"+S+"fonts"+S+"text.ttf");
        options.put(Options.KEY_TITLE_FONT, "src"+S+"main"+S+"resources"+S+"fonts"+S+"title.ttf");
        options.put(Options.KEY_PGP_LOCATION, "");
        options.put(Options.KEY_SIMPLE_USE, "true");
        options.put(Options.KEY_VERBOSE_MODE, "false");
        options.put(Options.KEY_KEEP_JCMEMORY, "true");
        options.put(Options.KEY_DELETE_IMPLICIT, "true");
        options.put(Options.KEY_EXCLUSIVE_CARD_CONNECT, "false");
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
        addOption(KEY_LANGUAGE, this.language.getLocaleString());
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
        if (!name.equals(KEY_LANGUAGE)) { //do not allow raw change language
            options.put(name, value);
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

    private void loadLanguage() {
        language = LanguageImpl.from(getOption(KEY_LANGUAGE));
    }

    private void getFileOptions() {
        options = new HashMap<>();
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
                if (options.size() == 0) {
                    setDefaults();
                }
            } else {
                setDefaults();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Failed to read app options.", e);
            setDefaults();
        }
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


    //handles the custom opt settings, leave for later
//    //available languages
//    private final String[] langs = new String[]{
//            "English", "eng",
//            "Česky", "cz"
//    };
//    OptionsLoader(File file, boolean startAppOnSelect) {
//
//        Config.options.clear();
//        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        //center this dialog
//        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
//        this.setSize(50, 30 * (langs.length / 2) + 40 /*bar height*/);
//
//
//        JPanel panel = (JPanel) this.getContentPane();
//        CustomJListFactory list = new CustomJListFactory();
//        list.setCellSize(50, 20);
//        for (int i = 0; i < langs.length; i += 2) {
//            list.add(langs[i], Config.IMAGE_DIR + langs[i+1] + ".jpg");
//        }
//        JList jList = list.build();
//        jList.setBorder(new EmptyBorder(10,10, 10, 10));
//
//        //on click save choosed language and run
//        jList.addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent e) {
//                int valueIndex = e.getLastIndex();
//                BufferedWriter writer = null;
//                try {
//                    writer = new BufferedWriter(new FileWriter(file));
//                    writer.write("lang=" + langs[(2*valueIndex+1)] + "\n");
//                    writer.close();
//                    populateOptions(langs[(2*valueIndex+1)]);
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                    populateOptions("eng");
//                }
//                //context.dispose();
//                //problem cannot get language while fetching options
//                JOptionPane.showMessageDialog(null, "Changes will apply after an restart.");
//
//            }
//        });
//        panel.add(jList);
//    }
//private void populateOptions(String key, String value) {
//    Config.options.put(key, value);
//}
}
