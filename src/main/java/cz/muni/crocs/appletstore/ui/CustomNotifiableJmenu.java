package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;


/**
 * JMenu Item that can notify user by temporary change color, used for Readers
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomNotifiableJmenu extends CustomJmenu {

    private Color notifyColor = new Color(177, 129, 96);
    private boolean notify = false;

    private static final int DELAY = 250;
    private Timer timer = new Timer(DELAY, e -> {
        repaint();
    });

    public CustomNotifiableJmenu(String title) {
        super(title);
    }

    public CustomNotifiableJmenu(AbstractAction action) {
        super(action);
    }

    public CustomNotifiableJmenu(String title, String description, int mnemonic) {
        super(title, description, mnemonic);
    }

    public CustomNotifiableJmenu(AbstractAction action, String description, int mnemonic) {
        super(action, description, mnemonic);
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
        if (this.notify && !timer.isRunning()) {
            timer.start();
        } else if (this.notify) {
            notifyColor = new Color(212, 164, 86);
        } else {
            notifyColor = new Color(212, 164, 86);
            timer.stop();
        }
        revalidate();
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
            g.setColor(Color.BLACK);
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    private static int lower(int a) {
        return Math.max(0, a - 2);
    }

    private static Color darken(Color c) {
        return new Color(lower(c.getRed()), lower(c.getGreen()), lower(c.getBlue()));
    }
}
