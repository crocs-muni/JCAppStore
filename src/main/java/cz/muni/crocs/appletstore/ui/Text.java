package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;

/**
 * JLabel extension allowing font (size/style) one-liner setup
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Text extends JLabel {

    /**
     * Create a JLabel
     * @param s text to display
     * @param icon icon to display
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Text(String s, Icon icon, int horizontalAlignment) {
        super(s, icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel
     * @param s text to display
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Text(String s, int horizontalAlignment) {
        super(s, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel
     * @param s text to display
     */
    public Text(String s) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel
     * @param icon icon to display
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Text(Icon icon, int horizontalAlignment) {
        super(icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel
     * @param icon icon to display
     */
    public Text(Icon icon) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel
     */
    public Text() {
        setFont(OptionsFactory.getOptions().getFont());
    }

    public Text(String s, Icon icon, float size, int horizontalAlignment) {
        super(s, icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel
     * @param s text to display
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Text(String s, float size, int horizontalAlignment) {
        super(s, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel
     * @param s text to display
     * @param size float text size
     */
    public Text(String s, float size) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel
     * @param icon icon to display
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Text(Icon icon, float size, int horizontalAlignment) {
        super(icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel
     * @param icon icon to display
     * @param size float text size
     */
    public Text(Icon icon, float size) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel
     * @param size float text size
     */
    public Text(float size) {
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel
     * @param style text style as defined in JLabel
     */
    public Text(int style) {
        setFont(OptionsFactory.getOptions().getFont(style));
    }

    /**
     * Create a JLabel
     * @param s text to display
     * @param icon icon to display
     * @param style text style as defined in JLabel
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Text(String s, Icon icon, int style, float size, int horizontalAlignment) {
        super(s, icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel
     * @param s text to display
     * @param style text style as defined in JLabel
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Text(String s, int style, float size, int horizontalAlignment) {
        super(s, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel
     * @param s text to display
     * @param style text style as defined in JLabel
     * @param size float text size
     */
    public Text(String s, int style, float size) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel
     * @param s text to display
     * @param icon icon to display
     * @param style text style as defined in JLabel
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Text(Icon icon, int style, float size, int horizontalAlignment) {
        super(icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel
     * @param icon icon to display
     * @param style text style as defined in JLabel
     * @param size float text size
     */
    public Text(Icon icon, int style, float size) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel
     * @param style text style as defined in JLabel
     * @param size float text size
     */
    public Text(int style, float size) {
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }
}
