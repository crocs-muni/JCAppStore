package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.ui.CustomButton;
import cz.muni.crocs.appletstore.ui.CustomFont;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LoadingPane extends JPanel {

    private int progress = 0;
    private int toReach = 0;

    public LoadingPane() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.translate(this.getWidth() / 2, this.getHeight() / 2);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.rotate(Math.toRadians(270));

        if (toReach > progress) progress++;
        Shape progressBar = createRingShape(progress, getHeight() / 10);
        graphics2D.setColor(Color.WHITE);
        graphics2D.draw(progressBar);
        graphics2D.fill(progressBar);
        graphics2D.setFont(CustomFont.plain);
        //graphics2D.drawString(progress + "%", 0, 0);
    }

    public boolean update(int value) {
        toReach = value;
        return toReach <= 100;
    }

    private static Shape createRingShape(int progress, int widthHeight) {


        Arc2D.Float outer = new Arc2D.Float(Arc2D.OPEN);
        outer.setFrameFromCenter(new Point(0, 0), new Point(widthHeight, widthHeight));
        outer.setAngleStart(1);
        outer.setAngleExtent(-progress * 3.6);

        Ellipse2D inner = new Ellipse2D.Float();
        inner.setFrameFromCenter(new Point(0, 0), new Point(widthHeight - 4, widthHeight - 4));

        Area area = new Area(outer);
        area.subtract(new Area(inner));
        return area;
    }
}

