package cz.muni.crocs.appletstore.action.applet;

import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public abstract class AppletBase<T> {
    private static final Logger logger = LoggerFactory.getLogger(AppletBase.class);

    public T performDefault() throws UnknownKeyException, LocalizedCardException {
        return performCardOperation(this::executeAppletCommunication);
    }

    public T perform(AppletAction<T> action) throws UnknownKeyException, LocalizedCardException {
        return performCardOperation(action);
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
     * @throws LocalizedCardException the command could not be performed
     * @throws UnknownKeyException if the key to the card is unknown
     */
    protected T performCardOperation(AppletAction<T> operation) throws LocalizedCardException, UnknownKeyException {
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
            } catch (IOException e) {
                throw new LocalizedCardException("Failed to install " + getAppletName(), "jcmemory_install_failure", e);
            }
        }

        result = operation.perform(manager);
        uninstallIfNotKeep(manager, true);
        return result;
    }

    private void uninstallIfNotKeep(CardManager manager, boolean refresh)
            throws LocalizedCardException, UnknownKeyException {
        if (!OptionsFactory.getOptions().is(Options.KEY_KEEP_JCMEMORY)) {
            logger.info(getAppletName() + " removed because of the mode disabled.");
            manager.uninstall(getPackageInfo(), true);
        } else if (refresh) {
            manager.loadCard();
        }
    }

    /**
     * Executable interface for applet action
     * @param <T>
     */
    @FunctionalInterface
    public interface AppletAction<T> {
        T perform(CardManager manager) throws LocalizedSignatureException;
    }
}
