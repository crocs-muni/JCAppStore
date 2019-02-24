package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.Terminals;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.muni.crocs.appletstore.ui.CustomFont;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import java.awt.Toolkit;
import java.awt.Dimension;
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

    private static final Logger logger = LogManager.getLogger(AppletStore.class);

    private TabbedPaneSimulator window;
    private Menu menu;

    private AppletStore() {
        setup();
        setUI();
        initComponents();

        //save options on close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    Config.saveOptions();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    //TODO handle
                }
            }
        });
    }

    public TabbedPaneSimulator getWindow() {
        return window;
    }

    private void setup() {
        CustomFont.refresh(); //load font
        try {
            Config.getFileOptions();
        } catch (IOException e) {
            //set defaults, do not block the app
            Config.options.put("lang", "en");
            Config.options.put("bg", "bg.jpeg");
            e.printStackTrace();
        }
    }

    private void setUI() {
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
            e.printStackTrace();
            //Ignore
        }
        // make the frame half the height and width
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height;
        int width = screenSize.width;
        setSize((int) (width / 1.5), (int) (height / 1.5));
        window = new TabbedPaneSimulator(this);
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
        CardManager manager = CardManager.getInstance();

        new Thread(() -> {
            while (true) {
                // 1st task - refresh card readers and cards
                final Terminals.TerminalState oldState = manager.getTerminalState();
                int result = manager.refresh(window.localPanel);
                if (result > 0) {
                    final Terminals.TerminalState state = manager.getTerminalState();

                    if (oldState != state) {
                        SwingUtilities.invokeLater(() -> {
                            if (result == 2) window.localPanel.updatePanes(state);
                            menu.resetTerminalButtonGroup();
                        });
                    }
                }


                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error("Detection routine failed: " + e.getMessage());
                    //todo continue?
                    checkTerminalsRoutine();
                    break;
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new AppletStore();
                } catch (Exception e) {
                    e.printStackTrace();
                    new FeedbackFatalError("Fatal Error", e.getMessage(), e.getMessage(), true,
                            JOptionPane.QUESTION_MESSAGE, null);
                    //todo show error
                    logger.error("Fatal Error: " + e.getMessage());
                }
            }
        });
    }
}
