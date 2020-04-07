package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;

/**
 * Extension of hint label.
 * Allows to adjust font styles/sizes easily, to setup all text components in an uniform way
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class HintText extends HintLabel {

    /**
     * Create a text with default size
     */
    public HintText() {
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create an iamge with hint
     * @param text text to display
     * @param hint hint to display when hovered
     */
    public HintText(String text, String hint) {
        super(text, hint);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create an image with hint
     * @param icon icon to display
     * @param hint hint to display when hovered
     */
    public HintText(Icon icon, String hint) {
        super(icon, hint);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a text with image and hint
     * @param text text to display
     * @param icon icon to display
     * @param hint hint to display when hovered
     */
    public HintText(String text, String hint, Icon icon) {
        super(text, hint, icon);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create text with hint
     * @param size text size
     */
    public HintText(float size) {
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a text with hint
     * @param text text to display
     * @param hint hint to display when hovered
     * @param size text size
     */
    public HintText(String text, String hint, float size) {
        super(text, hint);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create an image with hint
     * @param icon icon to display
     * @param hint hint to display when hovered
     * @param size text size
     */
    public HintText(Icon icon, String hint, float size) {
        super(icon, hint);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create text with image and hint
     * @param text text to display
     * @param icon icon to display
     * @param hint hint to display when hovered
     * @param size text size
     */
    public HintText(String text, String hint, Icon icon, float size) {
        super(text, hint, icon);
        setFont(OptionsFactory.getOptions().getFont(size));
    }
}
