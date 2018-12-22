package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.StoreWindowPane;
import cz.muni.crocs.appletstore.iface.ProcessTrackable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class DownloaderWorker extends SwingWorker<String, Object> implements ProcessTrackable {

    private static final Logger logger = LogManager.getLogger(DownloaderWorker.class);
    private StoreWindowPane parent;

    public DownloaderWorker(StoreWindowPane parent) {
        this.parent = parent;
    }

    public void setLoaderMessage(int message) {
        parent.setLoadingPaneMessage(message);
    }

    @Override
    public String doInBackground() {
        setProgress(0);

        String[] storeInfo = InternetConnection.checkAndGetLatestReleaseVersion(
                Config.options.get(Config.OPT_KEY_GITHUB_LATEST_VERSION)
        );

        if (storeInfo == null) {
            setProgress(100);
            setLoaderMessage(68);
            parent.setStatus(StoreWindowPane.StoreState.NO_CONNECTION);
            return "done";
        } else if (storeInfo[0].equals("ok") && checkNotEmpty()) {
            setProgress(100);
            setLoaderMessage(111);
            parent.setStatus(StoreWindowPane.StoreState.REBUILD);
            return "done";
        }
        System.out.println("downloading");
        AppletDownloader downloader = new AppletDownloader(storeInfo[1], this);
        if (!downloader.run()) parent.setStatus(StoreWindowPane.StoreState.FAILED);
        else parent.setStatus(StoreWindowPane.StoreState.REBUILD);

        return storeInfo[2];
    }

    @Override
    protected void done() {
      //  parent.update();
    }


    /**
     * Check if the folder is not damaged by user or system
     *
     * @return true if not necessary to re-download
     */
    private static boolean checkNotEmpty() {
        //TODO try to be more sophisticated about the file content, eg. lookup specific file
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
}

