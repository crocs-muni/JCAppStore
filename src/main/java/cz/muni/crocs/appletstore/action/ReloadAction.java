package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseEvent;

/**
 * Class to add to button as listener target to perform applet installation
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ReloadAction extends CardAbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(ReloadAction.class);

    public ReloadAction(OnEventCallBack<Void, Void> call) {
        super(call);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        logger.info("Refreshing card...");
        execute(() -> {
            CardManager manager = CardManagerFactory.getManager();
            manager.loadCard();
        }, "Failed to reload card.", textSrc.getString("failed_to_reload"), 10000);
    }
}
