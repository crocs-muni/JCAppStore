package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class InputHintTextField extends JTextField implements FocusListener {


    private final String hint;
    private boolean showingHint;

    public InputHintTextField(final String hint) {
        super(hint);
        this.hint = hint;
        this.showingHint = true;
        this.setText(hint);
        this.setBorder(null);
        this.setOpaque(false);
        super.addFocusListener(this);
    }

    @Override
    protected void paintBorder(Graphics g) {
        //delete
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setText("");
            showingHint = false;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setText(hint);
            showingHint = true;
        }
    }

    @Override
    public String getText() {
        return showingHint ? "" : super.getText();
    }
}
