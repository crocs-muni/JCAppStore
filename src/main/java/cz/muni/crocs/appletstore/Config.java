package cz.muni.crocs.appletstore;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Config {

    public static Translation translation;
    public static HashMap<String, String> options = new HashMap<>();
    public static final String OPT_KEY_LANGUAGE = "lang";
    public static final String OPT_KEY_GITHUB_LATEST_VERSION = "github.latest.version";
    public static final String OPT_KEY_BACKGROUND = "background";


    public static final String IMAGE_DIR = "src/main/resources/img/";
    public static final String LANG_DIR = "src/main/resources/lang/";

    //TODO: update urls
    public static final String REMOTE_DIR_INFO = "https://www.github.com/";
    public static final String FILE_LIST_SOURCE = "file_list.xml";
    public static final String CONTAINER_FILE_LIST_TAG = "files";
    public static final String REMOTE_DIR_CONTENT = "https://www.github.com/";

    public static final String APP_ROOT_DIR = getSystemEnv();
    public static final String APP_DATA_DIR = APP_ROOT_DIR + "/data";
    public static final String APP_STORE_DIR = APP_ROOT_DIR + "/store";
    public static final String APP_LOCAL_DIR = APP_ROOT_DIR + "/my_files";

    private final static String TERMINALS = "terminals";

    public static String getSystemEnv() {
        //from  https://stackoverflow.com/questions/8782797/creating-directory-in-application-support-or-appdata

        String appFolder = FileSystemView.getFileSystemView().getDefaultDirectory() + "/" + "JCAppStore";

        //todo test various systems
//        System.out.println("Searching for system");
//
//        String os = System.getProperty("os.name").toUpperCase();
//        if (os.contains("WIN")) {
//            appFolder = System.getenv("CSIDL_PROFILE") + "/" + "JCAppStore";
//            System.out.println("Found windows");
//        }
//        if (os.contains("MAC")) {
//            appFolder = System.getProperty("user.home") + "/Library/Application " + "Support"
//                    + "JCAppStore";
//            System.out.println("Found mac");
//        }
//        if (os.contains("NUX")) {
//            appFolder = System.getProperty("user.dir") + ".JCAppStore";
//            System.out.println("Found linux");
//        }

        File directory = new File(appFolder);

        if (directory.exists()) {
            System.out.println("App data folder present.");
        }

        if (!directory.exists()) {
            directory.mkdir();
            System.out.println("App data folder created: " + appFolder);
        }

        makeFolders(appFolder + "/data");
        makeFolders(appFolder + "/store");
        makeFolders(appFolder + "/my_files");
        return appFolder;
    }

    private static void makeFolders(String folder) {
        File f = new File(folder);
        if (!f.exists()) {
            f.mkdir();
        }
    }


    public static void getFileOptions() throws IOException {
        if(options.size() != 0) return; //already loaded
        File file = new File(Config.APP_DATA_DIR + "/jcappstore.options");

        if (!file.createNewFile()) {
            try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = r.readLine()) != null) {
                    String[] content = line.split("=");
                    options.put(content[0], content[1]);
                }
            }
            if (options.size() == 0) {
                new OptionsLoader(file);
            }
        } else {
            new OptionsLoader(file);
        }
        translation = new Translation(options.get("lang"));
    }

    private static void safeWriter(BufferedWriter writer, String key, String value) {
        try {
            writer.write(key + "=" + value + "\n");
        } catch (IOException e) {
            //TODO private boolean to set if failed writing to notice an posible notify user?
            e.printStackTrace();
        }
    }

    public static void saveOptions() throws IOException {
        File file = new File(Config.APP_DATA_DIR + "/jcappstore.options");

        if (!file.createNewFile()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                options.forEach((key, value) -> safeWriter(writer, key, value));
            }
            if (options.size() == 0) {
                new OptionsLoader(file);
            }
        }
    }
}
