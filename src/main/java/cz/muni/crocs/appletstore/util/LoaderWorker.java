package cz.muni.crocs.appletstore.util;

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
        setProgress(0);
        //first get options will force to initialize
        try {
            OptionsFactory.getOptions();

            info = textSrc.getString("detect_cards");
            CardManager manager = CardManagerFactory.getManager();
            try {
                manager.needsCardRefresh();
                manager.loadCard();
            } catch (LocalizedCardException e) {
                info = textSrc.getString("failed_detect");
                waitWhile(500);
            }
            info = textSrc.getString("launch");
            waitWhile(100);
            setProgress(getMaximum());
        } catch (Exception e) {
            info = textSrc.getString("failed: " + e.getMessage());
            waitWhile(2000);
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

