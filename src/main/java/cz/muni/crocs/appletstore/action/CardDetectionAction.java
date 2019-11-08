package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.AppletStore;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class CardDetectionAction extends CardAbstractAction {

    private static final Logger logger = LoggerFactory.getLogger(CardDetectionAction.class);

    public CardDetectionAction(OnEventCallBack<Void, Void> call) {
        super(call);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        final CardManager manager = CardManagerFactory.getManager();

        execute(() -> {
                    manager.needsCardRefresh();
                    if (manager.getTerminalState() != Terminals.TerminalState.NO_SERVICE) {
                        manager.loadCard();
                    }
                }, "Card detection on app startup failed.",
                null);
    }
}
