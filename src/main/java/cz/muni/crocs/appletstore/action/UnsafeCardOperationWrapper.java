package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

/**
 * Handle some unsafe operations that were delivered as callback fixes when raised an exception
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class UnsafeCardOperationWrapper extends CardAbstractAction<Void, Void> {
    private static final Logger logger = LoggerFactory.getLogger(UnsafeCardOperationWrapper.class);

    private final UnsafeCardOperation operation;

    public UnsafeCardOperationWrapper(OnEventCallBack<Void, Void> call, UnsafeCardOperation operation) {
        super(call);
        this.operation = operation;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        logger.info("Refreshing card...");
        execute(() -> {
            operation.fire();
            return null;
        }, "Failed to reload card.", textSrc.getString("failed_to_reload"), 10, TimeUnit.SECONDS);
    }
}
