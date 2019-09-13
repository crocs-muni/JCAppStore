package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.StoreWindowManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Store downloader which checks internet connection,
 *   and launches AppletDownloader if not up to date
 *   returns "done" when up to date or no connection
 *   returns "&lt;version&gt;" version of the newest obtained store
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class DownloaderWorker extends SwingWorker<String, Object> implements ProcessTrackable {

    private static final Logger logger = LogManager.getLogger(DownloaderWorker.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private StoreWindowManager parent;

    //todo ugly dependency
    public DownloaderWorker(StoreWindowManager parent) {
        this.parent = parent;
    }

    public void setLoaderMessage(String message) {
        parent.setLoadingPaneMessage(message);
    }

    @Override
    public String doInBackground() {
        setProgress(0);

        String[] storeInfo = InternetConnection.checkAndGetLatestReleaseVersion(
                OptionsFactory.getOptions().getOption(Options.KEY_GITHUB_LATEST_VERSION)
        );

        if (storeInfo == null) {
            setProgress(100);
            setLoaderMessage(textSrc.getString("E_no_internet"));
            parent.setStatus(StoreWindowManager.StoreState.NO_CONNECTION);
            return "done";
        } else if (storeInfo[0].equals("ok") && checkNotEmpty()) {
            setProgress(100);
            setLoaderMessage(textSrc.getString("done"));
            parent.setStatus(StoreWindowManager.StoreState.REBUILD);
            return "done";
        }
        System.out.println("downloading");
        AppletDownloader downloader = new AppletDownloader(storeInfo[1], this);
        if (!downloader.run()) parent.setStatus(StoreWindowManager.StoreState.FAILED);
        else parent.setStatus(StoreWindowManager.StoreState.REBUILD);

        return storeInfo[2];
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
    private static boolean checkNotEmpty() {
        //TODO try to be more sophisticated about the file contents, eg. lookup specific file/s
        String[] files = Config.APP_STORE_DIR.list();
        if (files != null && files.length == 0) {
            return false;
        } else if (files == null) {
            logger.error("Could not read store folder: " + Config.APP_ROOT_DIR);
            return false;
        } else {
            return true;
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

