package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.plaf.metal.MetalTextFieldUI;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Hint component as an input field
 * unlike HintPanel, this component displays the text inside if empty using gray color
 * (e.g. Search...)
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class InputHintTextField extends JTextField implements FocusListener {

    private final String hint;
    private boolean isHint;

    /**
     * Create an input field with hint
     * @param hint hint to display
     */
    public InputHintTextField(final String hint) {
        super(hint);
        this.hint = hint;
        setup();
    }

    /**
     * Create an input field with hint
     * @param text text to fill with
     * @param hint hint to display if text removed
     */
    public InputHintTextField(String text, final String hint) {
        super(text == null || text.isEmpty() ? hint : text);
        this.hint = hint;
        setup();
    }

    private void setup() {
        isHint = true;
        setBorder(null);
        setOpaque(false);
        addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (super.getText().equals(hint)) {
            setText("");
            isHint = false;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (super.getText().isEmpty()) {
            setText(hint);
            isHint = true;
        }
    }

    /**
     * Set whether hint should be displayed
     * @param isHint true if display
     */
    public void setShowHint(boolean isHint) {
        this.isHint = isHint;
    }

    @Override
    public String getText() {
        return (isHint || super.getText().equals(hint)) ? "" : super.getText();
    }

}
