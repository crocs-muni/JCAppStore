package cz.muni.crocs.appletstore.ui;

import javax.swing.*;

public class HtmlTitle extends Title {

    public HtmlTitle(String text, Icon icon, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, horizontalAlignment);
    }

    public HtmlTitle(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    public HtmlTitle(String text) {
        this(text, null, LEADING);
    }

    public HtmlTitle(String text, Icon icon, float size, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, size, horizontalAlignment);
    }

    public HtmlTitle(String text, float size, int horizontalAlignment) {
        this(text, null, size, horizontalAlignment);
    }

    public HtmlTitle(String text, float size) {
        this(text, null, size, LEADING);
    }

    public HtmlTitle(String text, Icon icon, int style, float size, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, style, size, horizontalAlignment);
    }

    public HtmlTitle(String text, int style, float size, int horizontalAlignment) {
        this(text, null, style, size, horizontalAlignment);
    }

    public HtmlTitle(String text, int style, float size) {
        this(text, null, style, size, LEADING);
    }
}
