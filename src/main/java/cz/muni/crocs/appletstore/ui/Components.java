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

    public static JPanel getNotice(String text, float fontSize, Color background, ImageIcon icon, String css) {
        final int depth = 5;
        // idea from https://stackoverflow.com/questions/13368103/jpanel-drop-shadow
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                int color = 0;
                int maxOp = 80;
                for (int i = 0; i < depth; i++) {
                    g.setColor(new Color(color, color, color, ((maxOp / depth) * i)));
                    g.drawRect(i, i, this.getWidth() - ((i * 2) + 1), this.getHeight() - ((i * 2) + 1));
                }
                g.setColor(background);
                g.fillRect(depth, depth, getWidth() - depth * 2, getHeight() - depth * 2);
            }
        };
        container.setBorder(BorderFactory.createCompoundBorder(
                container.getBorder(), BorderFactory.createEmptyBorder(depth, depth, depth, depth))
        );

        JLabel img = new JLabel(icon);
        JLabel desc = new HtmlText("<div style=\"" + css + "\">" + text + "</div>", fontSize);
        img.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        desc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        container.add(img);
        container.add(desc);
        return container;
    }

    public static HintLabel getHintLabel(String title, String hint, Font font, Border border) {
        HintLabel label = getHintLabel(title, hint, font);
        label.setBorder(border);
        return label;
    }

    public static JLabel getLabel(String title, float size) {
        JLabel label = new Text(title, size);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return label;
    }

    public static JLabel getLabel(String title, float size, Border border) {
        JLabel label = getLabel(title, size);
        label.setBorder(border);
        return label;
    }

    public static JComboBox<String> getBoxSelection(String[] values) {
        JComboBox<String> box = new StyledComboBox<>(values);
        box.setMaximumRowCount(5);
        return box;
    }

    public static JButton getButton(String text, String css, Float fontSize, Color foreground, Color background, boolean title) {
        JButton button = new JButton("<html><div style=\"" + css + "\">" + text + "</div></html>");
        button.setUI(new CustomButtonUI());
        button.setFont(title ? OptionsFactory.getOptions().getTitleFont(Font.BOLD, fontSize) : OptionsFactory.getOptions().getFont(Font.BOLD, fontSize));
        button.setForeground(foreground);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(background);
        return button;
    }

    public static JTextPane getTextField(String text, Font font) {
        JTextPane field = getTextFieldCore();
        field.setText("<html><div>" + text + "</div></html>");
        field.setFont(font);
        return field;
    }

    public static JTextPane getTextField(String text, Font font, String css, Color background) {
        JTextPane field = getTextFieldCore();
        field.setText("<html><div style=\"" + css + "\">" + text + "</div></html>");
        if (background == null) {
            field.setOpaque(false);
        } else {
            field.setBackground(background);
        }
        field.setFont(font);
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
        return field;
    }
}
