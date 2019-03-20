package cz.muni.crocs.appletstore.util;


import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.MainPanel;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.Warning;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Informer {

    private static Informer informer = null;
    private MainPanel context;

    private Informer(MainPanel context) {
        this.context = context;
    }

    public static void init(MainPanel parent) {
        informer = new Informer(parent);
    }

    public static Informer getInstance() {
        return informer;
    }

    public void showInfo(String info) {
        context.showInfo(info);
    }

    public void showInfo(int translationId) {
        context.showInfo(Config.translation.get(translationId));
    }

    public void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable) {
        context.showWarning(msg, status, icon, callable);
    }

    public void showWarningToClose(String msg, Warning.Importance status) {
        context.showWarning(msg, status, Warning.CallBackIcon.CLOSE, context);
    }

    public void showWarningToClose(int translationId, Warning.Importance status) {
        context.showWarning(Config.translation.get(translationId), status, Warning.CallBackIcon.CLOSE, context);
    }

    public void showWarning(int translationId, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable) {
        context.showWarning(Config.translation.get(translationId), status, icon, callable);
    }

    public void closeWarning() {
        context.closeWarning();
    }
}
