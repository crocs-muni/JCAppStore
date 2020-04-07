package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

/**
 * BasicSplitPaneDivider custom graphics
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomSplitPaneDivider extends BasicSplitPaneDivider {

    private Font title = OptionsFactory.getOptions().getFont(Font.BOLD, 10f);

    /**
     * Creates an instance of BasicSplitPaneDivider. Registers this
     * instance for mouse events and mouse dragged events.
     *
     * @param ui split pane parent ui
     */
    public CustomSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
    }

    @Override
    protected void oneTouchExpandableChanged() {
        //delete buttons on split pane
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Border b = getBorder();
        if (b != null) {
            Dimension size = getSize();
            g.setColor(new Color(255, 255, 255, 100));
            g.drawRect(0, 0, size.width, size.height);
            b.paintBorder(this, g, 0, 0, size.width, size.height);
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.white);
        g2d.setFont(title);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.drawString("JCAppStore logger", 18, 11);
    }
};

