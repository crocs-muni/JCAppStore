package cz.muni.crocs.appletstore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class OptionsManager {

    private HashMap<String, String> options;

    public OptionsManager(HashMap<String, String> options) {
        this.options = options;
        if (options.isEmpty())
            setDefaults();
    }

    public void setDefaults() {
        options.clear();
        options.put(Config.OPT_KEY_LANGUAGE, "en");
        options.put(Config.OPT_KEY_BACKGROUND, "bg.jpg");
        options.put(Config.OPT_KEY_GITHUB_LATEST_VERSION, "none");
        options.put(Config.OPT_KEY_HINT, "true");
    }

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

    public static HashMap<String, String> fillDefault(HashMap<String, String> options) {
        OptionsManager mngr = new OptionsManager(options);
        mngr.setDefaults();
        mngr.save();
        return options;
    }

    public static HashMap<String, String> getFileOptions(){
        HashMap<String, String> options = new HashMap<>();
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
                    return fillDefault(options);
                }
            } else {
                return fillDefault(options);
            }
        } catch (IOException e) {
            return fillDefault(options);
        }
        return options;
    }

    private static void safeWriter(BufferedWriter writer, String key, String value) {
        try {
            writer.write(key + "=" + value + "\n");
        } catch (IOException e) {
            //TODO private boolean to set if failed writing to notice an posible notify user?
            e.printStackTrace();
        }
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