package cz.muni.crocs.appletstore.sources;

import cz.muni.crocs.appletstore.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.*;
import java.util.HashMap;

public class OptionImpl implements Options<String> {
    private static final Logger logger = LoggerFactory.getLogger(OptionImpl.class);

    private HashMap<String, String> options;
    private StyleSheet sheet;
    private Font font;

    public OptionImpl() {
        getFileOptions();
        if (options.isEmpty())
            setDefaults();

        setStyles();
        loadFont();
    }

    public OptionImpl(HashMap<String, String> options) {
        this.options = options;
        if (options.isEmpty())
            setDefaults();

        setStyles();
        loadFont();
    }

    @Override
    public void setDefaults() {
        options.clear();
        options.put(Options.KEY_LANGUAGE, "en");
        options.put(Options.KEY_BACKGROUND, "bg.jpg");
        options.put(Options.KEY_GITHUB_LATEST_VERSION, "none");
        options.put(Options.KEY_HINT, "true");
        options.put(Options.KEY_STYLESHEET, "src/main/resources/css/default.css");
        options.put(Options.KEY_FONT, "src/main/resources/fonts/x.ttf");
    }

    @Override
    public Font getDefaultFont() {
        return font;
    }

    @Override
    public StyleSheet getDefaultStyleSheet() {
        return sheet;
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
    public void saveOption(String name, String value) {
        options.put(name, value);
    }

    @Override
    public void save() {
        File file = new File(Config.APP_DATA_DIR + Config.SEP + "jcappstore.options");
        try {
            if (!file.createNewFile()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                    options.forEach((key, value) -> safeWriter(writer, key, value));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getFileOptions(){
        options = new HashMap<>();
        File file = new File(Config.APP_DATA_DIR + Config.SEP + "jcappstore.options");

        try {
            if (!file.createNewFile()) {
                try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        String[] content = line.split("=");
                        options.put(content[0], content[1]);
                    }
                }
                if (options.size() == 0) {
                    setDefaults();
                }
            } else {
                setDefaults();
            }
        } catch (IOException e) {
            setDefaults();
            e.printStackTrace();
            logger.warn("Failed to read app options.");
        }
    }

    private void safeWriter(BufferedWriter writer, String key, String value) {
        try {
            writer.write(key + "=" + value + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Failed to save options.");
        }
    }

    private void setStyles() {
        sheet = new StyleSheet();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(options.get(Options.KEY_STYLESHEET))))) {
            sheet.loadRules(br, null);

        } catch (IOException e) {
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

    public void loadFont() {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(options.get(Options.KEY_FONT)));
        } catch (IOException | FontFormatException e) {
            font = new Font("Courier", Font.PLAIN, 14);
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);
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
