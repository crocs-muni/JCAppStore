package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.ui.Warning;

/**
 * Informer that uses Informable instance inside to display the messages.
 * should be able to handle more messages at once
 */
public interface Informer {

    public static int INFINITY = Integer.MAX_VALUE;

    /**
     * Show ingo to the user
     * @param info string text to show
     */
    void showInfo(String info);

    /**
     * Show warning to the user with custom callback to perform on icon click
     * @param msg message text to show
     * @param status status level of the Warning.Importance enum, e.g. SEVERE, INFO ...
     * @param icon icon to show
     * @param callable action to perform on click
     */
    void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable);

    /**
     * Show warning to the user with close option
     * @param msg message to show
     * @param status status level of the Warning.Importance enum, e.g. SEVERE, INFO ...
     */
    void showWarningToClose(String msg, Warning.Importance status);

    /**
     * Show warning to the user with custom callback to perform on icon click
     * @param msg message text to show
     * @param status status level of the Warning.Importance enum, e.g. SEVERE, INFO ...
     * @param icon icon to show
     * @param callable action to perform on click
     * @param milis duration after which the message is closed, null if do not close
     */
    void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable, Integer milis);

    /**
     * Show warning to the user with close option
     * @param msg message to show
     * @param status status level of the Warning.Importance enum, e.g. SEVERE, INFO ...
     * @param milis duration after which the message is closed
     */
    void showWarningToClose(String msg, Warning.Importance status, Integer milis);

    /**
     * Close current displayed warning or do nothing if no warn displayed
     */
    void closeWarning();
}
