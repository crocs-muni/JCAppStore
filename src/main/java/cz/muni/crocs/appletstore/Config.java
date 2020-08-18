package cz.muni.crocs.appletstore;

import org.apache.logging.log4j.core.lookup.MainMapLookup;

import java.io.File;

/**
 * Application global configurations
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Config {
    public static final String VERSION = "1.2";

    //system path separator
    public static final String S = File.separator;

    //app external folders
    public static final String APP_ROOT_DIR = getDefaultAppRootFolder();
    public static final File APP_DATA_DIR = checkFolders(APP_ROOT_DIR + S + "data");
    public static final File APP_STORE_DIR = checkFolders(APP_ROOT_DIR + S + "store");
    public static final File APP_LOCAL_DIR = checkFolders(APP_ROOT_DIR + S + "my_applets");
    public static final File APP_LOG_DIR = checkFolders(APP_DATA_DIR + S + "logs");
    public static final File APP_TEST_DIR = checkFolders(APP_DATA_DIR + S + "results");

    public static final String LOG_FILENAME = "jcAppStore";
    public static final String LOG_FILENAME_EXT = ".log";

    public static final File APP_STORE_CAPS_DIR = new File(APP_ROOT_DIR + S + "store" + S + "JCApplets");

    //app internal dirs
    public static final String RESOURCES_DIR = "src"+S+"main"+S+"resources"+S;
    public static final String IMAGE_DIR = RESOURCES_DIR +"img"+S;
    public static final String DATA_DIR = RESOURCES_DIR +"data"+S;

    //config files related constants
    public static final String CARD_LIST_FILE = APP_DATA_DIR + S + "cards.ini";
    public static final String CARD_TYPES_FILE = "src"+S+"main"+S+"resources"+S+"data"+S+"types.ini";
    public static final String OPTIONS_FILE = APP_DATA_DIR + S + "jcappstore.options";

    //store constants - URLs to store repo & jcalgtest
    public static final String JCALGTEST_RESULTS_DIR = "https://api.github.com/repos/crocs-muni/JCAlgTest/contents/Profiles/results";
    public static final String REMOTE_STORE_URL = "https://github.com/petrs/JCAppStoreContent.git";
    public static final String REMOTE_STORE_LATEST_URL = "https://api.github.com/repos/petrs/JCAppStoreContent/releases/latest";
    public static final String REPO_ISSUES = "https://github.com/JavaCardSpot-dev/JCAppStore/issues";
    public static final String FILE_INFO_PREFIX = "info_";
    public static final String FILE_INFO_SUFFIX = ".json";

    public static final String RESOURCES = APP_STORE_DIR + S + "Resources" + S;

    /**
     * Gets the default app folder root location
     * @return
     */
    public static String getDefaultAppRootFolder() {
        //from  https://stackoverflow.com/questions/8782797/creating-directory-in-application-support-or-appdata
        String appFolder = System.getProperty("user.home") + S + "JCAppStore";
        File directory = new File(appFolder);
        if (!directory.exists() && (!directory.mkdirs())) {
            throw new RuntimeException("The Application doesn't have the rights to store data into default folder.");
        }
        return appFolder;
    }

    public static boolean setupLogger() {
        MainMapLookup.setMainArguments(APP_LOG_DIR.getAbsolutePath(), LOG_FILENAME, LOG_FILENAME_EXT);
        return true;
    }

    private static File checkFolders(String folder) {
        File f = new File(folder);
        if (!f.exists()) {
            if (!f.mkdir()) {
                throw new RuntimeException("The app cannot create folders in the documents. Allow this operation first.");
            }
        }
        if (!f.isDirectory())
            throw new RuntimeException("File 'folder' already exists. Move or delete this file to stat this app.");
        return f;
    }
}
