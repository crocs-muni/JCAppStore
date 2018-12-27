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
    //get translation
    public static Translation translation;
    //options map and key values
    public static HashMap<String, String> options = new HashMap<>();
    public static final String OPT_KEY_LANGUAGE = "lang";
    public static final String OPT_KEY_GITHUB_LATEST_VERSION = "github.latest.version";
    public static final String OPT_KEY_BACKGROUND = "background";
    //system path separator
    public static final String SEP = File.separator;

    //app external data cache folders
    public static final String APP_ROOT_DIR = getSystemEnvAndSetup();
    public static final File APP_DATA_DIR = checkFolders(APP_ROOT_DIR + SEP + "data");
    public static final File APP_STORE_DIR = checkFolders(APP_ROOT_DIR + SEP + "store");
    public static final File APP_LOCAL_DIR = checkFolders(APP_ROOT_DIR + SEP + "my_applets");

    //app internal dirs
    public static final String IMAGE_DIR = "src"+SEP+"main"+SEP+"resources"+SEP+"img"+SEP;
    public static final String LANG_DIR = "src"+SEP+"main"+SEP+"resources"+SEP+"lang"+SEP;

    //store hierarchy
    public static final String REMOTE_STORE_URL = "https://github.com/petrs/JCAppStoreContent.git";
    public static final String REMOTE_STORE_LATEST_URL = "https://api.github.com/repos/petrs/JCAppStoreContent/releases/latest";
    public static final String FILE_INFO_PREFIX = "info_";
    public static final String FILE_INFO_SUFFIX = ".json";
    public static final String JSON_TAG_NAME = "name";
    public static final String JSON_TAG_TITLE = "title";
    public static final String JSON_TAG_ICON = "icon";
    public static final String JSON_TAG_VERSION = "version";
    public static final String JSON_TAG_AUTHOR = "author";
    public static final String JSON_TAG_INFO = "info";
    public static final String JSON_TAG_DESC = "description";
    public static final String JSON_TAG_URL = "url";
    public static final String JSON_TAG_USAGE = "usage";
    public static final String RESOURCES = Config.APP_STORE_DIR + Config.SEP + "Resources" + Config.SEP;

    private final static String TERMINALS = "terminals";

    public static String getSystemEnvAndSetup() {
        //from  https://stackoverflow.com/questions/8782797/creating-directory-in-application-support-or-appdata
        String appFolder = FileSystemView.getFileSystemView().getDefaultDirectory() + SEP + "JCAppStore";
        //todo test various systems
        //todo solve "run as admin neccessary" .. ? depends on where is our base folder created
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
        if (!directory.exists() && (!directory.mkdirs())) {
            throw new RuntimeException("The Application doesn't have the rights to store data into default folder.");
        }
        return appFolder;
    }

    private static File checkFolders(String folder) {
        File f = new File(folder);
        if (!f.exists()) {
            if (!f.mkdir()) {
                return null;
            }
        }
        if (!f.isDirectory()) return null;
        return f;
    }


    public static void getFileOptions() throws IOException {
        if(options.size() != 0) return; //already loaded
        File file = new File(Config.APP_DATA_DIR + SEP + "jcappstore.options");

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
        File file = new File(Config.APP_DATA_DIR + SEP + "jcappstore.options");

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
