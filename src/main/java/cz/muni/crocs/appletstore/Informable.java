package cz.muni.crocs.appletstore;

import javax.swing.*;

public interface Informable {

    /**
     * Show supplied component to user
     * @param component component to show
     */
    void showWarning(JComponent component);

    /**
     * Hide supplied component if shown
     * @param component component component to hide
     */
    void hideWarning(JComponent component);

    /**
     * Show info to user
     * @param info information text
     */
    void showInfo(String info);
}
