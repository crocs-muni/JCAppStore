package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.action.CardDetectionAction;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
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
public class LoaderWorker extends SwingWorker<Void, Void> implements ProcessTrackable {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private static final Logger logger = LogManager.getLogger(LoaderWorker.class);

    private String info = textSrc.getString("loading_opts");

    @Override
    public Void doInBackground() {
        //todo maybe return the exception and start the app with it (e.g. do not manager.setReloadCard() but just get the exception so the message can be displayed
        //! now the card is reloaded 2x and the screen flickers
        setProgress(0);
        //first get options will force to initialize
        try {
            OptionsFactory.getOptions();
            CardManager manager = CardManagerFactory.getManager();
            manager.needsCardRefresh();
            new CardDetectionAction(new OnEventCallBack<Void, Void>() {
                @Override
                public void onStart() {
                    info = textSrc.getString("detect_cards");
                }

                @Override
                public void onFail() {
                    info = textSrc.getString("failed_detect");
                    manager.setReloadCard();
                    waitWhile(300);
                    setProgress(getMaximum());
                }

                @Override
                public Void onFinish() {
                    info = textSrc.getString("launch");
                    waitWhile(100);
                    setProgress(getMaximum());
                    return null;
                }

                @Override
                public Void onFinish(Void aVoid) {
                    return onFinish();
                }
            }).start();
        } catch (Exception e) {
            info = textSrc.getString("failed: " + e.getMessage());
            waitWhile(2000);
            setProgress(getMaximum());
        }
        return null;
    }

    @Override
    protected void done() {
        //  do nothing
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
}

