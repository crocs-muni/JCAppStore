package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomFont;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */

public class AppletStore extends JFrame {

    public Terminals terminals = new Terminals(""); //TODO terminal reader?
    public Menu menu;
    TabbedPaneSimulator window;

    private AppletStore() {
        setup();
        setUI();
        initComponents();
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

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        setContentPane(pane);
        //add the content window
        window = new TabbedPaneSimulator(this);
        pane.add(window.get(), BorderLayout.CENTER);
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


    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new AppletStore();
                } catch (Exception e) {
                    new FeedbackFatalError("Title", "Message", e.getMessage(), true,
                            JOptionPane.QUESTION_MESSAGE, null);
                    e.printStackTrace();
                }
            }
        });
    }
}
