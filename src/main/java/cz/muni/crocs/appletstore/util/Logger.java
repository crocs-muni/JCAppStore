package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.AppletStore;
import cz.muni.crocs.appletstore.Config;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Logger {

    private final static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AppletStore.class.getName());
    private static Handler fh;

    private static void write() {
        if (fh != null) {
            fh.flush();
            fh.close();
            fh = null;
        }
    }

    public static void load() throws IOException {
        write();
        fh = new FileHandler(Config.APP_DATA_DIR + "error.log", true);  // append is true
        LOGGER.addHandler(fh);
    }

    public static void log(Level level, Exception cause) {
        LOGGER.log(level, cause.getMessage(), cause);
    }
    //TODO log errors white at app close
}
