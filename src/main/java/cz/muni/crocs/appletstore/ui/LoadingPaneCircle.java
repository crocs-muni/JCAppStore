package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;


/**
 * Loading panel with circle
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LoadingPaneCircle extends JPanel {

    private CircleAnimation animation = new CircleAnimation();

    public LoadingPaneCircle() {
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        animation.paint((Graphics2D)g, getWidth(), getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return animation.getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return animation.getMinimumSize();
    }
}

