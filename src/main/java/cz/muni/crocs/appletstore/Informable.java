package cz.muni.crocs.appletstore;

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
     * @param milis how long the info lasts, 0 if forever
     */
    void showInfo(JComponent component, int milis);

    /**
     * Hide supplied component if shown
     */
    void hideInfo();

    /**
     * Show info to user
     * @param info information text
     * @param image image name (relative path to image src dir)
     */
    void showInfoMessage(Object info, String image);


    void showMessage(String title, Object message, String image);


    void showQuestion(String title, Object message, String image);
}
