package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.HtmlText;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

public class NotFoundItem extends JPanel implements Item, Comparable<Item> {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public NotFoundItem() {
        setOpaque(false);
        JLabel label = new HtmlText("<div width=\"150\">" + textSrc.getString("no_results") + "</div>");
        label.setOpaque(false);
        label.setForeground(Color.white);
        add(label);
    }

    @Override
    public String getSearchQuery() {
        return "";
    }

    @Override
    public int compareTo(Item o) {
        return 1;
    }
}
