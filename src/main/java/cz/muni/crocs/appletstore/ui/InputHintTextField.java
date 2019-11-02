package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.plaf.metal.MetalTextFieldUI;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class InputHintTextField extends JTextField implements FocusListener {

    private final String hint;
    private boolean isHint;

    public InputHintTextField(final String hint) {
        super(hint);
        this.hint = hint;
        setup();
    }

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

    public void setShowHint(boolean isHint) {
        this.isHint = isHint;
    }

    @Override
    public String getText() {
        return (isHint || super.getText().equals(hint)) ? "" : super.getText();
    }

}
