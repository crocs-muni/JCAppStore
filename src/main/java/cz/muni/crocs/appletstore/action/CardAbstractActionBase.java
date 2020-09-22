package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.CardStatusNotifiable;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.GUIFactory;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.LocalizedException;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/**
 * Abstract card action wrapper providing failure management
 * TODO consider remowing TRet parameter both from wapper and OnEventCallBack iface
 *
 * @author Jiří Horák
 * @version 1.0
 */
public abstract class CardAbstractActionBase<TRet, TArg> extends MouseAdapter implements CardAction {
    protected static final Logger logger = LoggerFactory.getLogger(CardAbstractActionBase.class);

    protected static ResourceBundle textSrc = ResourceBundle.getBundle("Lang",
            OptionsFactory.getOptions().getLanguageLocale());
    protected final OnEventCallBack<TRet, TArg> call;
    //a variable the execution result is stored in
    protected TArg result = null;

    private boolean runImplicitlyCallBack;

    protected CardAbstractActionBase(OnEventCallBack<TRet, TArg> call, boolean runImplicitlyCallBack) {
        this.call = call;
        this.runImplicitlyCallBack = runImplicitlyCallBack;
    }

    @Override
    public void start() {
        mouseClicked(null);
    }

    /**
     * Routine wrapper for the card action execution providing error handling and
     * unknown key management, this should be implemented by a wrapper (see job())
     *   iface: CardAction - the interface
     *       base: CardAbstractActionBase - the common error handling implementation
     *          wrappers: 1) Card...Wrapper1 - the class used in other actions, implements job as a single task
     *                    2) Card...Wrapper2 - the class used in other actions, implements job as a CRON job
     * @param toExecute     CardExecutable implementation that is to be executed
     * @param loggerMessage message to write into logger on a failure
     * @param title         title (must be translated!) for the user dialog on failure
     * @param timeOut       timeout after which is the execution cancelled
     * @param unitsMeaning  meaning for the timeUnits value
     */
    protected abstract void execute(CardExecutable<TArg> toExecute, String loggerMessage, String title, int timeOut,
                                    TimeUnit unitsMeaning);

    /**
     * Routine wrapper for the card action execution providing error handling and
     * unknown key management
     *    iface: CardAction - the interface
     *       base: CardAbstractActionBase - the common error handling implementation
     *          wrappers: 1) Card...Wrapper1 - the class used in other actions, implements job as a single task
     *                    2) Card...Wrapper2 - the class used in other actions, implements job as a CRON job
     * @param toExecute     CardExecutable implementation that is to be executed
     * @param loggerMessage message to write into logger on a failure
     * @param title         title (must be translated!) for the user dialog on failure
     */
    protected abstract void execute(CardExecutable<TArg> toExecute, String loggerMessage, String title);

    /**
     * Actual execution call on a thread. It constructs and returns the task for execute() to be launched there.

     * @param toExecute task to execute
     * @param loggerMessage message to write into logs upon a failure
     * @param title the translated string - title for failure dialog window
     * @return Job instance (a Thread, Executor service or other way of running the job on another thread)
     */
    protected Runnable job(CardExecutable<TArg> toExecute, String loggerMessage, String title) {
        if (runImplicitlyCallBack) call.onStart();
        return () -> {
            try {
                result = toExecute.execute();
                if (runImplicitlyCallBack) {
                    SwingUtilities.invokeLater(() -> {
                        if (result == null) call.onFinish();
                        else call.onFinish(result);
                    });
                }
            } catch (UnknownKeyException e) {
                handleUnknownKey(toExecute, loggerMessage, title, e);
            } catch (CardNotAuthenticatedException ex) {
                tryListUnauthenticated();
            } catch (LocalizedCardException ex) {
                if (ex.getImageName() != null) caughtGoFullScreen(title, loggerMessage, ex.getImageName(), ex);
                else caughtGoMessage(title, loggerMessage, ex);
            } catch (Exception e) {
                caughtGoMessage(null, "Unknown exception: " + e.getMessage(),
                        new LocalizedCardException(e, "E_unknown_error"));
            }
        };
    }

    /**
     * Task that handles unknown key error
     * @param toExecute     CardExecutable implementation that is to be executed again, it should be the task
     *                      that invoked the unknown key error
     * @param loggerMessage original (toExecute) message to write into logger on a failure
     * @param title         original (toExecute) title (must be translated!) for the user dialog on failure
     * @param image         image to display in case the handler fails
     * @param e             UnknownKeyException error that is being handled
     */
    protected void handleUnknownKey(CardExecutable<TArg> toExecute, String loggerMessage, String title,
                                    String image, UnknownKeyException e) {
        boolean implicit = false;
        try {
            InformerFactory.getInformer().showFullScreenInfo(
                    new ErrorPane(textSrc.getString("E_unknown_key"), "lock.png"));

            if (!runImplicitlyCallBack) SwingUtilities.invokeLater(call::onStart);
            implicit = runImplicitlyCallBack;
            runImplicitlyCallBack = true; //act as if it was already enabled -- wont run twice

            result = new UnknownKeyHandler<>(toExecute, e).handle();
        } catch (LocalizedCardException ex) {
            //allways fullscreen
            caughtGoFullScreen(title, loggerMessage, image, ex);
        } catch (CardNotAuthenticatedException ex) {
            logger.warn("Card is not to be authenticated to.", ex);
            tryListUnauthenticated();
        } catch (UnknownKeyException ex) {
            logger.warn("Card key failure: 2nd time. Running unauthorized version.", ex);
            tryListUnauthenticated();
        } finally {
            runImplicitlyCallBack = implicit;
        }
    }

    protected void handleUnknownKey(CardExecutable<TArg> toExecute, String loggerMessage, String title, UnknownKeyException e) {
        handleUnknownKey(toExecute, loggerMessage, title, "error_white.png", e);
    }

    protected void caughtGoMessage(String title, String loggerMessage, LocalizedException e) {
        e.printStackTrace();
        logger.warn(loggerMessage, e);
        if (title != null) {
            SwingUtilities.invokeLater(() -> showFailed(title, e.getLocalizedMessage()));
        }
        SwingUtilities.invokeLater(call::onFail);
    }

    protected void caughtGoFullScreen(String title, String loggerMessage, String image, LocalizedException e) {
        e.printStackTrace();
        logger.warn(loggerMessage + e.getMessage());
        if (title != null) {
            ErrorPane err = null;

            try {
                if (e.getUnsafeOperation() == null) {
                    err = new ErrorPane(title, e.getLocalizedMessage(), image);
                } else {
                    err = getErrorPaneWithOperation(title, image, e);
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            InformerFactory.getInformer().showFullScreenInfo(err);
        }
        SwingUtilities.invokeLater(call::onFail);
    }

    protected void tryListUnauthenticated() {
        if (!runImplicitlyCallBack) SwingUtilities.invokeLater(call::onStart);

        try {
            //attempt to load unauthorized card
            CardManagerFactory.getManager().loadCardUnauthorized();
            InformerFactory.getInformer().showInfoToClose(textSrc.getString("E_master_key_not_found"),
                    Notice.Importance.SEVERE, 35000);
            SwingUtilities.invokeLater(call::onFinish);

        } catch (LocalizedCardException unauthFail) {
            InformerFactory.getInformer().showFullScreenInfo(
                    new ErrorPane(textSrc.getString("E_authentication"),
                            textSrc.getString("E_master_key_not_found"), "lock.png"));
            SwingUtilities.invokeLater(call::onFail);
        }
    }

    protected ErrorPane getErrorPaneWithOperation(String title, String image, LocalizedException e) {
        return new ErrorPane(title, e.getLocalizedMessage(), image, e.getOperationLocalizedMsg(),
                (CallBack<Void>) () -> {
//                    new UnsafeCardOperationWrapper(new OnEventCallBack<Void, Void>() {
//                        @Override
//                        public void onStart() {
//                            call.onStart();
//                        }
//
//                        @Override
//                        public void onFail() {
//                            call.onFail();
//                        }
//
//                        @Override
//                        public Void onFinish() {
//                            call.onFinish();
//                            return null;
//                        }
//
//                        @Override
//                        public Void onFinish(Void aVoid) {
//                            call.onFinish(null);
//                            return null;
//                        }
//                    }, e.getUnsafeOperation()).mouseClicked(null);

                    //todo find solution how to get deafult OnEventCallBack running
                    InformerFactory.getInformer().showMessage("Not working yet as intended. Re-plug card to perform this operation.");
                    return null;
                });
    }

    protected static void showFailed(String title, String message) {
        JOptionPane.showMessageDialog(null,
                "<html><div width=\"350\">" + message + "</div></html>",
                title, JOptionPane.ERROR_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "error.png"));
    }

    /**
     * Callback for the action implementation
     */
    @FunctionalInterface
    public interface CardExecutable<T> {
        T execute() throws LocalizedCardException, UnknownKeyException, CardNotAuthenticatedException;
    }

    /**
     * Idle task for card executable
     */
    public static class CardExecutableIdle implements CardExecutable<Void> {
        private static final CardExecutable<Void> self = new CardExecutableIdle();

        private CardExecutableIdle() {
        }

        public static CardExecutable<Void> get() {
            return self;
        }

        @Override
        public Void execute() {
            return null;
        }
    }

    /**
     * Handler for the unknown key event
     * tries to use 4041...4E4F default test key for authentication
     * if user agrees or fails while showing the failure cause.
     */
    public static class UnknownKeyHandler<T> {
        private final UnknownKeyException e;
        private final CardExecutable<T> failed;

        UnknownKeyHandler(CardExecutable<T> failedTask, UnknownKeyException e) {
            this.e = e;
            this.failed = failedTask;
        }

        T handle() throws LocalizedCardException, UnknownKeyException, CardNotAuthenticatedException {
            CardManager manager = CardManagerFactory.getManager();

            if (useDefaultTestKey() == JOptionPane.YES_OPTION) {
                manager.setTryGenericTestKey();
                manager.setReloadCard();
                return failed.execute();
            } else {
                logger.warn(e.getMessage());
                InformerFactory.getInformer().showFullScreenInfo(new ErrorPane(textSrc.getString("E_authentication"),
                        textSrc.getString("E_master_key_not_found"), "lock.png"));
                return null;
            }
        }

        public static int useDefaultTestKey() {
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
