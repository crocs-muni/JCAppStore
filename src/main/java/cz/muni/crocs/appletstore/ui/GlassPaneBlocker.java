package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;

import javax.swing.*;
import java.awt.*;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class GlassPaneBlocker extends JPanel implements MouseListener, FocusListener {

    public GlassPaneBlocker() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        add(new JLabel(new ImageIcon(Config.IMAGE_DIR + "loading.gif")));

        addMouseListener(this);
        addFocusListener(this);
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
}

