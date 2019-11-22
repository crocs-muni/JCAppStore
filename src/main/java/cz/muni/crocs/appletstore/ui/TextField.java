package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.plaf.PopupMenuUI;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;

public class TextField {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

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

        JPopupMenu menu = new JPopupMenu();

        JMenuItem item = new JMenuItem(new DefaultEditorKit.CopyAction());
        item.setIcon(new ImageIcon(Config.IMAGE_DIR + "copy.png"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        item.setText(textSrc.getString("copy"));
        menu.add(item);
        field.setComponentPopupMenu(menu);

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
