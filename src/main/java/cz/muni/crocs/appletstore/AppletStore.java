package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.ui.GlassPaneBlocker;
import cz.muni.crocs.appletstore.util.Sources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */

public class AppletStore extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(AppletStore.class);

    private boolean windowOpened = true;
    private GlassPaneBlocker blocker = new GlassPaneBlocker();
    public static final int PREFFERED_WIDTH = 1100;
    public static final int PREFFERED_HEIGHT = 550;

    private MainPanel window;
    private Menu menu;

    public AppletStore() {
        logger.info("------- App started");
        setup();
        initComponents();

        //save options on close & kill routine
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new OptionsManager(Sources.options).save();
                windowOpened = false;
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            setGlassPane(null);
        } else {
            System.out.println("glass");
            setGlassPane(blocker);
            blocker.setVisible(true);
        }
    }

    public MainPanel getWindow() {
        return window;
    }

    private void setup() {
        HTMLEditorKit kit = new HTMLEditorKit();
        kit.setStyleSheet(Sources.sheet);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //ignore
        }
        //setDefaultLookAndFeelDecorated(false);
        UIManager.put("MenuItem.selectionBackground", Color.WHITE);
        UIManager.put("Menu.background", Color.BLACK);
        UIManager.put("Menu.foreground", Color.WHITE);
        UIManager.put("Menu.selectionBackground", Color.WHITE);
        UIManager.put("Menu.selectionForeground", Color.BLACK);
        UIManager.put("MenuBar.borderColor", Color.BLACK);
    }


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
        setJMenuBar(menu);

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
        CardManager manager = Sources.manager;

        new Thread(() -> {
            while (windowOpened) {
                try {
                    int result = manager.needsCardRefresh();

                    if (result > 0) {
                        if (result == 2) {
                            SwingUtilities.invokeLater(() -> {
                                window.getLocalPanel().updatePanes(Terminals.TerminalState.LOADING);
                            });
                            manager.refreshCard();
                        }

                        SwingUtilities.invokeLater(() -> {
                            if (result == 2)
                                window.getLocalPanel().updatePanes(manager.getTerminalState());
                            menu.resetTerminalButtonGroup();
                        });
                    }

                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("Terminal routine interrupted, should not happened.", e);
                    checkTerminalsRoutine();
                }
            }
        }).start();
    }
}
