package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;

import java.awt.*;

/**
 * Idea from http://forums.devshed.com/java-help-9/vertical-flowlayout-593580.html
 * the problem of scrolling is that flow layout always allows extending to the right
 * inside scroll pane and thus the vertical scroll never occurs
 *
 * @author Babu Kalakrishnan
 * modyfied by Jiří Horák (the height computation was a bit off)
 * @version 1.0
 */
public class CustomFlowLayout extends FlowLayout {

    public CustomFlowLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        //both minimum
        return getMinSize(target);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        //both minimum
        return getMinSize(target);
    }

    private Dimension getMinSize(Container target) {
        //prevent from thread collision
        synchronized (target.getTreeLock()) {
            int hgap = getHgap();
            int vgap = getVgap();
            int width = target.getWidth();
            if (width == 0) width = Integer.MAX_VALUE;

            Insets insets = target.getInsets();
            if (insets == null) insets = new Insets(0, 0, 0, 0);

            int reqdWidth = 0;
            int maxwidth = width - (insets.left + insets.right + hgap * 2);

            int x = 0;
            int y = insets.top;
            int rowHeight = 0;

            for (Component c : target.getComponents()) {
                if (c.isVisible()) {
                    Dimension minimumSize = c.getMinimumSize();
                    if ((x == 0) || ((x + minimumSize.width) <= maxwidth)) {
                        if (x > 0) {
                            x += hgap;
                        }
                        x += minimumSize.width;
                        rowHeight = Math.max(rowHeight, minimumSize.height);
                    } else {
                        x = minimumSize.width;
                        y += vgap + rowHeight;
                        rowHeight = minimumSize.height;
                    }
                    reqdWidth = Math.max(reqdWidth, x);
                }
            }
            y += rowHeight;
            return new Dimension(
                    reqdWidth + insets.left + insets.right,
                    y + insets.top + insets.bottom);
        }
    }
}
