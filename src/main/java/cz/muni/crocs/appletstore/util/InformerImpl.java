package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Informable;
import cz.muni.crocs.appletstore.ui.Warning;

import javax.swing.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class InformerImpl implements Informer, CallBack<Void> {

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
    public void showWarningToClose(String msg, Warning.Importance status) {
        warning = new Warning(msg, status, Warning.CallBackIcon.CLOSE, this);
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
