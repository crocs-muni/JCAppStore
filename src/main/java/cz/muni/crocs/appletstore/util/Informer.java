package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.ui.Warning;

public interface Informer {

    /**
     * Show ingo to the user
     * @param info string text to show
     */
    void showInfo(String info);

    /**
     * Show warning to the user
     * @param msg message text to show
     * @param status status level of the warning, e.g. SEVERE, INFO ...
     * @param icon icon to show
     * @param callable action to perform on click
     */
    void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable);

    /**
     * Show warning to the user
     * @param msg message to show
     * @param status status level of the warning, e.g. SEVERE, INFO ...
     */
    void showWarningToClose(String msg, Warning.Importance status);

    /**
     * Close current warning or do nothing
     */
    void closeWarning();
}
