package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

/**
 * Detection of any new card
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardDetectionAction extends CardAbstractAction<Void, Void> {
    private static final Logger logger = LoggerFactory.getLogger(CardDetectionAction.class);

    public CardDetectionAction(OnEventCallBack<Void, Void> call) {
        super(call);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        final CardManager manager = CardManagerFactory.getManager();
        logger.info("Detecting card...");
        execute(() -> {
                    manager.needsCardRefresh();
                    if (manager.getTerminalState() != Terminals.TerminalState.NO_SERVICE) {
                        manager.loadCard();
                    } else {
                        logger.info("No service enabled: card refresh not performed.");
                    }
                    return null;
                }, "Card detection on app startup failed.",
                null, 10, TimeUnit.SECONDS);
    }
}
