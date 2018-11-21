package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;
import java.awt.*;
import java.util.Arrays;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomJTabbedPaneUi extends BasicTabbedPaneUI {

    private Color selectColor;
    private Color deSelectColor;
    private int inclTab = 4;
    private int anchoFocoH = 16;
    private Polygon shape;

    public static ComponentUI createUI(JComponent c) {
        return new CustomJTabbedPaneUi();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabAreaInsets = new Insets(10,10 ,10 ,10 );
    }

    @Override
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        if (runCount > 1) {
            int lines[] = new int[runCount];
            for (int i = 0; i < runCount; i++) {
                lines[i] = rects[tabRuns[i]].y + (tabPlacement == TOP ? maxTabHeight : 0);
            }
            Arrays.sort(lines);
            if (tabPlacement == TOP) {
                int fila = runCount;
                for (int i = 0; i < lines.length - 1; i++, fila--) {
                    Polygon carp = new Polygon();
                    carp.addPoint(0, lines[i]);
                    carp.addPoint(tabPane.getWidth() - 2 * fila - 2, lines[i]);
                    carp.addPoint(tabPane.getWidth() - 2 * fila, lines[i] + 3);
                    if (i < lines.length - 2) {
                        carp.addPoint(tabPane.getWidth() - 2 * fila, lines[i + 1]);
                        carp.addPoint(0, lines[i + 1]);
                    } else {
                        carp.addPoint(tabPane.getWidth() - 2 * fila, lines[i] + rects[selectedIndex].height);
                        carp.addPoint(0, lines[i] + rects[selectedIndex].height);
                    }
                    carp.addPoint(0, lines[i]);
                    g.setColor(hazAlfa(fila));
                    g.fillPolygon(carp);
                    g.setColor(darkShadow.darker());
                    g.drawPolygon(carp);
                }
            } else {
                int fila = 0;
                for (int i = 0; i < lines.length - 1; i++, fila++) {
                    Polygon carp = new Polygon();
                    carp.addPoint(0, lines[i]);
                    carp.addPoint(tabPane.getWidth() - 2 * fila - 1, lines[i]);
                    carp.addPoint(tabPane.getWidth() - 2 * fila - 1, lines[i + 1] - 3);
                    carp.addPoint(tabPane.getWidth() - 2 * fila - 3, lines[i + 1]);
                    carp.addPoint(0, lines[i + 1]);
                    carp.addPoint(0, lines[i]);
                    g.setColor(hazAlfa(fila + 2));
                    g.fillPolygon(carp);
                    g.setColor(darkShadow.darker());
                    g.drawPolygon(carp);
                }
            }
        }
        super.paintTabArea(g, tabPlacement, selectedIndex);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2D = (Graphics2D) g;
        GradientPaint gradientShadow;
        int xp[]; // Para la forma
        int yp[];
        switch (tabPlacement) {
            case LEFT:
                xp = new int[]{x, x, x + w, x + w};
                yp = new int[]{y, y + h, y + h, y};
                break;
            case RIGHT:
                xp = new int[]{x, x, x + w - 2, x + w - 2, x};
                yp = new int[]{y, y + h - 3, y + h - 3, y, y};
                break;
            case BOTTOM:
                xp = new int[]{x, x, x + 3, x + w - inclTab - 6, x + w - inclTab - 2, x + w - inclTab, x + w - 3, x};
                yp = new int[]{y, y + h - 3, y + h, y + h, y + h - 1, y + h - 3, y, y};
                break;
            case TOP:
            default:
                xp = new int[]{x, x, x + w, x + w};
                yp = new int[]{y, y + h, y + h, y};
                break;
        }

        shape = new Polygon(xp, yp, xp.length);
        if (isSelected) {

           // g2D.setColor(selectColor);
            g2D.setColor(new Color(0, 0, 0, 0));
//            Border linebor = BorderFactory.createLineBorder(new Color(0x000000), 2);
//            g2D.set
        } else {
            g2D.setColor(new Color(0, 0, 0, 255));
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(new Color(0, 0, 0, 0));

        }
        //selectColor = new Color(255, 255, 200);
        //deSelectColor = new Color(240, 255, 210);

//        g2D.fill(shape);
//        if (runCount > 1) {
//            g2D.setColor(hazAlfa(getRunForTab(tabPane.getTabCount(), tabIndex) - 1));
//            g2D.fill(shape);
//        }
       g2D.fill(shape);
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        super.paintText(g, tabPlacement, CustomFont.plain, metrics, tabIndex, title, textRect, isSelected);
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return 20 + inclTab + super.calculateTabWidth(tabPlacement, tabIndex, metrics);
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        if (tabPlacement == LEFT || tabPlacement == RIGHT) {
            return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
        } else {
            return anchoFocoH + super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
        }
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        if (tabPane.hasFocus() && isSelected) {
            g.setColor(new Color(52, 52, 52, 246));
        } else {
            g.setColor(new Color(0, 0, 0, 0));
        }
    }

    protected Color hazAlfa(int fila) {
        int alfa = 0;
        if (fila >= 0) {
            alfa = 50 + (fila > 7 ? 70 : 10 * fila);
        }
        return new Color(0, 0, 0, alfa);
    }
}