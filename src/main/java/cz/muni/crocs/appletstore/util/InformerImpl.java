package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Informable;
import cz.muni.crocs.appletstore.ui.Warning;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class InformerImpl implements Informer, CallBack<Void> {

    public static final Integer DELAY = 8000;

    private Informable context;
    private volatile Deque<Tuple<Warning, Integer>> queue = new LinkedBlockingDeque<>();
    private volatile Deque<Warning> toClose = new LinkedBlockingDeque<>();
    private volatile Boolean busy = false;

    public InformerImpl(Informable context) {
        this.context = context;
    }

    @Override
    public void showInfo(String info) {
        context.showInfo(info);
    }

    @Override
    public void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable) {
        showWarning(msg, status, icon, callable, DELAY);
    }

    @Override
    public void showWarningToClose(String msg, Warning.Importance status) {
        showWarningToClose(msg, status, DELAY);
    }

    @Override
    public void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable, Integer milis) {
        queue.add(new Tuple<>(new Warning(msg, status, icon, callable == null ? this : callable), milis));
        fireWarning();
    }

    @Override
    public void showWarningToClose(String msg, Warning.Importance status, Integer milis) {
        queue.add(new Tuple<>(new Warning(msg, status, Warning.CallBackIcon.CLOSE, this), milis));
        fireWarning();
    }


    @Override
    public void closeWarning() {
        if (!toClose.isEmpty()) {
            context.hideWarning(toClose.pop());
        }
    }

    @Override
    public Void callBack() {
        closeWarning();
        return null;
    }

    private synchronized void fireWarning() {
        if (busy)
            return;

        busy = true;
        System.out.println("Running");
        new Thread(() -> {
            while (true) {
                System.out.println(queue.toString());

                if (queue.isEmpty()) {
                    busy = false;
                    return;
                }
                final Tuple<Warning, Integer> next = queue.pop();
                toClose.add(next.first);

                System.out.println(next.first.getBackground());
                SwingUtilities.invokeLater(() -> {
                    context.showWarning(next.first);
                });
                try {
                    Thread.sleep(next.second);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SwingUtilities.invokeLater(this::closeWarning);
            }
        }).start();
    }
}
