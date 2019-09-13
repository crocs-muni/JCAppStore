package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class Components {

    public static HintLabel getHintLabel(String title, String hint, Font font) {
        HintLabel label = new HintLabel(title, hint);
        if (font != null) label.setFont(font);
        label.setFocusable(true);
        return label;
    }

    public static HintLabel getHintLabel(String title, String hint, Font font, Border border) {
        HintLabel label = getHintLabel(title, hint, font);
        label.setBorder(border);
        return label;
    }

    public static JLabel getLabel(String title, Font font) {
        JLabel label = new JLabel(title);
        if (font != null) label.setFont(font);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return label;
    }

    public static JLabel getLabel(String title, Font font, Border border) {
        JLabel label = getLabel(title, font);
        label.setBorder(border);
        return label;
    }

    public static JComboBox<String> getBoxSelection(String[] values) {
        JComboBox<String> box = new StyledComboBox<>(values);
        box.setMaximumRowCount(5);
        return box;
    }

    public static JButton getButton(String text, String css, Float fontSize, Color foreground, Color background) {
        JButton button = new JButton("<html><div style=\"" + css + "\">" + text + "</div></html>");
        button.setUI(new CustomButtonUI());
        button.setFont(OptionsFactory.getOptions().getDefaultFont().deriveFont(Font.BOLD, fontSize));
        button.setForeground(foreground);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(background);
        return button;
    }

    public static JTextPane getTextField(String text, Font font) {
        JTextPane field = new JTextPane();
        DefaultCaret caret = (DefaultCaret) field.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        field.setContentType("text/html");
        field.setText("<html><div style=\"margin: 10px; width:600px\">" + text + "</div></html>");
        field.setBackground(new Color(255, 255, 255, 80));
        field.setOpaque(true);
        field.setEditable(false);
        field.setBorder(null);
        return field;
    }
}
