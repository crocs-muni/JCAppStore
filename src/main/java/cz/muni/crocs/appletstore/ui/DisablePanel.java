package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

/**
 * Panel intended to be able to look disabled
 * @author Jiří Horák
 * @version 1.0
 */
public class DisablePanel extends JPanel {

    protected boolean disabled = false;
    private CircleAnimation animation = new CircleAnimation();

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (disabled) {
            Graphics2D g2d = (Graphics2D)g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            animation.paint(g2d, getWidth(), getHeight());
        }
    }
}
