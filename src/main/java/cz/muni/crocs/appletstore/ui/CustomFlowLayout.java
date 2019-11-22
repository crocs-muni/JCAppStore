package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Idea from https://stackoverflow.com/questions/3679886/how-can-i-let-jtoolbars-wrap-to-the-next-line-flowlayout-without-them-being-hi/4611117
 * the problem of scrolling is that flow layout always allows extending to the right
 * inside scroll pane and thus the vertical scroll never occurs
 *
 * @author Babu Kalakrishnan
 * modyfied by Jiří Horák (the height computation was a bit off, added custom functionality CENTER TITLE)
 * @version 1.1
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

    @Override
    public void layoutContainer(Container parent) {
        //copied from original FlowLayout and extended
        synchronized (parent.getTreeLock()) {
            int num = parent.getComponentCount();

            Component[] comps = parent.getComponents();

            Dimension d = parent.getSize();
            Insets ins = parent.getInsets();

            ComponentOrientation orient = parent.getComponentOrientation();
            boolean left_to_right = orient.isLeftToRight();

            int y = ins.top + getVgap();
            int i = 0;
            while (i < num) {
                // Find the components which go in the current row.
                int new_w = ins.left + getHgap() + ins.right;
                int new_h = 0;
                int j;
                boolean found_one = false;

                for (j = i; j < num; ++j) {
                    // Skip invisible items.
                    if (!comps[j].isVisible())
                        continue;

                    if (comps[j].getAlignmentX() == Component.CENTER_ALIGNMENT) {
                        //we found center alignment break and
                        // fill the row either with (compsInRow==0) or without this component
                        j++; //add j so this component is actually considered (below k ranges from i to j-1)
                        break;
                    }

                    Dimension c = comps[j].getPreferredSize();

                    int next_w = new_w + getHgap() + c.width;
                    if (next_w <= d.width || !found_one) {
                        new_w = next_w;
                        new_h = Math.max(new_h, c.height);
                        found_one = true;
                    } else {
                        // Must start a new row, and we already found an item
                        break;
                    }
                }

                // Set the location of each component for this row.
                int x;

                int align = getAlignment();
                int myalign = align;
                if (align == LEADING)
                    myalign = left_to_right ? LEFT : RIGHT;
                else if (align == TRAILING)
                    myalign = left_to_right ? RIGHT : LEFT;

                if (myalign == RIGHT)
                    x = ins.left + (d.width - new_w) + getHgap();
                else if (myalign == CENTER)
                    x = ins.left + (d.width - new_w) / 2 + getHgap();
                else // LEFT and all other values of align.
                    x = ins.left + getHgap();

                for (int k = i; k < j; ++k) {
                    if (comps[k].isVisible()) {
                        Dimension c = comps[k].getPreferredSize();

                        //if last component is CENTERED
                        if (k + 1 == j && comps[k].getAlignmentX() == Component.CENTER_ALIGNMENT) {
                            //if only one component in row is CENTERED
                            if (i != k) {
                                y += new_h + getVgap();
                            }
                            comps[k].setBounds(ins.left, y, c.width, c.height);
                            new_h = c.height;
                            break;
                        }
                        comps[k].setBounds(x, y + (new_h - c.height) / 2, c.width, c.height);
                        x += c.width + getHgap();
                    }
                }
                i = j;
                y += new_h + getVgap();
            }
        }
    }

    private Dimension getMinSize(Container target) {
        synchronized (target.getTreeLock()) {
            int hgap = getHgap();
            int vgap = getVgap();
            int width = target.getWidth();
            if (width == 0) width = Integer.MAX_VALUE;

            Insets insets = target.getInsets();
            if (insets == null) insets = new Insets(0, 0, 0, 0);

            int reqdWidth = 0;
            int maxWidth = (width - (insets.left + insets.right + hgap * 2)) / 2;

            int x = 0;
            int y = insets.top;
            int rowHeight = 0;

            boolean newLine = true;
            for (Component c : target.getComponents()) {
                if (c.isVisible()) {
                    Dimension minimumSize = c.getPreferredSize();
                    if (c.getAlignmentX() == Component.CENTER_ALIGNMENT) {  //center component reserve all row
                        x = maxWidth;
                        if (!newLine) {
                            y += vgap + rowHeight;
                        }
                        y += vgap + minimumSize.height;
                        rowHeight = 0;
                    } else if ((x == 0) || ((x + minimumSize.width) <= maxWidth)) { //inside row
                        if (x > 0) {
                            x += hgap;
                        }
                        x += minimumSize.width;
                        rowHeight = Math.max(rowHeight, minimumSize.height);
                        newLine = false;
                    } else { //begin new row
                        x = minimumSize.width;
                        y += vgap + rowHeight;
                        rowHeight = minimumSize.height;
                        newLine = true;
                    }
                    reqdWidth = Math.max(reqdWidth, x);
                }
            }
            y += rowHeight;
            return new Dimension(reqdWidth + insets.left + insets.right, y + insets.top + insets.bottom);
        }
    }
}
