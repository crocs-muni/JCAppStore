package cz.muni.crocs.appletstore.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.Store;
import cz.muni.crocs.appletstore.iface.ProcessTrackable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
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
    private static final ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private final Store parent;

    /**
     * Create a new worker
     * @param parent store parent to notify about progress
     */
    public StoreWorker(Store parent) {
        this.parent = parent;
    }

    /**
     * Forward the message to parent store
     * @param message message to forward
     */
    public void setLoaderMessage(String message) {
        SwingUtilities.invokeLater(() -> parent.setProcessMessage(message));
    }

    /**
     * Verifies the connection and version of latest downloaded release.
     * Re-downloads the store if newer version found or crucial store files missing.
     * @return store state. NO_CONNECTION if no internet found, FAILED if failed and REBUILD if successfully completed
     */
    @Override
    public Store.State doInBackground() {
        setProgress(0);

        final Options<String> options = OptionsFactory.getOptions();
        String[] storeInfo = checkAndGetLatestReleaseVersion(options.getOption(Options.KEY_GITHUB_LATEST_VERSION));
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
                            + OptionsFactory.getOptions().getLanguage().getLocaleString()
                            + Config.FILE_INFO_SUFFIX).exists();
        }
    }

    /**
     * Check for the latest version and internet connection as well
     * @param currentVersion string of the latest version downloaded
     * @return url for download if not up to date, "ok" if up to date, null if no internet connection
     */
    private String[] checkAndGetLatestReleaseVersion(String currentVersion) {

        JsonElement root = GitHubApiGetter.getJsonContents(Config.REMOTE_STORE_LATEST_URL);
        setProgress(50);
        if (root == null) return null;
        JsonObject latest = root.getAsJsonObject();

        String latestVersion = latest.get("name").getAsString();

        return new String[] {
                currentVersion.equals(latestVersion) ? "ok" : "nok",
                latest.get("zipball_url").getAsString(),
                latestVersion
        };
    }
}

