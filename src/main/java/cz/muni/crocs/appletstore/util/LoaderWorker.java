package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.LocalizedException;
import cz.muni.crocs.appletstore.action.CardDetectionAction;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.card.UnknownKeyException;
import cz.muni.crocs.appletstore.ui.HtmlText;
import jnasmartcardio.Smartcardio;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

/**
 * SwingWorker for app setup
 *
 * @author Jiří Horák
 * @version 1.0
 */
public abstract class LoaderWorker extends SwingWorker<Exception, Void> implements ProcessTrackable {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private static final Logger logger = LogManager.getLogger(LoaderWorker.class);

    private String info = textSrc.getString("loading_opts");

    @Override
    public Exception doInBackground() {
        setProgress(0);
        //first get options will force to initialize
        final CardManager manager = CardManagerFactory.getManager();

        try {
            OptionsFactory.getOptions();



            info = textSrc.getString("detect_cards");
            manager.needsCardRefresh();
            manager.loadCard();
            update("launch", 100, getMaximum());
            return null;
        } catch (UnknownKeyException e) {
            info = textSrc.getString("E_unknown_key");
            if (useDefaultTestKey() == JOptionPane.YES_OPTION) {
                try {
                    manager.setTryGenericTestKey();
                    manager.loadCard();
                    update("launch", 100, getMaximum());
                    return null;
                } catch (LocalizedCardException ex) {
                    update("failed_detect", 200, getMaximum());
                    return ex;
                } catch (UnknownKeyException ex) {
                    update("failed_detect", 200, getMaximum());
                    return new LocalizedCardException("WARNING: Card loading failed, should've not happened!",
                            "E_master_key_not_found");
                }
            } else {
                update("E_unknown_key", 200, getMaximum());
                return new LocalizedCardException("Card auth failed: user refused to use default test key.",
                        "E_master_key_not_found");
            }
        } catch (LocalizedException e) {
            update("failed_detect", 200, getMaximum());
            return e;
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Store initialization failed: generic error.", e);
            update("load_failed", 1000, getMaximum());
            return null;
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

    private int useDefaultTestKey() {
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

