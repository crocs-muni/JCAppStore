package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.*;
import java.util.HashMap;

import static cz.muni.crocs.appletstore.Config.S;

public class OptionsImpl implements Options<String> {
    private static final Logger logger = LoggerFactory.getLogger(OptionsImpl.class);

    private HashMap<String, String> options;
    private StyleSheet sheet;
    private Font text;
    private Font title;
    private final String HEADER = "JCAppStore";

    public OptionsImpl() {
        getFileOptions();
        if (options.isEmpty())
            setDefaults();

        setStyles();
        loadFonts();
    }

    OptionsImpl(HashMap<String, String> options) {
        this.options = options;
        if (options.isEmpty())
            setDefaults();

        setStyles();
        loadFonts();
    }

    @Override
    public void setDefaults() {
        //todo use delimiter of system
        options.clear();
        options.put(Options.KEY_LANGUAGE, "en"); // en cs todo really, not internacionalization?
        options.put(Options.KEY_BACKGROUND, "bg.jpg");
        options.put(Options.KEY_GITHUB_LATEST_VERSION, "none");
        options.put(Options.KEY_HINT, "true");  //true false
        options.put(Options.KEY_STYLESHEET, "src"+S+"main"+S+"resources"+S+"css"+S+"default.css");
        options.put(Options.KEY_FONT, null);
        options.put(Options.KEY_TITLE_FONT, "src"+S+"main"+S+"resources"+S+"fonts"+S+"title.ttf");
        options.put(Options.KEY_KEYBASE_LOCATION, "");
        options.put(Options.KEY_ERROR_MODE, "default"); // default / verbose
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
    public boolean isVerbose() {
        return getOption(Options.KEY_ERROR_MODE).trim().toLowerCase().equals("verbose");
    }

    @Override
    public String getOption(String name) {
        return options.get(name);
    }

    @Override
    public void addOption(String name, String value) {
        options.put(name, value);
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
            e.printStackTrace();
        }
    }

    private void getFileOptions() {
        options = new HashMap<>();
        File file = new File(Config.OPTIONS_FILE);

        try {
            if (!file.createNewFile()) {
                IniParser parser = new IniParserImpl(file, HEADER, "");
                for (String key : parser.keySet()) {
                    options.put(key, parser.getValue(key));
                }
                if (options.size() == 0) {
                    setDefaults();
                }
            } else {
                setDefaults();
            }
        } catch (IOException e) {
            e.printStackTrace();
            setDefaults();
            logger.warn("Failed to read app options.");
        }
    }

    private void setStyles() {
        sheet = new StyleSheet();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(options.get(Options.KEY_STYLESHEET))))) {
            sheet.loadRules(br, null);

        } catch (IOException e) {
            e.printStackTrace();
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
            return Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
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
//                //todo problem cannot get language while fetching options
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
