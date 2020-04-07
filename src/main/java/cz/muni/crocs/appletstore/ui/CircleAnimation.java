package cz.muni.crocs.appletstore.ui;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;


/**
 * Circle animation for loading
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CircleAnimation  {

    private final static int radius = 50;
    private int progress = 0;
    private int alpha = 0;

    private Dimension prefferedSize = new Dimension(250, 250);

    /**
     * Create circle animation 250x250 px
     */
    public CircleAnimation() {}

    /**
     * Create circle animation
     * @param prefferedSize optional size
     */
    public CircleAnimation(Dimension prefferedSize) {
        this.prefferedSize = prefferedSize;
    }

    /**
     * Paint the circle
     * @param g2d Graphics2D graphic to use for painting
     * @param screenWidth parent screen width
     * @param screenHeight parent screen height
     */
    public void paint(Graphics2D g2d, int screenWidth, int screenHeight) {
        g2d.translate(screenWidth / 2, screenHeight / 2);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha / 100));
        g2d.setColor(Color.BLACK);
        g2d.rotate(Math.toRadians(progress));

        Shape s = createRingShape(20);
        g2d.draw(s);
        g2d.fill(s);

        if (alpha < 100) alpha += 1;
        progress = (progress + 4) % 360;
    }

    public Dimension getPreferredSize() {
        return prefferedSize;
    }

    public Dimension getMinimumSize() {
        return prefferedSize;
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

