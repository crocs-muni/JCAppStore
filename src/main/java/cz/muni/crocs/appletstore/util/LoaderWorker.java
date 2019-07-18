package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.iface.ProcessTrackable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LoaderWorker extends SwingWorker<Void, Void> implements ProcessTrackable {

    private static final Logger logger = LogManager.getLogger(LoaderWorker.class);
    private String info = "Loading options...";

    @Override
    public Void doInBackground() {
        setProgress(0);
        //first get options will force to initialize
        OptionsFactory.getOptions();

        info = "Detecting cards...";
        CardManager manager = CardManagerFactory.getManager();
        manager.needsCardRefresh();
        manager.refreshCard();

        info = "Creating window...";
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
}

