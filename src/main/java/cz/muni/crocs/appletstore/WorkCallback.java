package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.OnEventCallBack;

public class WorkCallback implements OnEventCallBack<Void, Void, Void> {

    private BackgroundChangeable context;

    public WorkCallback(BackgroundChangeable context) {
        this.context = context;
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
        //todo show user the OK notice
        context.switchEnabled(true);
        return null;
    }
}
