package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.Menu;
import jdk.nashorn.internal.ir.Terminal;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Jiří Horák
 * @version 1.0
 */

public class AppletStore extends JFrame {

    public Terminals terminals = new Terminals("");

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
        TabbedPaneSimulator window = new TabbedPaneSimulator(this);
        pane.add(window.get(), BorderLayout.CENTER);
        //add the menu
        Menu menu = new Menu(this);
        setJMenuBar(menu);
        // set default window properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new AppletStore();
                } catch (Exception e) {
                    //TODO close system exit
                    new Feedback("Title", "Message", e.getMessage(), true,
                            JOptionPane.QUESTION_MESSAGE, null);
                    e.printStackTrace();
                }
            }
        });
    }
}
