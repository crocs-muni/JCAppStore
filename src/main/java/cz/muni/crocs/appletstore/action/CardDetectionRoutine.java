package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.AppletStore;
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

    private final AppletStore main;
    private int counter = 0;

    public CardDetectionRoutine(AppletStore main, OnEventCallBack<Void, Void> call) {
        super(call, DELAY, TimeUnit.SECONDS);
        this.main = main;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        final CardManager manager = CardManagerFactory.getManager();
        logger.info("------- Routine started -------");
        execute(() -> {
                    try {
                        int result = manager.needsCardRefresh();

                        if (manager.getTerminalState() == Terminals.TerminalState.NO_SERVICE) {
                            //todo debug

                            SwingUtilities.invokeLater(() -> InformerFactory.getInformer().showInfo(
                                    textSrc.getString("H_service"), Notice.Importance.FATAL,
                                    Notice.CallBackIcon.RETRY, () -> {
                                        new CardDetectionRoutine(main, OnEventCallBack.empty()).start();
                                        return null;
                                    }));
                            logger.info("[ROUTINE] Card routine detection killed: Smart card service manager offline.");
                            main.getWindow().getRefreshablePane().refresh();
                            breakExecution();
                        }

                        if (result > 0) {
                            if (result == 2) {
                                try {
                                    SwingUtilities.invokeLater(() -> main.switchEnabled(false));
                                    manager.loadCard();
                                } catch (LocalizedCardException ex) {
                                    ex.printStackTrace();
                                    logger.warn("Failed to load card", ex);
                                    main.getWindow().getRefreshablePane().showError("E_loading_failed",
                                            "CARD: " + manager.getLastCardDescriptor() + "<br>",
                                            ex.getImageName(), ex);
                                    return null;
                                } finally {
                                    SwingUtilities.invokeLater(() -> main.switchEnabled(true));
                                }
                            }

                            SwingUtilities.invokeLater(() -> {
                                if (result == 2) {
                                    main.getWindow().getRefreshablePane().refresh();
                                    main.getMenu().setCard(manager.getCard());
                                }
                                main.getMenu().resetTerminalButtonGroup();
                            });
                        }
                        counter = 0;
                    } catch (UnknownKeyException ex) {
                        handleUnknownKey(CardExecutableIdle.get(),
                                "ERROR: failed to authenticate card. The routine should be error prone!",
                                "lock.png", textSrc.getString("E_routine"), ex);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        counter++;
                        main.getWindow().getRefreshablePane().refresh();
                        if (counter > 10) {
                            logger.info("[ROUTINE] Terminal routine killed after 10 failures.", ex);
                            SwingUtilities.invokeLater(() -> InformerFactory.getInformer().showInfoToClose(
                                    textSrc.getString("H_routine"), Notice.Importance.FATAL, 20000));
                            breakExecution();
                        } else {
                            logger.info("[ROUTINE] Terminal routine caught an error: " + ex.getMessage() +
                                    ". The routine continues for: " + counter, e);
                        }
                    }
            return null;
        }, "ERROR: The routine failure should not occurred. The routine should be error prone!",
                textSrc.getString("E_routine"));
    }
}
