package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.MouseAdapter;

public abstract class CardAction extends MouseAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CardAction.class);
    protected final OnEventCallBack<Void, Void> call;

    public CardAction(OnEventCallBack<Void, Void> call) {
        this.call = call;
    }

    protected void execute(CardExecutable r, String loggerMessage, String title) {
        call.onStart();
        new Thread(() ->  {
            try {
                r.execute();
            } catch (LocalizedCardException ex) {
                ex.printStackTrace();
                logger.warn(loggerMessage + ex.getMessage());
                SwingUtilities.invokeLater(() -> showFailed(title, ex.getLocalizedMessage()));
                SwingUtilities.invokeLater(call::onFail);
            }
            SwingUtilities.invokeLater(call::onFinish);
        }).start();
    }

    protected void showFailed(String title, String message) {
        JOptionPane.showMessageDialog(null,
                "<html><div width=\"350\">" + message + "</div></html>",
                title, JOptionPane.ERROR_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "error.png"));
    }

    @FunctionalInterface
    public interface CardExecutable {
        void execute() throws LocalizedCardException;
    }
}
