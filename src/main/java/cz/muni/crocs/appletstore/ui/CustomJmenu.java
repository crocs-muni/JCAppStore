package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomJmenu extends JMenu {

    public CustomJmenu(String title, String description, int mnemonic) {
        super("<html><p style='margin: 3 8'>" + title + "</p></html>");
        defaultSettings(description, mnemonic);
    }

    public CustomJmenu(AbstractAction action, String description, int mnemonic) {
        super(action);
        defaultSettings(description, mnemonic);
    }

    private void defaultSettings(String description, int mnemonic) {
        setMnemonic(mnemonic);
        getAccessibleContext().setAccessibleDescription(description);
        setOpaque(false);
        setFocusPainted(false);
        setFont(OptionsFactory.getOptions().getFont(12f));
        setForeground(Color.WHITE);
        setMargin(new Insets(0,0 ,0 ,0 ));
    }

    //remove popumenu border
    @Override
    public JPopupMenu getPopupMenu() {
        JPopupMenu menu = super.getPopupMenu();
        menu.setBorder(null);
        return menu;
    }

    //deletes button border
    @Override
    protected void paintBorder(Graphics g){
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

//    @Override
//    public void paint(Graphics g) {
//        super.paint(g);
//
//        g.setColor(Color.BLACK);
//        g.fillRect(0, 0, getWidth(), getHeight());
//        paintComponent(g);
//    }

    @Override
    public void setContentAreaFilled(boolean b) {
    }
}
