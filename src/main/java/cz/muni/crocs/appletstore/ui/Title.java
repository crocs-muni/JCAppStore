package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;

/**
 * JLabel extension allowing font (size/style) one-liner setup ofr titles
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Title extends JLabel {

    /**
     * Create a JLabel title
     * @param s text to display
     * @param icon icon to display
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Title(String s, Icon icon, int horizontalAlignment) {
        super(s, icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel title
     * @param s text to display
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Title(String s, int horizontalAlignment) {
        super(s, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel title
     * @param s text to display
     */
    public Title(String s) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel title
     * @param icon icon to display
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Title(Icon icon, int horizontalAlignment) {
        super(icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel title
     * @param icon icon to display
     */
    public Title(Icon icon) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel title
     */
    public Title() {
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a JLabel title
     * @param s text to display
     * @param icon icon to display
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Title(String s, Icon icon, float size, int horizontalAlignment) {
        super(s, icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel title
     * @param s text to display
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Title(String s, float size, int horizontalAlignment) {
        super(s, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel title
     * @param s text to display
     * @param size float text size
     */
    public Title(String s, float size) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel title
     * @param icon icon to display
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Title(Icon icon, float size, int horizontalAlignment) {
        super(icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel title
     * @param icon icon to display
     * @param size float text size
     */
    public Title(Icon icon, float size) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel title
     * @param size float text size
     */
    public Title(float size) {
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a JLabel title
     * @param style text style as defined in JLabel
     */
    public Title(int style) {
        setFont(OptionsFactory.getOptions().getFont(style));
    }

    /**
     * Create a JLabel title
     * @param s text to display
     * @param icon icon to display
     * @param style text style as defined in JLabel
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Title(String s, Icon icon, int style, float size, int horizontalAlignment) {
        super(s, icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel title
     * @param s text to display
     * @param style text style as defined in JLabel
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Title(String s, int style, float size, int horizontalAlignment) {
        super(s, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel title
     * @param s text to display
     * @param style text style as defined in JLabel
     * @param size float text size
     */
    public Title(String s, int style, float size) {
        super(s);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel title
     * @param icon icon to display
     * @param style text style as defined in JLabel
     * @param size float text size
     * @param horizontalAlignment text alignment as defined in JLabel
     */
    public Title(Icon icon, int style, float size, int horizontalAlignment) {
        super(icon, horizontalAlignment);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel title
     * @param icon icon to display
     * @param style text style as defined in JLabel
     * @param size float text size
     */
    public Title(Icon icon, int style, float size) {
        super(icon);
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }

    /**
     * Create a JLabel title
     * @param style text style as defined in JLabel
     * @param size float text size
     */
    public Title(int style, float size) {
        setFont(OptionsFactory.getOptions().getFont(style, size));
    }
}
