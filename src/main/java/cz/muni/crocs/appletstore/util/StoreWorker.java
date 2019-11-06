package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.Store;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Store downloader which checks internet connection,
 *   and launches AppletDownloader if not up to date
 *   returns Store.State value to update the store accordingly
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWorker extends SwingWorker<Store.State, Object> implements ProcessTrackable {

    private static final Logger logger = LogManager.getLogger(StoreWorker.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private Store parent;

    public StoreWorker(Store parent) {
        this.parent = parent;
    }

    public void setLoaderMessage(String message) {
        SwingUtilities.invokeLater(() -> parent.setProcessMessage(message));
    }

    @Override
    public Store.State doInBackground() {
        setProgress(0);

        final Options<String> options = OptionsFactory.getOptions();
        String[] storeInfo = GitHubInternetConnection.checkAndGetLatestReleaseVersion(
                options.getOption(Options.KEY_GITHUB_LATEST_VERSION)
        );

        if (storeInfo == null) {
            setProgress(100);
            setLoaderMessage(textSrc.getString("E_no_internet"));
            return Store.State.NO_CONNECTION;
        } else if (storeInfo[0].equals("ok") && checkValidStoreDir()) {
            setProgress(100);
            setLoaderMessage(textSrc.getString("done"));
            return Store.State.REBUILD;
        }
        StoreDownloader downloader = new StoreDownloader(storeInfo[1], this);
        if (!downloader.run()) {
            return Store.State.FAILED;
        }
        else {
            options.addOption(Options.KEY_GITHUB_LATEST_VERSION, storeInfo[2]);
            return Store.State.REBUILD;
        }
    }

    @Override
    protected void done() {
      //  do nothing
    }

    /**
     * Check if the folder is not damaged by user or system
     *
     * @return true if not necessary to re-download
     */
    private static boolean checkValidStoreDir() {
        String[] files = Config.APP_STORE_DIR.list();
        if (files == null) {
            logger.error("Could not read store folder: " + Config.APP_ROOT_DIR);
            return false;
        }  else if (files.length < 3) {
            logger.error("Missing files in store folder: " + Config.APP_ROOT_DIR);
            return false;
        } else {
            return Config.APP_STORE_CAPS_DIR.exists() &&
                    new File(Config.APP_STORE_DIR, Config.FILE_INFO_PREFIX
                            + OptionsFactory.getOptions().getOption(Options.KEY_LANGUAGE)
                            + Config.FILE_INFO_SUFFIX).exists();
        }
    }

    @Override
    public void updateProgress(int amount) {
        setProgress(amount);
    }

    @Override
    public int getMaximum() {
        return 100;
    }

    @Override
    public String getInfo() {
        throw new UnsupportedOperationException();
    }
}

