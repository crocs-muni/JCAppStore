package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomJmenu extends JMenu {


    private Color defaultColor = Color.BLACK;
    private Color notifyColor = new Color(212, 164, 86);
    private boolean notify = false;

    private static final int DELAY = 250;
    private Timer timer = new Timer(DELAY, e -> {
        repaint();
    });

    public CustomJmenu(String title, String description, int mnemonic) {
        super("<html><p style='margin: 3 8'>" + title + "</p></html>");
        defaultSettings(description, mnemonic);
    }

    public CustomJmenu(AbstractAction action, String description, int mnemonic) {
        super(action);
        defaultSettings(description, mnemonic);
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
        if (this.notify) {
            timer.start();
        } else {
            notifyColor = new Color(212, 164, 86);
            timer.stop();
        }
        revalidate();
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
    protected void paintBorder(Graphics g) {
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (notify) {
            g.setColor(notifyColor);
            notifyColor = darken(notifyColor);
            if (notifyColor == Color.BLACK) {
                notifyColor = new Color(212, 164, 86);
                timer.stop();
                notify = false;
            }
        } else {
            g.setColor(defaultColor);
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public void setContentAreaFilled(boolean b) {
    }

    private static int lower(int a) {
        return Math.max(0, a - 2);
    }

    private static Color darken(Color c) {
        return new Color(lower(c.getRed()), lower(c.getGreen()), lower(c.getBlue()));
    }

    private void defaultSettings(String description, int mnemonic) {
        setMnemonic(mnemonic);
        getAccessibleContext().setAccessibleDescription(description);
        setOpaque(false);
        setFocusPainted(false);
        setFont(OptionsFactory.getOptions().getFont(12f));
        setForeground(Color.WHITE);
        setMargin(new Insets(0, 0, 0, 0));
    }
}
