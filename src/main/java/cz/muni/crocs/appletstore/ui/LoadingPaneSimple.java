package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LoadingPaneSimple extends JPanel {

    private final static int radius = 50;
    private int progress = 0;
    private int alpha = 0;

    public LoadingPaneSimple() {
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.translate(this.getWidth() / 2, this.getHeight() / 2);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha / 100));
        graphics2D.setColor(Color.BLACK);
        graphics2D.rotate(Math.toRadians(progress));

        Shape s = createRingShape(20);
        graphics2D.draw(s);
        graphics2D.fill(s);

        if (alpha < 100) alpha += 1;
        progress = (progress + 4) % 360;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 250);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private static Shape createRingShape(int progress) {
        Arc2D.Float outer = new Arc2D.Float(Arc2D.OPEN);
        outer.setFrameFromCenter(new Point(0, 0), new Point(radius, radius));
        outer.setAngleStart(1);
        outer.setAngleExtent(-progress * 3.6);

        Ellipse2D inner = new Ellipse2D.Float();
        inner.setFrameFromCenter(new Point(0, 0), new Point(radius - 4, radius - 4));

        Area area = new Area(outer);
        area.subtract(new Area(inner));
        return area;
    }
}

