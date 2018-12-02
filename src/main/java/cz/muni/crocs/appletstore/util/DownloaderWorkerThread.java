package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class DownloaderWorkerThread implements Callable<WorkerThreadResult> {

    private static final Logger logger = LogManager.getLogger(DownloaderWorkerThread.class);
    private int progress = 0;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int amount) {
        if (amount > 100)
            progress = 100;
        else
            progress = amount;
    }

    public void raiseProgressByOne() {
        if (progress < 100)
            progress++;
    }


    @Override
    public WorkerThreadResult call() {
        //get info about connection
        String[] storeInfo = InternetConnection.checkAndGetLatestReleaseVersion(
                Config.options.get(Config.OPT_KEY_GITHUB_LATEST_VERSION)
        );

        if (storeInfo == null) {
            progress = 100;
            return WorkerThreadResult.NO_CONNECTION;
        }  else if (storeInfo[0].equals("ok") && checkNotEmpty()) {
            progress = 100;
            return WorkerThreadResult.OK;
        }

        //download all necessary files
        Config.options.put(Config.OPT_KEY_GITHUB_LATEST_VERSION, storeInfo[1]);
        AppletDownloader downloader = new AppletDownloader(
                Integer.valueOf(storeInfo[2]), storeInfo[0], this
        );
        if (!downloader.run()) return WorkerThreadResult.FAILED;
        return WorkerThreadResult.OK;
    }

    /**
     * Check if the folder is not damaged by user or system
     * @return true if not necessary to re-download
     */
    private static boolean checkNotEmpty() {
        //TODO try to be more sophisticated about the file content, eg. lookup specific file
        File file = new File(Config.APP_STORE_DIR);
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files != null && files.length == 0) {
                return false;
            } else if (files == null) {
                logger.error("Could not read store folder: " + Config.APP_ROOT_DIR);
                return false;
            } else {
                return true;
            }
        }
        logger.error("There is file with name: " + Config.APP_ROOT_DIR + ", but it's no folder.");
        return false;
    }
}

