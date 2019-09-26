package cz.muni.crocs.appletstore;

import java.awt.image.BufferedImage;

/**
 * Class enables to update background with image
 * enables to disable and again enable whole GUI area
 */
public interface BackgroundChangeable {

    /**
     * Update background of the panel
     * @param image image to display on background
     */
    void updateBackground(BufferedImage image);

    /**
     * Get the panel height
     * @return panel height
     */
    int getHeight();

    /**
     * Get the panel width
     * @return panel width
     */
    int getWidth();

    /**
     * Enable or disable all inputs from user
     * @param enabled true if enabled
     */
    void switchEnabled(boolean enabled);
}
