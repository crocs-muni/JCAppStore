package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.ui.GlassPaneBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * App main window
 *
 * @author Jiří Horák
 * @version 1.0
 */

public class AppletStore extends JFrame implements BackgroundChangeable {
    private static final Logger logger = LoggerFactory.getLogger(AppletStore.class);
    public static final int PREFFERED_WIDTH = 1100;
    public static final int PREFFERED_HEIGHT = 550;

    private boolean windowOpened = true;
    private MainPanel window;
    private Menu menu;
    private GlassPaneBlocker blocker = new GlassPaneBlocker();

    public AppletStore() {
        logger.info("------- App started");

        setup();
        initComponents();

        //save options on close & kill routine
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                OptionsFactory.getOptions().save();
                windowOpened = false;
            }
        });
    }

    /**
     * Environment and style settings
     */
    private void setup() {
        HTMLEditorKit kit = new HTMLEditorKit();
        kit.setStyleSheet(OptionsFactory.getOptions().getDefaultStyleSheet());
        UIManager.put("MenuItem.selectionBackground", Color.WHITE);
        UIManager.put("Menu.background", Color.BLACK);
        UIManager.put("Menu.foreground", Color.WHITE);
        UIManager.put("Menu.selectionBackground", Color.WHITE);
        UIManager.put("Menu.selectionForeground", Color.BLACK);
        UIManager.put("MenuBar.borderColor", Color.BLACK);
    }

    /**
     * Build Swing components and start routine
     */
    private void initComponents() {
        try {
            setIconImage(ImageIO.read(new File(Config.IMAGE_DIR + "icon.png")));
        } catch (IOException e) {
            //ignore
        }
        setSize(PREFFERED_WIDTH, PREFFERED_HEIGHT);
        window = new MainPanel(this);
        setContentPane(window);

        menu = new Menu(this);
        menu.setCard(CardManagerFactory.getManager().getCardDescriptor());
        setJMenuBar(menu);
        setGlassPane(blocker);

        //start routine
        checkTerminalsRoutine();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Looking for terminals present once a 2 sec
     */
    private void checkTerminalsRoutine() {
        CardManager manager = CardManagerFactory.getManager();

        new Thread(() -> {
            logger.info("------- routine started");
            while (windowOpened) {
                try {
                    int result = manager.needsCardRefresh();

                    if (result > 0) {
                        if (result == 2) {
                            try {
                                SwingUtilities.invokeLater(() -> {
                                    switchEnabled(false);
                                });
                                manager.loadCard();
                            } catch (LocalizedCardException e) {
                                e.printStackTrace();
                                window.getRefreshablePane().showError("E_loading_failed",
                                        (OptionsFactory.getOptions().getOption(Options.KEY_ERROR_MODE).equals("verbose") ?
                                                e.getLocalizedMessage() : e.getLocalizedMessageWithoutCause())
                                        + "<br> CARD: " + manager.getLastCardDescriptor(),
                                        "announcement_white.png");
                                continue;
                            } finally {
                                SwingUtilities.invokeLater(() -> {
                                    switchEnabled(true);
                                });
                            }
                        }

                        SwingUtilities.invokeLater(() -> {
                            if (result == 2) {
                                window.getRefreshablePane().refresh();
                                menu.setCard(manager.getCardDescriptor());
                            }

                            menu.resetTerminalButtonGroup();
                        });
                    }
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        InformerFactory.getInformer().showWarningToClose(e.getMessage(), Warning.Importance.SEVERE);
                    });
                    logger.info("Terminal routine interrupted, should not happened.", e);
                    window.getRefreshablePane().refresh();
                    checkTerminalsRoutine();
                }
            }
        }).start();
    }

    @Override
    public void updateBackground(BufferedImage image) {
        ((BackgroundImgPanel) getContentPane()).setNewBackground(image);
    }

    @Override
    public void switchEnabled(boolean enabled) {
        if (enabled == isEnabled())
            return;
        setEnabled(enabled);
        getGlassPane().setVisible(!enabled);
        revalidate();
    }
}
