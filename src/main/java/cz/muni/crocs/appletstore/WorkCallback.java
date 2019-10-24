package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.OnEventCallBack;

public class WorkCallback implements OnEventCallBack<Void, Void, Void> {

    private BackgroundChangeable context;
    private Refreshable content;

    public WorkCallback(BackgroundChangeable context, Refreshable content) {
        this.context = context;
        this.content = content;
    }

    @Override
    public Void onStart() {
        context.switchEnabled(false);
        return null;
    }

    @Override
    public Void onFail() {
        context.switchEnabled(true);
        return null;
    }

    @Override
    public Void onFinish() {
        context.switchEnabled(true);
        content.refresh();
        return null;
    }
}
