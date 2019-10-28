package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.OnEventCallBack;

public class WorkCallback implements OnEventCallBack<Void, Void> {

    private BackgroundChangeable context;
    private Refreshable content;

    public WorkCallback(BackgroundChangeable context, Refreshable content) {
        this.context = context;
        this.content = content;
    }

    @Override
    public void onStart() {
        context.switchEnabled(false);
    }

    @Override
    public void onFail() {
        context.switchEnabled(true);
    }

    @Override
    public Void onFinish() {
        context.switchEnabled(true);
        content.refresh();
        return null;
    }

    @Override
    public Void onFinish(Void aVoid) {
        return null;
    }
}
