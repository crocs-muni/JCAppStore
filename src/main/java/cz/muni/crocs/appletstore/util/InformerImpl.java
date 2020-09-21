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
    private Thread current;
    private static final Integer DELAY = 8000;

    private Informable context;
    private final Deque<Tuple<Notice, Integer>> queue = new LinkedBlockingDeque<>();
    private volatile Boolean busy = false;

    private String lastNotDisplayedMessage;
    private JPanel lastNotDisplayedPanel;

    @Override
    public void setInformableDelegate(Informable delegate) {
        this.context = delegate;

        //some notifications?
        fireInfo();

        //some messages?
        if (lastNotDisplayedMessage != null) {
            showMessage(lastNotDisplayedMessage);
            lastNotDisplayedMessage = null;
        }

        //some panel to show?
        if (lastNotDisplayedPanel != null) {
            showFullScreenInfo(lastNotDisplayedPanel);
            lastNotDisplayedPanel = null;
        }
    }

    @Override
    public void showMessage(String info) {
        if (context == null) lastNotDisplayedMessage = info;
        else SwingUtilities.invokeLater(() -> context.showMessage(info));
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
            queue.add(new Tuple<>(new Notice(msg, status, icon, callable, this), milis));
            if (context != null) fireInfo();
        }
    }

    @Override
    public void showInfoToClose(String msg, Notice.Importance status, Integer milis) {
        queue.add(new Tuple<>(new Notice(msg, status, Notice.CallBackIcon.CLOSE, this), milis));
        if (context != null) fireInfo();
    }

    @Override
    public void closeInfo() {
        context.hideInfo();
    }

    @Override
    public Void callBack() {
        closeInfo();
        current.interrupt();
        return null;
    }

    private Thread getNewNoticeSwitcherThread() {
        return new Thread(() -> {
            while (true) {
                if (queue.isEmpty()) {
                    current = null;
                    busy = false;
                    return;
                }
                final Tuple<Notice, Integer> next = queue.pop();
                SwingUtilities.invokeLater(() -> {
                    context.showInfo(next.first);
                });

                if (next.second != null) {
                    try {
                        Thread.sleep(next.second);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    SwingUtilities.invokeLater(this::closeInfo);
                } else {
                    try {
                        Thread.currentThread().wait();
                    } catch (InterruptedException e) {
                        //do nothing
                    }
                }
            }
        });
    }

    private synchronized void fireInfo() {
        if (busy || queue.isEmpty())
            return;
        busy = true;
        current = getNewNoticeSwitcherThread();
        current.start();
    }
}
