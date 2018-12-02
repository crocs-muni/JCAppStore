package cz.muni.crocs.appletstore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.muni.crocs.appletstore.ui.CustomFont;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */

public class AppletStore extends JFrame {

    private static final Logger logger = LogManager.getLogger(AppletStore.class);
    public Terminals terminals = new Terminals(""); //TODO terminal reader?
    //main menu
    public Menu menu;
    //main window under menu
    TabbedPaneSimulator window;

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

    private void setup() {
        CustomFont.refresh(); //load font
        try {
            //necessarry to call as a first thing, since other thing depend on options
            Config.getFileOptions();
        } catch (IOException e) {
            //set defaults, do not block the app
            Config.options.put("lang", "eng");
            Config.options.put("bg", "bg.jpeg");
            e.printStackTrace();
        }
        //look for terminals
        terminals.update();
    }

    private void setUI() {
        setDefaultLookAndFeelDecorated(false);
        //setUndecorated(true);
        UIManager.put("MenuItem.selectionBackground", Color.WHITE);
        UIManager.put("Menu.background", Color.BLACK);
        UIManager.put("Menu.foreground", Color.WHITE);
//        UIManager.put("Menu.disabledBackground", Color.BLACK);
//        UIManager.put("Menu.disabledForeground", Color.WHITE);
        UIManager.put("Menu.selectionBackground", Color.WHITE);
        UIManager.put("Menu.selectionForeground", Color.BLACK);
    }

    private void initComponents() {
        pack();
        // make the frame half the height and width
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height;
        int width = screenSize.width;
        setSize((int) (width / 1.5), (int) (height / 1.5));
        //get main container
        //JPanel mainContainer = new JPanel();

        window = new TabbedPaneSimulator(this);
        setContentPane(window);

        //add the menu
        menu = new Menu(this);
        setJMenuBar(menu);
        // set default window properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }


    //search for present terminals and card
    public void refresh(boolean refreshEvenIfReadersFound) {
        //refresh only if bool true || terminals not found
        if (refreshEvenIfReadersFound || !terminals.isFound()) {
            terminals.update();
            window.localPanel.init();
            menu.resetTerminalButtonGroup();
        }
    }

    public void redraw() {
        this.getContentPane().repaint();
    }


    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new AppletStore();
                } catch (Exception e) {
                    new FeedbackFatalError("Fatal Error", e.getMessage(), e.getMessage(), true,
                            JOptionPane.QUESTION_MESSAGE, null);

                    logger.error("Fatal Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
