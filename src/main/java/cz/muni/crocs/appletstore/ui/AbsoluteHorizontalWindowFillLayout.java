package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Author Jiří Horák
 */

public class AbsoluteHorizontalWindowFillLayout implements LayoutManager2, Serializable {
    private static final long serialVersionUID = 18082829169631543L;

    private ArrayList<Integer> idxs = new ArrayList<>();

    /**
     * The container to be laid out.
     */
    private Container target;
    private Frame window;

    private int lastHeight;


    private SizeRequirements[] xChildren;
    private SizeRequirements[] yChildren;

    /**
     * Creates a new OverlayLayout for the specified container.
     *
     * @param target the container to be laid out
     */
    public AbsoluteHorizontalWindowFillLayout(Container target, Frame window) {
        this.target = target;
        this.window = window;
        this.lastHeight = window.getHeight();
    }

    public void addAbsolutePositioned(int growY, int x, int y) {
        idxs.add(growY);
        idxs.add(x);
        idxs.add(y);
    }

    public void updateAbsolutePositioned(int idx, int x, int y) {
        idxs.set(3 * idx + 1, x);
        idxs.set(3 * idx + 2, y);
    }

    /**
     * Notifies the layout manager that the layout has become invalid. It throws
     * away cached layout information and recomputes it the next time it is
     * requested.
     *
     * @param target not used here
     */
    public void invalidateLayout(Container target) {
        xChildren = null;
        yChildren = null;
    }

    /**
     * This method is not used in this layout manager.
     *
     * @param string    not used here
     * @param component not used here
     */
    public void addLayoutComponent(String string, Component component) {
        // Nothing to do here.
    }

    /**
     * This method is not used in this layout manager.
     *
     * @param component   not used here
     * @param constraints not used here
     */
    public void addLayoutComponent(Component component, Object constraints) {
        // Nothing to do here.
    }

    /**
     * This method is not used in this layout manager.
     *
     * @param component not used here
     */
    public void removeLayoutComponent(Component component) {
        // Nothing to do here.
    }

    /**
     * Returns the preferred size of the container that is laid out. This is
     * computed by the children's preferred sizes, taking their alignments into
     * account.
     *
     * @param target not used here
     * @return the preferred size of the container that is laid out
     */
    public Dimension preferredLayoutSize(Container target) {
        if (target != this.target)
            throw new AWTError("OverlayLayout can't be shared");
        return window.getSize();
    }

    /**
     * Returns the minimum size of the container that is laid out. This is
     * computed by the children's minimum sizes, taking their alignments into
     * account.
     *
     * @param target not used here
     * @return the minimum size of the container that is laid out
     */
    public Dimension minimumLayoutSize(Container target) {
        if (target != this.target)
            throw new AWTError("OverlayLayout can't be shared");
        return window.getSize();
    }

    /**
     * Returns the maximum size of the container that is laid out. This is
     * computed by the children's maximum sizes, taking their alignments into
     * account.
     *
     * @param target not used here
     * @return the maximum size of the container that is laid out
     */
    public Dimension maximumLayoutSize(Container target) {
        if (target != this.target)
            throw new AWTError("OverlayLayout can't be shared");
        return window.getMaximumSize();
    }

    public float getLayoutAlignmentX(Container target) {
        //absolute positioned
        return 0;
    }

    public float getLayoutAlignmentY(Container target) {
        return 0;
    }

    public void layoutContainer(Container target) {
        if (target != this.target)
            throw new AWTError("OverlayLayout can't be shared");

        checkRequirements();
        checkLayout();
        Component[] children = target.getComponents();
        for (int i = 0; i < children.length; i++)
            children[i].setBounds(idxs.get(3 * i + 1), idxs.get(3 * i + 2), window.getWidth(),
                    idxs.get(3 * i) == 1 ? window.getHeight() : yChildren[i].preferred);
    }

    /**
     * Makes sure that the offsetsX, offsetsY, spansX and spansY fields are set
     * up correctly. A call to {@link #invalidateLayout} sets these fields
     * to null and they have to be recomputed.
     */
    private void checkLayout() {
        int wHeight = window.getHeight();
        if (wHeight <= 0)
            return;

        for (int i = 0; i < idxs.size(); i += 3) {
            int third = i / 3;

            int lastY = idxs.get(i + 2);
            if (third == 1) System.out.println("SETadd: " + (wHeight - lastHeight));
            if (third == 1) System.out.println("LAST: " + (lastHeight) + " NEW: " + wHeight);

            if (idxs.get(i) == 0)
                idxs.set(i + 2, lastY + wHeight - lastHeight);

        }
        lastHeight = wHeight;
    }

    private void checkRequirements() {
        if (xChildren == null || yChildren == null) {
            Component[] children = target.getComponents();
            xChildren = new SizeRequirements[children.length];
            yChildren = new SizeRequirements[children.length];
            for (int i = 0; i < children.length; i++) {
                if (!children[i].isVisible()) {
                    xChildren[i] = new SizeRequirements();
                    yChildren[i] = new SizeRequirements();
                } else {
                    xChildren[i] =
                            new SizeRequirements(children[i].getMinimumSize().width,
                                    children[i].getPreferredSize().width,
                                    children[i].getMaximumSize().width,
                                    children[i].getAlignmentX());
                    yChildren[i] =
                            new SizeRequirements(children[i].getMinimumSize().height,
                                    children[i].getPreferredSize().height,
                                    children[i].getMaximumSize().height,
                                    children[i].getAlignmentY());
                }
            }
        }
    }
}