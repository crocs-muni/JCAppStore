package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class FreeMemoryAction extends CardAction {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private static final Logger logger = LoggerFactory.getLogger(CardAction.class);
    private OnEventCallBack<Void, Integer> customCall;

    public FreeMemoryAction(OnEventCallBack<Void, Integer> call) {
        super(null);
        customCall = call;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        customCall.onStart();

        SwingWorker<byte[], Void> worker = new SwingWorker<byte[], Void>() {
            private LocalizedCardException e = null;

            @Override
            protected byte[] doInBackground() {
                try {
                    return new JCSystemInfo().getSystemInfo();
                } catch (LocalizedCardException ex) {
                    ex.printStackTrace();
                    e = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                if (e != null) {
                    customCall.onFail();
                    logger.warn("Failed to obtain the free memory space: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> showFailed(textSrc.getString("memory_failed"),
                            OptionsFactory.getOptions().getOption(Options.KEY_ERROR_MODE).equals("verbose") ?
                                    e.getLocalizedMessage() : e.getLocalizedMessageWithoutCause()));
                } else {
                    int availableSpace = -1;
                    try {
                        availableSpace = getAvailableMemory(get());
                    } catch (InterruptedException | ExecutionException ex) {
                        //ignore
                        ex.printStackTrace();
                    }
                    customCall.onFinish(availableSpace);
                }
            }
        };

        worker.execute();
    }

    static int getAvailableMemory() {
        try {
            return getAvailableMemory(new JCSystemInfo().getSystemInfo());
        } catch (LocalizedCardException e) {
            e.printStackTrace();
            return -1;
        }
    }

    static int getAvailableMemory(byte[] response) {
        //supposes big endian
        return (int)response[3] << 0x08 + (int)response[4];
    }
}
