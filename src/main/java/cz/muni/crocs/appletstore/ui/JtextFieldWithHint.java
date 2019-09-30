package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class JtextFieldWithHint extends JTextField implements FocusListener {

    private String hint;
    private boolean hintOn = false;

    public JtextFieldWithHint(String text, String hint) {
        super(text == null || text.isEmpty() ? hint : text);
        setup(text, hint);
    }

    public JtextFieldWithHint(String text, String hint, int columns) {
        super(text == null || text.isEmpty() ? hint : text, columns);
        setup(text, hint);
    }

    public JtextFieldWithHint(Document doc, String text, String hint, int columns) {
        super(doc, text == null || text.isEmpty() ? hint : text, columns);
        setup(text, hint);
    }

    private void setup(String text, String hint) {
        hintOn = text == null || text.isEmpty();
        if (hintOn) setForeground(Color.GRAY);
        this.hint = hint;
        addFocusListener(this);
    }


    @Override
    public void focusGained(FocusEvent e) {
        if (hint == null)
            return;
        if (super.getText().equals(hint)) {
            setText("");
            setForeground(Color.BLACK);
            hintOn = false;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (hint == null)
            return;
        if (super.getText().isEmpty()) {
            setText(hint);
            setForeground(Color.GRAY);
            hintOn = true;
        }
    }

    @Override
    public String getText() {
        return hintOn ? null : super.getText();
    }
}
