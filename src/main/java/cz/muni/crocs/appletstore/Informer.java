package cz.muni.crocs.appletstore;


import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.Sources;

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

    public void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable) {
        context.showWarning(msg, status, icon, callable);
    }

    public void showWarningToClose(String langKey, Warning.Importance status) {
        context.showWarning(Sources.language.get(langKey), status, Warning.CallBackIcon.CLOSE, context);
    }

    public void closeWarning() {
        context.closeWarning();
    }
}
