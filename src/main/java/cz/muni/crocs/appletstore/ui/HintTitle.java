package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;

/**
 * Extension of hint label.
 * Allows to adjust font styles/sizes easily, to setup all title components in an uniform way
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class HintTitle extends HintLabel {

    /**
     * Create a title component with hint of default size
     */
    public HintTitle() {
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a title component with hint
     * @param hint hint to display when hovered
     */
    public HintTitle(String labelTitle, String hint) {
        super(labelTitle, hint);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a title component with hint
     * @param hint hint to display when hovered
     * @param icon icon to display
     */
    public HintTitle(Icon icon, String hint) {
        super(icon, hint);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a title component with hint
     * @param text text to display
     * @param hint hint to display when hovered
     * @param icon icon to display
     */
    public HintTitle(String text, String hint, Icon icon) {
        super(text, hint, icon);
        setFont(OptionsFactory.getOptions().getFont());
    }

    /**
     * Create a title component with hint
     * @param size text size
     */
    public HintTitle(float size) {
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a title component with hint
     * @param hint hint to display when hovered
     * @param size text size
     */
    public HintTitle(String labelTitle, String hint, float size) {
        super(labelTitle, hint);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a title component with hint
     * @param hint hint to display when hovered
     * @param icon icon to display
     * @param size text size
     */
    public HintTitle(Icon icon, String hint, float size) {
        super(icon, hint);
        setFont(OptionsFactory.getOptions().getFont(size));
    }

    /**
     * Create a title component with hint
     * @param text text to display
     * @param hint hint to display when hovered
     * @param icon icon to display
     * @param size text size
     */
    public HintTitle(String text, String hint, Icon icon, float size) {
        super(text, hint, icon);
        setFont(OptionsFactory.getOptions().getFont(size));
    }
}
