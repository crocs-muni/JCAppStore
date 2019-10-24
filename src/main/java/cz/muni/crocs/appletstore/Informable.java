package cz.muni.crocs.appletstore;

import javax.swing.*;

/**
 * Interface for informer.
 * Class that implements should be able to display to the user
 * GUI swing messages using following methods.
 */
public interface Informable {

    /**
     * Show supplied component to user
     * @param component component to show
     */
    void showWarning(JComponent component);

    /**
     * Hide supplied component if shown
     */
    void hideWarning();

    /**
     * Show info to user
     * @param info information text
     */
    void showInfo(String info);
}
