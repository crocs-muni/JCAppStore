package cz.muni.crocs.appletstore.ui;

import javax.swing.*;

/**
 * JLabel extension allowing easy font setup (style/size) and auto-include html tags for titles
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class HtmlTitle extends Title {

    /**
     * Create a HTML JLabel text component
     * @param text text to display
     * @param icon icon to display
     * @param horizontalAlignment alignment as defined by JLabel
     */
    public HtmlTitle(String text, Icon icon, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, horizontalAlignment);
    }

    /**
     * Create a HTML JLabel text component
     * @param text text to display
     * @param horizontalAlignment alignment as defined by JLabel
     */
    public HtmlTitle(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    /**
     * Create a HTML JLabel text component
     * @param text text to display
     */
    public HtmlTitle(String text) {
        this(text, null, LEADING);
    }

    /**
     * Create a HTML JLabel text component
     * @param text text to display
     * @param icon icon to display
     * @param size text size
     * @param horizontalAlignment alignment as defined by JLabel
     */
    public HtmlTitle(String text, Icon icon, float size, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, size, horizontalAlignment);
    }

    /**
     * Create a HTML JLabel text component
     * @param text text to display
     * @param size text size
     * @param horizontalAlignment alignment as defined by JLabel
     */
    public HtmlTitle(String text, float size, int horizontalAlignment) {
        this(text, null, size, horizontalAlignment);
    }

    /**
     * Create a HTML JLabel text component
     * @param text text to display
     * @param size text size
     */
    public HtmlTitle(String text, float size) {
        this(text, null, size, LEADING);
    }

    /**
     * Create a HTML JLabel text component
     * @param text text to display
     * @param icon icon to display
     * @param style text style as defined by JLabel
     * @param size text size
     * @param horizontalAlignment alignment as defined by JLabel
     */
    public HtmlTitle(String text, Icon icon, int style, float size, int horizontalAlignment) {
        super("<html>" + text + "</html>", icon, style, size, horizontalAlignment);
    }

    /**
     * Create a HTML JLabel text component
     * @param text text to display
     * @param style text style as defined by JLabel
     * @param size text size
     * @param horizontalAlignment alignment as defined by JLabel
     */
    public HtmlTitle(String text, int style, float size, int horizontalAlignment) {
        this(text, null, style, size, horizontalAlignment);
    }

    /**
     * Create a HTML JLabel text component
     * @param text text to display
     * @param style text style as defined by JLabel
     * @param size text size
     */
    public HtmlTitle(String text, int style, float size) {
        this(text, null, style, size, LEADING);
    }
}
