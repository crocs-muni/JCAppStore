package cz.muni.crocs.appletstore.ui;

import javax.swing.*;

public class HtmlTitleLabel extends Title {

    public HtmlTitleLabel(String text, Icon icon, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, horizontalAlignment);
    }

    public HtmlTitleLabel(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    public HtmlTitleLabel(String text) {
        this(text, null, LEADING);
    }

    public HtmlTitleLabel(String text, Icon icon, float size, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, size, horizontalAlignment);
    }

    public HtmlTitleLabel(String text, float size, int horizontalAlignment) {
        this(text, null, size, horizontalAlignment);
    }

    public HtmlTitleLabel(String text, float size) {
        this(text, null, size, LEADING);
    }
}
