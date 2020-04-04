package cz.muni.crocs.appletstore.ui;

import javax.swing.*;

public class HtmlText extends Text {

    public HtmlText() {
    }

    public HtmlText(String text, Icon icon, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, horizontalAlignment);
    }

    public HtmlText(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    public HtmlText(String text) {
        this(text, null, LEADING);
    }

    public HtmlText(String text, Icon icon, float size, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, size, horizontalAlignment);
    }

    public HtmlText(String text, float size, int horizontalAlignment) {
        this(text, null, size, horizontalAlignment);
    }

    public HtmlText(String text, float size) {
        this(text, null, size, LEADING);
    }

    public HtmlText(String text, Icon icon, int style, float size, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, style, size, horizontalAlignment);
    }

    public HtmlText(String text, int style, float size, int horizontalAlignment) {
        this(text, null, style, size, horizontalAlignment);
    }

    public HtmlText(String text, int style, float size) {
        this(text, null, style, size, LEADING);
    }

    @Override
    public void setText(String text) {
        super.setText("<html>" + text + "</html>");
    }
}
