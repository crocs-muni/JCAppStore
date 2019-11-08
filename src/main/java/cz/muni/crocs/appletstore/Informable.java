package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.ErrorPane;

import javax.swing.*;

/**
 * Interface for informer.
 * Class that implements should be able to display to the user
 * GUI swing messages using following methods.
 */
public interface Informable {

    /**
     * Show info covering the full screen, used when not possible to load the contents
     * @param pane panel to show
     */
    void showFullScreenInfo(JPanel pane);

    /**
     * Show supplied component to user
     * @param component component to show
     */
    void showInfo(JComponent component);

    /**
     * Hide supplied component if shown
     */
    void hideInfo();

    /**
     * Show info to user
     * @param info information text
     */
    void showMessage(String info);
}
