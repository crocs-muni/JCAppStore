package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.CallBack;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * todo worth creating interface
 * @author Jiří Horák
 * @version 1.0
 */
public class ErrorPane extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public ErrorPane (String title, String imgName) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        requestFocusInWindow();
        JLabel error = new JLabel(new ImageIcon(Config.IMAGE_DIR + imgName));
        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        error.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPopupMenu menu = new JPopupMenu();

        JMenuItem item = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                error.getText() + //todo
            }
        });
        item.setIcon(new ImageIcon(Config.IMAGE_DIR + "copy.png"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        item.setText(textSrc.getString("copy"));
        menu.add(item);
        error.setComponentPopupMenu(menu);
        add(error);
        JLabel errorMsg = new JLabel(title);
        errorMsg.setFont(OptionsFactory.getOptions().getTitleFont(20f));
        errorMsg.setForeground(Color.WHITE);
        errorMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorMsg.setComponentPopupMenu(menu);
        add(errorMsg);
    }

    public ErrorPane (String titleKey, String imgName, CallBack callable) {
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

    public ErrorPane (String title, String message, String imgName) {
        this(title, imgName);

        JLabel hint = new HtmlText("<p width=\"400\" align=\"center\">" + message + "</p>");
        hint.setBorder(new EmptyBorder(20, 20, 20, 20));
        hint.setFont(OptionsFactory.getOptions().getFont(16f));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(Color.WHITE);
        add(hint);
    }
}
