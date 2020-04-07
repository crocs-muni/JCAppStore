package cz.muni.crocs.appletstore.ui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Label that displays a hint message when hovered over
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class HintLabel extends JLabel {

    private String hint;

    /**
     * Create a Hint
     */
    public HintLabel() {}

    /**
     * Create a Hint
     * @param labelTitle title to display
     * @param hint hint to display when hovered
     */
    public HintLabel(String labelTitle, String hint) {
        super(labelTitle);
        this.hint = hint;
    }

    /**
     * Create a Hint
     * @param icon icon to display in label
     * @param hint hint to display when hovered
     */
    public HintLabel(Icon icon, String hint) {
        super(icon);
        this.hint = hint;
    }

    /**
     * Create a Hint
     * @param labelTitle title to display
     * @param hint hint to display when hovered
     * @param icon icon to display in label
     */
    public HintLabel(String labelTitle, String hint, Icon icon) {
        super(labelTitle, icon, JLabel.LEFT);
        this.hint = hint;
    }

    /**
     * Set text and hint
     * @param labelTitle text to set to component
     * @param hint hint to display
     */
    public void setText(String labelTitle, String hint) {
        super.setText(labelTitle);
        setHint(hint);
    }

    /**
     * Set hint only
     * @param hint hint to display
     */
    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getHint() {
        return hint;
    }
}