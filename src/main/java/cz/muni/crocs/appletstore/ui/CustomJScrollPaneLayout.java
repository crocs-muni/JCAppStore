package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;

public class CustomJScrollPaneLayout extends ScrollPaneLayout {
    @Override
    public void layoutContainer(Container parent) {
        JScrollPane scrollPane = (JScrollPane)parent;

        Rectangle availR = scrollPane.getBounds();
        availR.x = availR.y = 0;

        Insets insets = parent.getInsets();
        availR.x = insets.left;
        availR.y = insets.top;
        availR.width  -= insets.left + insets.right;
        availR.height -= insets.top  + insets.bottom;

        Rectangle vsbR = new Rectangle();
        vsbR.width  = 12;
        vsbR.height = availR.height;
        vsbR.x = availR.x + availR.width - vsbR.width;
        vsbR.y = availR.y;

        if(viewport != null) {
            viewport.setBounds(availR);
        }
        if(vsb != null) {
            vsb.setVisible(true);
            vsb.setBounds(vsbR);
        }
    }
}
