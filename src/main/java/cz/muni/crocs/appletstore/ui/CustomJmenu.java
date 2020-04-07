package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Custom JMenu style
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomJmenu extends JMenu {

    public CustomJmenu(String title) {
        super("<html><p style='margin: 3 8'>" + title + "</p></html>");
        uiSettings();
    }

    public CustomJmenu(AbstractAction action) {
        super(action);
        uiSettings();
    }

    public CustomJmenu(String title, String description, int mnemonic) {
        super("<html><p style='margin: 3 8'>" + title + "</p></html>");
        uiSettings();
        defaultSettings(description, mnemonic);
    }

    public CustomJmenu(AbstractAction action, String description, int mnemonic) {
        super(action);
        uiSettings();
        defaultSettings(description, mnemonic);
    }

    //remove popumenu border
    @Override
    public JPopupMenu getPopupMenu() {
        JPopupMenu menu = super.getPopupMenu();
        menu.setBorder(null);
        return menu;
    }

    //deletes border
    @Override
    protected void paintBorder(Graphics g) {
    }

    @Override
    public void setContentAreaFilled(boolean b) {
    }

    private void uiSettings() {
        setOpaque(false); //todo uncomment for mac
        setFocusPainted(false);
        setFont(OptionsFactory.getOptions().getFont(12f));
        setForeground(Color.WHITE);
    }

    private void defaultSettings(String description, int mnemonic) {
        setMnemonic(mnemonic);
        getAccessibleContext().setAccessibleDescription(description);
    }
}
