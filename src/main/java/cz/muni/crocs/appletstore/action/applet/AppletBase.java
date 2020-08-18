package cz.muni.crocs.appletstore.action.applet;

import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.util.LocalizedException;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ResourceBundle;


public abstract class AppletBase<T> {
    private static final Logger logger = LoggerFactory.getLogger(AppletBase.class);
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    public T performDefault() throws LocalizedException {
        return performAppletOperation(this::executeAppletCommunication);
    }

    public T perform(AppletAction<T> action) throws LocalizedException {
        return performAppletOperation(action);
    }

    public T performDefault(String installFailureMessage) throws LocalizedException {
        return performAppletOperation(this::executeAppletCommunication, installFailureMessage);
    }

    public T perform(AppletAction<T> action, String installFailureMessage)
            throws LocalizedException {
        return performAppletOperation(action, installFailureMessage);
    }

    /**
     * Applet name getter
     * @return applet name
     */
    public abstract String getAppletName();

    /**
     * AppletInfo instance getter
     * @return AppletInfo instance to work with in actions
     */
    public abstract AppletInfo getInfo();

    /**
     * AppletInfo package getter
     * @return AppletInfo package to work with in actions
     */
    public abstract AppletInfo getPackageInfo();

    /**
     * InstallOpts bundle getter
     * @return InstallOpts bundle for installation process
     */
    public abstract InstallOpts getInstallOptions();

    /**
     * Applet AID
     * @return Applet AID
     */
    public abstract String getAID();

    /**
     * Applet package ID
     * @return Applet package ID
     */
    public abstract String getPackageAID();

    /**
     * JCMemory sources
     * @return File with .cap from the store
     */
    public abstract File getSource();

    /**
     * Execute the applet comunication (that could be handled in the test phase unless it is not
     *  one-command APDU task).
     *
     * @return result parsed as desired type
     */
    protected abstract T executeAppletCommunication(CardManager manager);

    public static byte[] getDataFromResponseAPDU(ResponseAPDU responseAPDU) {
        if (responseAPDU != null && responseAPDU.getSW() == 0x9000) {
            return responseAPDU.getBytes();
        }
        return null;
    }

    /**
     * Tries to USE applet and install it in case it fails. Uninstalls applets unless keepJCMemory is enabled.
     * @param operation operation to perform
     * @throws LocalizedCardException the command could not be performed
     * @throws UnknownKeyException if the key to the card is unknown
     */
    protected T performAppletOperation(AppletAction<T> operation) throws LocalizedException {
        return performAppletOperation(operation, null);
    }

    /**
     * Tries to USE applet and install it in case it fails. Uninstalls applets unless keepJCMemory is enabled.
     * @param operation operation to perform
     * @param installFailureMessage message for the user when installation fails
     * @throws LocalizedCardException the command could not be performed
     * @throws UnknownKeyException if the key to the card is unknown
     */
    protected T performAppletOperation(AppletAction<T> operation, String installFailureMessage)
            throws LocalizedException {
        final CardManager manager = CardManagerFactory.getManager();
        T result;

        boolean selected;
        try {
            selected = manager.select(getAID());
        } catch (LocalizedCardException e) {
            selected = false;
            logger.info("Error detecting " + getAppletName() + "...", e);
        }

        if (selected) {
            result = operation.perform(manager);
            uninstallIfNotKeep(manager, false);
            return result;
        } else {
            try {
                logger.info("Installing " + getAppletName() + "...");
                manager.install(getSource(), getInstallOptions());
            } catch (Exception e) { //we want to catch any exception because we want to report this the same way
                if (installFailureMessage == null || installFailureMessage.isEmpty()) {
                    throw new LocalizedCardException("Failed to install " + getAppletName(), "generic_install_failure", e);
                }
                LocalizedCardException ex = new LocalizedCardException("Failed to install " + getAppletName(), e);
                String cause = e.getLocalizedMessage();
                String verbose = OptionsFactory.getOptions().is(Options.KEY_VERBOSE_MODE) && cause != null && !cause.isEmpty() ?
                        "<br>" + textSrc.getString("cause") + "<br>" + cause : "";

                ex.setTranslation(installFailureMessage + verbose);
                throw ex;
            }
        }

        result = operation.perform(manager);
        uninstallIfNotKeep(manager, true);
        return result;
    }

    private void uninstallIfNotKeep(CardManager manager, boolean refresh)
            throws LocalizedException {
        try {
            if (!OptionsFactory.getOptions().is(Options.KEY_KEEP_JCMEMORY)) {
                logger.info(getAppletName() + " removed because of the mode disabled.");
                manager.uninstall(getPackageInfo(), true);
            } else if (refresh) { //refresh is implicit when uninstalling
                manager.setReloadCard();
                manager.loadCard();
            }
        } catch (UnknownKeyException e) {
            throw LocalizedException.from(e);
        }
    }

    /**
     * Executable interface for applet action
     * @param <T>
     */
    @FunctionalInterface
    public interface AppletAction<T> {
        T perform(CardManager manager) throws LocalizedException;
    }
}
