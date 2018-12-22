package cz.muni.crocs.appletstore.iface;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public interface ProcessTrackable {

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
     * Set progress safely - won't overcome 100
     * @param amount
     */
    default void safeSetProgress(int amount) {
        if (amount > 100)
            updateProgress(100);
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
