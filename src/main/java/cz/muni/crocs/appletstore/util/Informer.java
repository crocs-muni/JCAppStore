package cz.muni.crocs.appletstore.util;


import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.TabbedPaneSimulator;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.Warning;

import javax.sound.sampled.Line;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Informer {

    private static Informer informer = null;
    private TabbedPaneSimulator context;

    private Informer(TabbedPaneSimulator context) {
        this.context = context;
    }

    public static void init(TabbedPaneSimulator parent) {
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

    public void showWarning(int translationId, Warning.Importance status, CallBack callable) {
        context.showWarning(translationId, status, callable);
    }

    public void closeWarning() {
        context.closeWarning();
    }
}
