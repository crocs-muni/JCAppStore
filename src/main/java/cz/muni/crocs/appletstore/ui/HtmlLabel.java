package cz.muni.crocs.appletstore.ui;

import javax.swing.*;

public class HtmlLabel extends Text {

    public HtmlLabel(String text, Icon icon, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, horizontalAlignment);
    }

    public HtmlLabel(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    public HtmlLabel(String text) {
        this(text, null, LEADING);
    }

    public HtmlLabel(String text, Icon icon, float size, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, size, horizontalAlignment);
    }

    public HtmlLabel(String text, float size, int horizontalAlignment) {
        this(text, null, size, horizontalAlignment);
    }

    public HtmlLabel(String text, float size) {
        this(text, null, size, LEADING);
    }
}
