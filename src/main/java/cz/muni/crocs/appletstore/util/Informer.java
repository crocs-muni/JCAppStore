package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Informable;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.Notice;

import javax.swing.*;

/**
 * Informer that uses Informable instance inside to display the messages.
 * should be able to handle more messages at once
 */
public interface Informer {

    int INFINITY = Integer.MAX_VALUE;

    /**
     * Set informable isntance - this will take care of displaying all messages.
     * Up until now, if some messages were introduced, these are stored and displayed until first call of this function.
     * @param delegate delegate that can take care of message displays
     */
    void setInformableDelegate(Informable delegate);

    /**
     * Show ingo to the user
     * @param info string text to show
     */
    void showMessage(String info);

    /**
     * Show info that replaces the screen
     * @param panel to show
     */
    void showFullScreenInfo(JPanel panel);

    /**
     * Show warning to the user with custom callback to perform on icon click
     * @param msg message text to show
     * @param status status level of the Warning.Importance enum, e.g. SEVERE, INFO ...
     * @param icon icon to show
     * @param callable action to perform on click
     */
    void showInfo(String msg, Notice.Importance status, Notice.CallBackIcon icon, CallBack<Void> callable);

    /**
     * Show warning to the user with close option
     * @param msg message to show
     * @param status status level of the Warning.Importance enum, e.g. SEVERE, INFO ...
     */
    void showInfoToClose(String msg, Notice.Importance status);

    /**
     * Show warning to the user with custom callback to perform on icon click
     * @param msg message text to show
     * @param status status level of the Warning.Importance enum, e.g. SEVERE, INFO ...
     * @param icon icon to show
     * @param callable action to perform on click
     * @param milis duration after which the message is closed, null if do not close
     */
    void showInfo(String msg, Notice.Importance status, Notice.CallBackIcon icon, CallBack<Void> callable, Integer milis);

    /**
     * Show warning to the user with close option
     * @param msg message to show
     * @param status status level of the Warning.Importance enum, e.g. SEVERE, INFO ...
     * @param milis duration after which the message is closed
     */
    void showInfoToClose(String msg, Notice.Importance status, Integer milis);

    /**
     * Close current displayed warning or do nothing if no warn displayed
     */
    void closeInfo();
}
