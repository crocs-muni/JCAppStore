package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.LocalizedCardException;

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

    private String info = textSrc.getString("loading_opts");

    @Override
    public Void doInBackground() {
        setProgress(0);
        //first get options will force to initialize
        OptionsFactory.getOptions();

        info = textSrc.getString("detect_cards");
        CardManager manager = CardManagerFactory.getManager();
        manager.needsCardRefresh();
        try {
            manager.refreshCard();
        } catch (LocalizedCardException e) {
            info = textSrc.getString("failed_detect");
            waitWhile(500);
        }

        info = textSrc.getString("lanuch");
        waitWhile(500);
        setProgress(getMaximum());
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

