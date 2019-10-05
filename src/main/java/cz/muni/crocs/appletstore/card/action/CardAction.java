package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.MouseAdapter;

public abstract class CardAction extends MouseAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CardAction.class);
    private final OnEventCallBack<Void, Void, Void> call;

    public CardAction(OnEventCallBack<Void, Void, Void> call) {
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
                SwingUtilities.invokeLater(() -> {
                    showFailed(title,
                            OptionsFactory.getOptions().getOption(Options.KEY_ERROR_MODE).equals("verbose") ?
                                    ex.getLocalizedMessage() : ex.getLocalizedMessageWithoutCause());
                });
                SwingUtilities.invokeLater(call::onFail);
            }
            SwingUtilities.invokeLater(call::onFinish);
        }).start();

    }

    private void showFailed(String title, String message) {
        JOptionPane.showMessageDialog(null,
                "<html><duv width=\"350\">" + message + "</div></html>",
                title, JOptionPane.ERROR_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "error.png"));
    }

    @FunctionalInterface
    public interface CardExecutable {
        void execute() throws LocalizedCardException;
    }
}
