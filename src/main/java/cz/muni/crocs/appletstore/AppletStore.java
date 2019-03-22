package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.Terminals;

import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.GlassPaneBlocker;
import cz.muni.crocs.appletstore.util.Sources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jiří Horák
 * @version 1.0
 */

public class AppletStore extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(AppletStore.class);

    private GlassPaneBlocker blocker = new GlassPaneBlocker();
    public static final int PREFFERED_WIDTH = 1100;
    public static final int PREFFERED_HEIGHT = 550;

    //executor
    //translator
    //card manager
    //window
    //menu
    private MainPanel window;
    private Menu menu;

    public AppletStore() {
        logger.info("------- App started");

        setup();
        initComponents();

        //save options on close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new OptionsManager(Sources.options).save();
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
            //Ignore
            e.printStackTrace();
        }

        setSize(PREFFERED_WIDTH, PREFFERED_HEIGHT);
        window = new MainPanel(this);
        setContentPane(window);
        //add the menu
        menu = new Menu(this);
        setJMenuBar(menu);
        //start routine
        checkTerminalsRoutine();
        Config.setWindow(this);
        // set default window properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //search for present terminals and card
    //called from the tabbedpanesimulator, it needs the panes already loaded
    private void checkTerminalsRoutine() {
        CardManager manager = Sources.manager;

        //todo will close on app exit?
        new Thread(() -> {
            while (true) {
                //do not fail at any rate
                try {
                    int result = manager.needsCardRefresh();

                    if (result > 0) {
                        if (result == 2) {
                            SwingUtilities.invokeLater(() -> {
                                window.localPanel.updatePanes(Terminals.TerminalState.LOADING);
                            });
                            manager.refreshCard();
                        }

                        SwingUtilities.invokeLater(() -> {
                            if (result == 2)
                                window.localPanel.updatePanes(manager.getTerminalState());
                            menu.resetTerminalButtonGroup();
                        });
                    }

                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("Terminal routine interrupted.", e);
                    checkTerminalsRoutine();
                }
            }
        }).start();
    }
}
