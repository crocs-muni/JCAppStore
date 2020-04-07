package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.OnEventCallBack;

import javax.swing.*;

/**
 * Callback used whn working with actions
 * this callback does not return any value, it mainly disables and enables user interaction
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class WorkCallback implements OnEventCallBack<Void, Void> {

    private AbstractAction onStart;
    private AbstractAction onFail;
    private AbstractAction onFinish;

    /**
     * Create a callback
     * @param onStart AbstractAction to perform when started
     * @param onFail AbstractAction to perform when failed
     * @param onFinish AbstractAction to perform once finished if succeeded
     */
    public WorkCallback(AbstractAction onStart, AbstractAction onFail, AbstractAction onFinish) {
        this.onStart = onStart;
        this.onFail = onFail;
        this.onFinish = onFinish;
    }

    @Override
    public void onStart() {
        onStart.actionPerformed(null);
    }

    @Override
    public void onFail() {
        onFail.actionPerformed(null);
    }

    @Override
    public Void onFinish() {
        onFinish.actionPerformed(null);
        return null;
    }

    @Override
    public Void onFinish(Void aVoid) {
        return null;
    }
}
