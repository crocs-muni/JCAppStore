package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.LocalizedException;
import cz.muni.crocs.appletstore.action.CardAbstractActionBase;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.iface.ProcessTrackable;
import cz.muni.crocs.appletstore.ui.HtmlText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.javacard.gp.GPException;

import javax.swing.*;

import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

/**
 * SwingWorker for app setup
 *
 * @author Jiří Horák
 * @version 1.0
 */
public abstract class LoaderWorker extends SwingWorker<Exception, Void> implements ProcessTrackable {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private static final Logger logger = LogManager.getLogger(LoaderWorker.class);

    private String info = textSrc.getString("loading_opts");

    @Override
    public Exception doInBackground() {
        setProgress(0);
        //first get options will force to initialize
        final CardManager manager = CardManagerFactory.getManager();

        try {
            info = textSrc.getString("detect_cards");
            manager.needsCardRefresh();
            manager.loadCard();
            update("launch", 100, getMaximum());
            return null;
        } catch (UnknownKeyException e) {
            e.printStackTrace();
            logger.warn("Unable to guess/obtain the card key for first time.", e);
            info = textSrc.getString("E_unknown_key");
            if (CardAbstractActionBase.UnknownKeyHandler.useDefaultTestKey() == JOptionPane.YES_OPTION) {
                try {
                    manager.setTryGenericTestKey();
                    manager.loadCard();
                    update("launch", 100, getMaximum());
                    return null;
                } catch (LocalizedCardException ex) {
                    update("failed_detect", 200, getMaximum());
                    ex.setImageName("plug-in-out.jpg");
                    return ex;
                } catch (UnknownKeyException ex) {
                    update("failed_detect", 200, getMaximum());
                    return new LocalizedCardException("WARNING: Card loading failed, should've not happened!",
                            "E_master_key_not_found");
                }
            } else {
                update("E_unknown_key", 200, getMaximum());
                return new LocalizedCardException("Card auth failed: user refused to use default test key.",
                        "E_master_key_not_found", "lock.png");
            }
        } catch (LocalizedException | GPException e) {
            update("failed_detect", 200, getMaximum());
            return e;
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Store initialization failed: generic error.", e);
            update("load_failed", 1000, getMaximum());
            return e;
        }
    }

    @Override
    public void updateProgress(int amount) {
        setProgress(amount);
    }

    @Override
    public int getMaximum() {
        return 16;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public void setLoaderMessage(String msg) {
        info = msg;
    }

    private void waitWhile(long ms) {
        try {
            //wait for user to be able to read the msg
            sleep(ms);
        } catch (InterruptedException ex) {
            //do nothing
        }
    }

    private void update(String key, int delay, int progress) {
        info = textSrc.getString(key);
        waitWhile(delay);
        setProgress(progress);
    }
}

