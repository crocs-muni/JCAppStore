package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.OnEventCallBack;

import javax.swing.*;

public class WorkCallback implements OnEventCallBack<Void, Void> {

    private AbstractAction onStart;
    private AbstractAction onFail;
    private AbstractAction onFinish;


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
