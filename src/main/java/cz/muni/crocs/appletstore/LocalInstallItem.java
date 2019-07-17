package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.iface.Item;
import cz.muni.crocs.appletstore.ui.CustomFont;

import cz.muni.crocs.appletstore.util.Sources;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalInstallItem extends JPanel implements Item, Comparable<Item> {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public LocalInstallItem() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        add(Box.createRigidArea(new Dimension(50, 30)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel icon = new JLabel("<html><img src=\"file:///" +
                new File(Config.IMAGE_DIR + "install_icon.png").getAbsolutePath() +
                "\" width=\"83\" height=\"56\" /> </html>");
        icon.setMaximumSize(new Dimension(83, 56));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(icon);

        add(Box.createRigidArea(new Dimension(50, 20)));

        JLabel title = new JLabel(textSrc.getString("CAP_install_applet"));
        title.setFont(CustomFont.plain.deriveFont(16f));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);

        title = new JLabel(textSrc.getString("from_pc"));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        add(title);
    }

    @Override
    public String getSearchQuery() {
        return textSrc.getString("install_kwords");
    }

    @Override
    public void paint(Graphics g) {
        ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        super.paint(g);
    }

    /**
     * Supposed to be the last element, insterted only once
     * @param o object to compare to
     * @return -1
     */
    @Override
    public int compareTo(Item o) {
        return -1;
    }
}
