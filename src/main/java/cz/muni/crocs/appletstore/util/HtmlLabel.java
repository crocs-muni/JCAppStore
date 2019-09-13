package cz.muni.crocs.appletstore.util;

import javax.swing.*;

public class HtmlLabel extends JLabel {

    public HtmlLabel(String text, Icon icon, int horizontalAlignment) {
        super("<html>" + text + "<html>", icon, horizontalAlignment);
    }

    public HtmlLabel(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    public HtmlLabel(String text) {
        this(text, null, LEADING);
    }
}
