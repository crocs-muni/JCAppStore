package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomScrollBarUI extends BasicScrollBarUI {

    private short opacity = 0;

    public CustomScrollBarUI() {
        minimumThumbSize = new Dimension(10, 10);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        JScrollBar scrollBar = (JScrollBar) c;
        if (!scrollBar.isEnabled() || r.width > r.height) {
            return;
        } else if (isDragging) {
            g2.setPaint(new Color(120, 120, 120, alpha(true)));

        } else if (isThumbRollover() || (r.y > 0)) {
            g2.setPaint(new Color(255, 255, 255, alpha(true)));

        } else {
            g2.setPaint(new Color(60, 60, 60, alpha(false)));
        }
        g2.fillRect(r.x, r.y + 5, r.width - 5, r.height - 10);
        g2.dispose();
    }

    private int alpha(boolean increase) {
        if ((increase && opacity == 250) || (! increase && opacity == 0)) return opacity;
        opacity += (increase) ? 25 : -25;
        return opacity;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        //do not show
        return new JButton() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension();
            }
        };
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        //do not show
        return new JButton() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension();
            }
        };
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    }


    @Override
    protected void setThumbBounds(int x, int y, int width, int height) {
        super.setThumbBounds(x, y, width, height);
        scrollbar.repaint();
    }
}
