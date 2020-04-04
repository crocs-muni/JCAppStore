package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.util.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseEvent;

public class FreeMemoryAction extends CardAbstractAction {

    private static final Logger logger = LoggerFactory.getLogger(CardAbstractAction.class);
    private OnEventCallBack<Void, byte[]> customCall;

    public FreeMemoryAction(OnEventCallBack<Void, byte[]> call) {
        super(new OnEventCallBack<Void, Void>() {
            @Override
            public void onStart() {
                call.onStart(); //delegated to the wrapper
            }

            @Override
            public void onFail() {
                call.onFail(); //delegated to the wrapper
            }

            @Override
            public Void onFinish() {
                return null; //handled by call
            }

            @Override
            public Void onFinish(Void aVoid) {
                return null; //handled by call
            }
        });
        customCall = call;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        execute(() -> customCall.onFinish(JCMemory.getSystemInfo()), "JCMemory.getSystemInfo() failed",
                textSrc.getString("E_could_not_get_memory"), 10000);
    }
}
