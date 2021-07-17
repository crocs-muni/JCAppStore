package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Informable;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.Notice;
import org.bouncycastle.crypto.engines.EthereumIESEngine;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Informer to remove cyclic dependency in component hierarchy (child wants to display message on an predecessor)
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class InformerImpl implements Informer, CallBack<Void> {
    private static final Integer DELAY = 8000;

    private Informable context;
    private final ArrayList<Tuple<Notice, Integer>> queue = new ArrayList<>();

//    private Tuple<String, String> lastNotDisplayedMessage;
    private JPanel lastNotDisplayedPanel;

    @Override
    public void setInformableDelegate(Informable delegate) {
        this.context = delegate;

        //some notifications?
        for (Tuple<Notice, Integer> info : queue) delegate.showInfo(info.first, info.second);

        //some messages?
        //TODO re-implement messages before inofrmer delegate initialized ? some messages?
//        if (lastNotDisplayedMessage != null) {
//            showInfoMessage(lastNotDisplayedMessage.first, lastNotDisplayedMessage.second);
//            lastNotDisplayedMessage = null;
//        }

        //some panel to show?
        if (lastNotDisplayedPanel != null) {
            showFullScreenInfo(lastNotDisplayedPanel);
            lastNotDisplayedPanel = null;
        }
    }

    @Override
    public void showInfoMessage(Object info, String image) {
        if (context == null) return; // { lastNotDisplayedMessage = new Tuple<>(info, image); return; }
        SwingUtilities.invokeLater(() -> context.showInfoMessage(info, image));
    }

    @Override
    public void showMessage(String title, Object message, String image) {
        if (context == null) return; // { lastNotDisplayedMessage = new Tuple<>(info, image);return; }
        SwingUtilities.invokeLater(() -> context.showMessage(title, message, image));
    }

    @Override
    public void showQuestion(String title, Object message, String image) {
        if (context == null) return; // { lastNotDisplayedMessage = new Tuple<>(info, image);return; }
        SwingUtilities.invokeLater(() -> context.showQuestion(title, message, image));
    }

    @Override
    public void showFullScreenInfo(JPanel panel) {
        if (context == null) lastNotDisplayedPanel = panel;
        else SwingUtilities.invokeLater(() -> context.showFullScreenInfo(panel));
    }

    @Override
    public void showInfo(String msg, Notice.Importance status, Notice.CallBackIcon icon, CallBack<Void> callable) {
        showInfo(msg, status, icon, callable, DELAY);
    }

    @Override
    public void showInfoToClose(String msg, Notice.Importance status) {
        showInfoToClose(msg, status, DELAY);
    }

    @Override
    public void showInfo(String msg, Notice.Importance status, Notice.CallBackIcon icon,
                         CallBack<Void> callable, Integer milis) {
        if (callable == null) {
            showInfoToClose(msg, status, milis);
        } else {
            if (context != null) SwingUtilities.invokeLater(() ->
                    context.showInfo(new Notice(msg, status, icon, callable, this), milis));
            else queue.add(new Tuple<>(new Notice(msg, status, icon, callable, this), milis));
        }
    }

    @Override
    public void showInfoToClose(String msg, Notice.Importance status, Integer milis) {
        if (context != null) SwingUtilities.invokeLater(() ->
                context.showInfo(new Notice(msg, status, Notice.CallBackIcon.CLOSE, this), milis));
        else queue.add(new Tuple<>(new Notice(msg, status, Notice.CallBackIcon.CLOSE, this), milis));
    }

    @Override
    public void closeInfo() {
        SwingUtilities.invokeLater(() -> context.hideInfo());
    }

    @Override
    public Void callBack() {
        closeInfo();
        return null;
    }
}
