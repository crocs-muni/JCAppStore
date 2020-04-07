package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


/**
 * Panel intended to be able to look disabled
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class DisablePanel extends JPanel {

    private CircleAnimation animation = new CircleAnimation();

    /**
     * Enable or disable the panel
     * @param enabled
     */
    public void setEnabledAll(boolean enabled) {
        setEnabled(enabled);
        transferFocus();
        if (enabled)
            setFocusCycleRoot(true);
        else
            setFocusCycleRoot(false);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!isEnabled()) {
            Graphics2D g2d = (Graphics2D)g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            animation.paint(g2d, getWidth(), getHeight());
        }
    }
}
