package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.action.CardDetectionAction;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.iface.ProcessTrackable;


import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

/**
 * SwingWorker for app setup
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LoaderWorker implements ProcessTrackable {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private String info = textSrc.getString("loading_opts");
    private int progress = 0;
    private CallBack<Void> callBack;

    public LoaderWorker(CallBack<Void> afterLoad) {
        this.callBack = afterLoad;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public int getProgress() {
        return progress;
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
        if (ms < 50) return;

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
        safeSetProgress(progress);
    }

    @Override
    public void run() {
        new CardDetectionAction(new OnEventCallBack<Void, Void>() {
            @Override
            public void onStart() {
                OptionsFactory.getOptions(); //forces to load
                update("detect_cards", 0, 4);
            }

            @Override
            public void onFail() {
                update("failed_detect", 200, getMaximum());
                callBack.callBack();
            }

            @Override
            public Void onFinish() {
                update("launch", 100, getMaximum());
                callBack.callBack();
                return null;
            }

            @Override
            public Void onFinish(Void aVoid) {
                return onFinish();
            }
        }).mouseClicked(null);
    }
}

