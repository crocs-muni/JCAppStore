package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class TextField {
    public static JTextPane getTextField(String text) {
        JTextPane field = getTextFieldCore();
        field.setText("<html><div>" + text + "</div></html>");
        return field;
    }

    public static JTextPane getTextField(String text, String css, Color background) {
        JTextPane field = getTextFieldCore();
        field.setText("<html><div style=\"" + css + "\">" + text + "</div></html>");
        if (background == null) {
            field.setOpaque(false);
        } else {
            field.setBackground(background);
        }
        return field;
    }

    private static JTextPane getTextFieldCore() {
        JTextPane field = new JTextPane();
        DefaultCaret caret = (DefaultCaret) field.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        field.setContentType("text/html");
        field.setOpaque(true);
        field.setEditable(false);
        field.setBorder(null);
        field.setFont(OptionsFactory.getOptions().getFont());
        return field;
    }
}
