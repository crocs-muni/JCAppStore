package cz.muni.crocs.appletstore.iface;

/**
 * Allows to track the process, for progress bars updates.
 */
public interface ProcessTrackable extends Runnable {

    /**
     * Get the progress
     * @return progress amount
     */
    int getProgress();

    /**
     * Set progress to new value. Does not check maximum.
     *  !!! Cannot use 'safeSetProgress' !!!
     * @param amount progress value to set
     */
    void updateProgress(int amount);

    /**
     * Return max value
     */
    int getMaximum();

    /**
     * Get info about progress
     * @return description on current progress
     */
    String getInfo();

    /**
     * Set message for the tracking interface
     * @param msg message to display
     */
    void setLoaderMessage(String msg);

    /**
     * Set progress safely - won't overcome 100
     * @param amount update the progress
     */
    default void safeSetProgress(int amount) {
        updateProgress(Math.min(amount, getMaximum()));
    }

    /**
     * Increase progress by one.
     */
    default void raiseProgressByOne() {
        if (getProgress() < 100)
            updateProgress(getProgress() + 1);
    }

    default boolean finished() {
        return getProgress() >= getMaximum();
    }
}
