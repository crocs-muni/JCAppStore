package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.GUIFactory;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

/**
 * Routine running on the background, card detection each DELAY ms
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardDetectionRoutine extends CardAbstractRoutine<Void, Void> {

    private static final Logger logger = LoggerFactory.getLogger(CardDetectionRoutine.class);
    private static final int DELAY = 2;

    public CardDetectionRoutine(OnEventCallBack<Void, Void> call) {
        super(call, DELAY, TimeUnit.SECONDS);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        final CardManager manager = CardManagerFactory.getManager();
        logger.info("------- Routine started -------");
        execute(() -> {
//                    try {
                        int result = manager.needsCardRefresh();

                        if (manager.getTerminalState() == Terminals.TerminalState.NO_SERVICE) {
                            //todo debug
                            SwingUtilities.invokeLater(() -> InformerFactory.getInformer().showInfo(
                                    textSrc.getString("H_service"), Notice.Importance.FATAL,
                                    Notice.CallBackIcon.RETRY, () -> {
                                        new CardDetectionRoutine(call).start();
                                        return null;
                                    }));
                            logger.info("[ROUTINE] Card routine detection killed: Smart card service manager offline.");
                            GUIFactory.Components().getStoreWindows().refreshCardPanel();
                            breakExecution();
                        }

                        if (result > 0) {

                            SwingUtilities.invokeLater(call::onStart);
                            if (result == 2) manager.loadCard();

                            SwingUtilities.invokeLater(call::onFinish);
                        }
            return null;
        }, "Error loading a card.", textSrc.getString("E_loading_failed"));
    }
}
