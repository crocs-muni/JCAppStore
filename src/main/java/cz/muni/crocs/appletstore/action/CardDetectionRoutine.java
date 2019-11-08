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

public class CardDetectionRoutine extends CardAbstractAction {

    private static final Logger logger = LoggerFactory.getLogger(CardDetectionRoutine.class);
    private static final int DELAY = 2000;

    private AppletStore main;

    public CardDetectionRoutine(AppletStore main, OnEventCallBack<Void, Void> call) {
        super(call);
        this.main = main;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        final CardManager manager = CardManagerFactory.getManager();

        execute(() -> {
            logger.info("------- Routine started -------");
            int counter = 0;
            while (main.isWindowOpened()) {
                try {
                    int result = manager.needsCardRefresh();

                    if (manager.getTerminalState() == Terminals.TerminalState.NO_SERVICE) {
                        SwingUtilities.invokeLater(() -> InformerFactory.getInformer().showInfoToClose(
                                textSrc.getString("H_service"), Notice.Importance.FATAL, 20000));
                        logger.info("[ROUTINE] Card routine detection killed: Smart card service manager offline.");
                        main.getWindow().getRefreshablePane().refresh();
                        break;
                    }

                    if (result > 0) {
                        if (result == 2) {
                            try {
                                SwingUtilities.invokeLater(() -> main.switchEnabled(false));
                                manager.loadCard();
                            } catch (LocalizedCardException ex) {
                                ex.printStackTrace();
                                //todo some cases want to show image of plug out / in -> add to the exception image name option
                                main.getWindow().getRefreshablePane().showError("E_loading_failed",
                                        "CARD: " + manager.getLastCardDescriptor() + "<br>",
                                        "announcement_white.png", ex);
                                continue;
                            } finally {
                                SwingUtilities.invokeLater(() -> main.switchEnabled(true));
                            }
                        }

                        SwingUtilities.invokeLater(() -> {
                            if (result == 2) {
                                main.getWindow().getRefreshablePane().refresh();
                                main.getMenu().setCard(manager.getCardDescriptor());
                            }

                            main.getMenu().resetTerminalButtonGroup();
                        });
                    }
                    counter = 0;
                    Thread.sleep(DELAY);
                } catch (UnknownKeyException ex) {
                    throw ex; //we want to throw only this exception so the unknown key is handled by wrapper
                } catch (Exception ex) {
                    ex.printStackTrace();
                    counter++;
                    main.getWindow().getRefreshablePane().refresh();
                    if (counter > 10) {
                        logger.info("[ROUTINE] Terminal routine killed after 10 failures.", ex);
                        SwingUtilities.invokeLater(() -> InformerFactory.getInformer().showInfoToClose(
                                textSrc.getString("H_routine"), Notice.Importance.FATAL, 20000));
                        break;
                    } else {
                        logger.info("[ROUTINE] Terminal routine caught an error: " + ex.getMessage() +
                                ". The routine continues for: " + counter, e);
                    }
                }
            }
        }, "ERROR: The routine failure should not occurred. The routine should be error prone!",
                textSrc.getString("E_routine"));
    }
}
