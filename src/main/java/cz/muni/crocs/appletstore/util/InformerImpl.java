package cz.muni.crocs.appletstore.util;


import cz.muni.crocs.appletstore.MainPanel;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.iface.Informable;
import cz.muni.crocs.appletstore.ui.Warning;

import javax.swing.*;
import java.util.Locale;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class InformerImpl implements Informer, CallBack<Void> {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    //todo fronta hlášení, pokud null -> zobrazit jiný
    private Warning warning;
    private Informable context;

    public InformerImpl(Informable context) {
        this.context = context;
    }

    @Override
    public void showInfo(String info) {
        context.showInfo(info);
    }

    @Override
    public void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable) {
        warning = new Warning(msg, status, icon, callable == null ? this : callable);
        fireWarning(10000);
    }

    @Override
    public void showWarningToClose(String langKey, Warning.Importance status) {
        warning = new Warning(textSrc.getString(langKey), status, Warning.CallBackIcon.CLOSE, this);
        fireWarning(8000);
    }

    @Override
    public void closeWarning() {
        if (warning != null) {
            context.hideWarning(warning);
            warning = null;
        }
    }

    @Override
    public Void callBack() {
        closeWarning();
        return null;
    }

    private void fireWarning(int milis) {
        context.showWarning(warning);

        new Thread(() -> {
            try {
                Thread.sleep(milis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(this::closeWarning);
        }).start();
    }
}
