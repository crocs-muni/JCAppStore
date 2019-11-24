package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.Text;
import cz.muni.crocs.appletstore.ui.Title;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Specific Item that is not installed on card but offers applet installation on click
 * always as the last item
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalInstallItem extends JPanel implements Item, Comparable<Item> {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    public LocalInstallItem() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 35, 0, 0));

        add(Box.createRigidArea(new Dimension(50, 30)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel icon = new HtmlText("<img src=\"file:///" +
                new File(Config.IMAGE_DIR + "install_icon.png").getAbsolutePath() +
                "\" width=\"83\" height=\"83\" />");
        icon.setMaximumSize(new Dimension(83, 83));
        icon.setMinimumSize(new Dimension(83, 83));

        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(icon);

        add(Box.createRigidArea(new Dimension(50, 20)));

        JLabel label = new Title(textSrc.getString("CAP_install_applet"), 16f);
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(label);

        label = new Text(textSrc.getString("from_pc"));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        add(label);
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
