package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.CallBack;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * todo worth creating interface
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ErrorPane extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    public ErrorPane(String title, String imgName) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        requestFocusInWindow();

        JLabel error = new JLabel(new ImageIcon(Config.IMAGE_DIR + imgName));
        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        error.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(error);

        JLabel errorMsg = new JLabel(title);
        errorMsg.setFont(OptionsFactory.getOptions().getTitleFont(20f));
        errorMsg.setForeground(Color.WHITE);
        errorMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorMsg);
    }

    public ErrorPane(String titleKey, String imgName, CallBack callable) {
        this(titleKey, imgName);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel(new ImageIcon(Config.IMAGE_DIR + "sync.png"));
        panel.add(icon);

        JLabel retry = new Text(textSrc.getString("retry"), 16f);
        retry.setForeground(Color.WHITE);
        panel.add(retry);

        panel.setOpaque(false);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                callable.callBack();
            }
        });
        add(panel);
    }

    public ErrorPane(String title, String message, String imgName) {
        this(title, imgName);
        add(getCopiableLabel(message));
    }

    public JTextPane getCopiableLabel(String text) {
        JTextPane f = TextField.getTextField(text, "width: 300px; text-align:center;", Color.WHITE);
        f.setAlignmentX(CENTER_ALIGNMENT);
        f.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        f.setOpaque(false);

        StyledDocument doc = f.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontFamily(style, OptionsFactory.getOptions().getFont().getFamily());
        StyleConstants.setForeground(style, Color.WHITE);
        StyleConstants.setFontSize(style, 14);
        doc.setParagraphAttributes(0, doc.getLength(), style, false);
        return f;
    }
}
