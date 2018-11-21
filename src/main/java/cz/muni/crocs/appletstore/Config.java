package cz.muni.crocs.appletstore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Config {

    public static Translation translation;
    public static HashMap<String, String> options = new HashMap<>();

    public static final String IMAGE_DIR = "src/main/resources/img/";
    public static final String LANG_DIR = "src/main/resources/lang/";

    public static final String APP_DATA_DIR = getSystemEnv();

    public static String getSystemEnv() {
        //from  https://stackoverflow.com/questions/8782797/creating-directory-in-application-support-or-appdata
        String appFolder = System.getenv("APPDATA") + "\\" + "JCAppStore";

        System.out.println("Searching for system");

        String os = System.getProperty("os.name").toUpperCase();
        if (os.contains("WIN")) {
            appFolder = System.getenv("APPDATA") + "\\" + "JCAppStore";
            System.out.println("Found windows");
        }
        if (os.contains("MAC")) {
            appFolder = System.getProperty("user.home") + "/Library/Application " + "Support"
                    + "JCAppStore";
            System.out.println("Found mac");
        }
        if (os.contains("NUX")) {
            appFolder = System.getProperty("user.dir") + ".JCAppStore";
            System.out.println("Found linux");
        }

        File directory = new File(appFolder);

        if (directory.exists()) {
            System.out.println("App data folder present.");
        }

        if (!directory.exists()) {
            directory.mkdir();
            System.out.println("App data folder created.");
        }
        return appFolder;
    }

    private final static String TERMINALS = "terminals";

    public static void getFileOptions() throws IOException {
        if(options.size() != 0) return; //already loaded
        File file = new File(Config.APP_DATA_DIR + "\\jcappstore.options");

        if (!file.createNewFile()) {
            try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = r.readLine()) != null) {
                    String[] content = line.split("=");
                    options.put(content[0], content[1]);
                }
            }
            if (options.size() == 0) {
                new OptionsLoader(file, true).setVisible(true);
            }
        } else {
            new OptionsLoader(file, true).setVisible(true);
        }
        translation = new Translation(options.get("lang"));
    }

}
