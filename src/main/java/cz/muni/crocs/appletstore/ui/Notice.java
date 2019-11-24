package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.CallBack;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Notice extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    public enum Importance {
        FATAL, SEVERE, INFO
    }

    public enum CallBackIcon {
        RETRY, CLOSE, NO_ICON
    }

    public Notice(String msg, Importance status, CallBackIcon type, CallBack ... onClick) {
        setLayout(new MigLayout("center, gapy 20, insets 0 20 0 20"));
        MouseAdapter call = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (CallBack c : onClick) {
                    c.callBack();
                }
            }
        };

        String image;
        switch (status) {
            case FATAL:
                image = "error.png";
                setBackground(new Color(193, 39, 39));
                break;
            case SEVERE:
                image = "announcement.png";
                setBackground(new Color(193, 137, 56));
                break;
            case INFO:
                image = "info.png";
                setBackground(new Color(159, 193, 55));
                break;
            default:
                image = "info.png";
        }

//        ((FlowLayout) getLayout()).setAlignment(FlowLayout.CENTER);

        JLabel error = new Text(new ImageIcon(Config.IMAGE_DIR + image));
        error.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(error);

        JLabel errorMsg = new HtmlText("<div style=\"max-width:90%;\">" + msg + "</div>");
        errorMsg.setFont(OptionsFactory.getOptions().getTitleFont(12f));
        errorMsg.setForeground(Color.BLACK);
        add(errorMsg, "growx");

        switch (type) {
            case CLOSE:
                JLabel iconClose = new JLabel(new ImageIcon(Config.IMAGE_DIR + "close_black.png"));
                iconClose.setBorder(new EmptyBorder(0, 20, 0, 0));
                iconClose.addMouseListener(call);
                add(iconClose);
                break;
            case RETRY:
                JLabel iconRetry = new JLabel(new ImageIcon(Config.IMAGE_DIR + "sync.png"));
                iconRetry.setBorder(new EmptyBorder(0, 20, 0, 0));
                add(iconRetry);

                JLabel retry = new JLabel(textSrc.getString("retry"));
                retry.setFont(OptionsFactory.getOptions().getTitleFont(12f));
                retry.setForeground(Color.BLACK);
                add(retry);
                retry.addMouseListener(call);
                break;
            default:
                break;
        }
    }
}
