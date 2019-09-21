package cz.muni.crocs.appletstore.util;

/**
 * Allows to track the process, for progress bars update.
 * @author Jiří Horák
 * @version 1.0
 */
public interface ProcessTrackable extends Runnable {

    /**
     * Get the progress
     * @return progress amount
     */
    int getProgress();

    /**
     * Set progress to new value
     * @param amount progress value to set
     */
    void updateProgress(int amount);

    /**
     * Return max value
     */
    int getMaximum();

    /**
     * Get info about progress
     * @return
     */
    String getInfo();

    /**
     * Set message for the tracking interface
     * @param msg message to display
     */
    void setLoaderMessage(String msg);

    /**
     * Set progress safely - won't overcome 100
     * @param amount
     */
    default void safeSetProgress(int amount) {
        if (amount > getMaximum())
            updateProgress(getMaximum());
        else
            updateProgress(amount);
    }

    /**
     * Increase progress by one.
     */
    default void raiseProgressByOne() {
        if (getProgress() < 100)
            updateProgress(getProgress() + 1);
    }
}
