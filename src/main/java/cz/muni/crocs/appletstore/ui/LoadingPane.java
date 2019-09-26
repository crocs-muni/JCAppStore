package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LoadingPane extends JPanel {

    private final int width = 300;
    private final int height = 5;
    private int progress = 0;
    private String message;
    private Rectangle outline = new Rectangle(-(width/2), -(height/2), width, height);

    public LoadingPane(String initialMsg) {
        setOpaque(false);
        this.message = initialMsg;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.translate(this.getWidth() / 2, this.getHeight() / 2);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Color.WHITE);;
        graphics2D.draw(outline);

        Rectangle inline = new Rectangle(-(width/2), -(height/2), width * progress / 100, height);
        graphics2D.draw(inline);
        graphics2D.fill(inline);
        graphics2D.setFont(OptionsFactory.getOptions().getTitleFont(Font.BOLD, 15f));
        if (0 < progress && progress < 100) {
            graphics2D.drawString(message + progress + "%", -(width/2), -(height/2) - 20);
        } else {
            graphics2D.drawString(message,  -(width/2), -(height/2) - 20);
        }
    }

    public boolean update(int value) {
        progress = value;
        return progress <= 100;
    }

//    private static Shape createRingShape(int progress, int widthHeight) {
//
//        Arc2D.Float outer = new Arc2D.Float(Arc2D.OPEN);
//        outer.setFrameFromCenter(new Point(0, 0), new Point(widthHeight, widthHeight));
//        outer.setAngleStart(1);
//        outer.setAngleExtent(-progress * 3.6);
//
//        Ellipse2D inner = new Ellipse2D.Float();
//        inner.setFrameFromCenter(new Point(0, 0), new Point(widthHeight - 4, widthHeight - 4));
//
//        Area area = new Area(outer);
//        area.subtract(new Area(inner));
//        return area;
//    }
}

