package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.card.UnknownKeyException;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.awt.event.MouseAdapter;
import java.util.ResourceBundle;

/**
 * Abstract card action wrapper providing failure management
 */
public abstract class CardAbstractAction extends MouseAdapter implements CardAction {
    private static final Logger logger = LoggerFactory.getLogger(CardAbstractAction.class);

    protected static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    protected final OnEventCallBack<Void, Void> call;

    public CardAbstractAction(OnEventCallBack<Void, Void> call) {
        this.call = call;
    }

    @Override
    public void start() {
        mouseClicked(null);
    }

    /**
     * Routine wrapper for the card action execution providing error handling and
     * unknown key management
     *
     * @param toExecute     CardExecutable implementation that is to be executed
     * @param loggerMessage message to write into logger on a failure
     * @param title         title (must be translated!) for the user dialog on failure
     */
    protected void execute(CardExecutable toExecute, String loggerMessage, String title) {
        call.onStart();
        new Thread(() -> {
            try {
                toExecute.execute();
            } catch (UnknownKeyException e) {
                if (!handleUnknownKey(toExecute, loggerMessage, title, e)) return;
            } catch (LocalizedCardException ex) {
                caught(title, loggerMessage, ex);
                //todo show
                return;
            }
            SwingUtilities.invokeLater(call::onFinish);
        }).start();
    }

    protected boolean handleUnknownKey(CardExecutable toExecute, String loggerMessage, String title, UnknownKeyException e) {
        return handleUnknownKey(toExecute, loggerMessage, title, null, e);
    }

    protected boolean handleUnknownKey(CardExecutable toExecute, String loggerMessage, String title, String image, UnknownKeyException e) {
        try {
            InformerFactory.getInformer().showFullScreenInfo(
                    new ErrorPane(textSrc.getString("E_unknown_key"), "lock.png"));
            new UnknownKeyHandler(toExecute, e).handle();
        } catch (LocalizedCardException ex) {
            if (image != null) caught(title, loggerMessage, image, ex);
            else caught(title, loggerMessage, ex);
            return false;
        } catch (UnknownKeyException ex) {
            ex.printStackTrace();
            logger.error("UnknownKeyException after key was set. Should not even get here.", ex);
            SwingUtilities.invokeLater(call::onFail);
            InformerFactory.getInformer().showFullScreenInfo(
                    new ErrorPane(textSrc.getString("E_authentication"),
                            textSrc.getString("E_master_key_not_found"), "lock.png"));
            return false;
        }
        return true;
    }

    protected static void showFailed(String title, String message) {
        JOptionPane.showMessageDialog(null,
                "<html><div width=\"350\">" + message + "</div></html>",
                title, JOptionPane.ERROR_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "error.png"));
    }

    private void caught(String title, String loggerMessage, LocalizedCardException e) {
        e.printStackTrace();
        logger.warn(loggerMessage, e);
        if (title != null) {
            SwingUtilities.invokeLater(() -> showFailed(title, e.getLocalizedMessage()));
        }
        SwingUtilities.invokeLater(call::onFail);
    }

    private void caught(String title, String loggerMessage, String image, LocalizedCardException e) {
        e.printStackTrace();
        logger.warn(loggerMessage + e.getMessage());
        if (title != null) {
            InformerFactory.getInformer().showFullScreenInfo(new ErrorPane(textSrc.getString(title), e.getLocalizedMessage(), image));
        }
        SwingUtilities.invokeLater(call::onFail);
    }

    /**
     * Callback for the action implementation
     */
    @FunctionalInterface
    public interface CardExecutable {
        void execute() throws LocalizedCardException, UnknownKeyException;
    }

    /**
     * Idle task for card executable
     */
    public static class CardExecutableIdle implements CardExecutable {
        private static CardExecutable self = new CardExecutableIdle();
        private CardExecutableIdle() {}

        public static CardExecutable get() {
            return self;
        }
        @Override
        public void execute() {}
    }

    /**
     * Handler for the unknown key event
     * tries to use 4041...4E4F default test key for authentication
     * if user agrees or fails while showing the failure cause.
     */
    class UnknownKeyHandler {
        private UnknownKeyException e;
        private CardExecutable failed;

        UnknownKeyHandler(CardExecutable failedTask, UnknownKeyException e) {
            this.e = e;
            this.failed = failedTask;
        }

        void handle() throws LocalizedCardException, UnknownKeyException {
            CardManager manager = CardManagerFactory.getManager();

            if (useDefaultTestKey() == JOptionPane.YES_OPTION) {
                manager.setTryGenericTestKey();
                manager.setReloadCard();
                failed.execute();
            } else {
                logger.warn(e.getMessage());
                InformerFactory.getInformer().showFullScreenInfo(new ErrorPane(textSrc.getString("E_authentication"),
                        textSrc.getString("E_master_key_not_found"), "lock.png"));
            }
        }

        int useDefaultTestKey() {
            return JOptionPane.showConfirmDialog(
                    null,
                    new HtmlText(textSrc.getString("I_use_default_keys_1") +
                            "<br>" + textSrc.getString("master_key") + ": <b>404142434445464748494A4B4C4D4E4F</b>" +
                            textSrc.getString("I_use_default_keys_2")),
                    textSrc.getString("key_not_found"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    new ImageIcon(Config.IMAGE_DIR + ""));
        }
    }
}
