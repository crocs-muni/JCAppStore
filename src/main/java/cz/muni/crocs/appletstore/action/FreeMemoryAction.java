package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.util.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseEvent;

public class FreeMemoryAction extends CardAbstractAction {

    private static final Logger logger = LoggerFactory.getLogger(CardAbstractAction.class);
    private OnEventCallBack<Void, byte[]> customCall;

    public FreeMemoryAction(OnEventCallBack<Void, byte[]> call) {
        super(null);
        customCall = call;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        customCall.onStart();
        execute(() -> customCall.onFinish(JCMemory.getSystemInfo()),
                "JCMemory.getSystemInfo() failed", textSrc.getString("E_could_not_get_memory"));
    }
}
