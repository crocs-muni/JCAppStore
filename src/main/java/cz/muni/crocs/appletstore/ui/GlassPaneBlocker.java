package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;

/**
 * "working..." pane blocker that blocks user interaction while card is being used
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class GlassPaneBlocker extends JPanel implements MouseListener, FocusListener {
    private static final ResourceBundle textSrc =
        ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private final JLabel working;

    public GlassPaneBlocker() {
        setLayout(new MigLayout("align center center, gapy 15"));
        add(new JLabel(new ImageIcon(Config.IMAGE_DIR + "load.gif")), "align center, wrap");

        working = new JLabel(textSrc.getString("working"));
        working.setFont(OptionsFactory.getOptions().getTitleFont(20f));
        add(working, "align center, wrap");

        setOpaque(false);
        addMouseListener(this);
        addFocusListener(this);
    }

    public void setMessage(String msg) {
        working.setText(msg);
        revalidate();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e.consume();
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(1f, 1f, 1f, 0.8f));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

