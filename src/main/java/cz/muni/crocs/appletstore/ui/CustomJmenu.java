package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomJmenu extends JMenu {

    public CustomJmenu(String title, String description, int mnemonic) {
        super("<html><p style='margin:5'>" + title + "</p></html>");
        setMnemonic(mnemonic);
        getAccessibleContext().setAccessibleDescription(description);
        setFont(CustomFont.plain);
        //setForeground(new Color(0xFFFFFF));
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
        if (getModel().isPressed()) {
            g.setColor(Color.WHITE);
            //setForeground(Color.BLACK);
        } else if (getModel().isRollover()) {
            g.setColor(Color.BLACK);
        } else {
            g.setColor(Color.BLACK);
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public void setContentAreaFilled(boolean b) {
    }
}
