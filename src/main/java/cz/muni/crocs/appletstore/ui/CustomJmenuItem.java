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
public class CustomJmenuItem extends JMenuItem {

    public CustomJmenuItem(String title) {
        super("<html><p style='padding: 3px 8px'>" + title + "</p></html>");
        uiSettings();
    }

    public CustomJmenuItem(AbstractAction action) {
        super(action);
        uiSettings();
    }

    public CustomJmenuItem(String title, String description, int mnemonic) {
        this(title);
        defaultSettings(description, mnemonic);
    }

    public CustomJmenuItem(AbstractAction action, String description, int mnemonic) {
        super(action);
        uiSettings();
        defaultSettings(description, mnemonic);
    }

    //deletes border
    @Override
    protected void paintBorder(Graphics g) {
    }

    @Override
    public void setContentAreaFilled(boolean b) {
    }

    private void uiSettings() {
        setOpaque(true);
        setFocusPainted(false);
    }

    private void defaultSettings(String description, int mnemonic) {
        setMnemonic(mnemonic);
        getAccessibleContext().setAccessibleDescription(description);
    }
}
