package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.iface.CallBack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Warning extends JPanel {

    public enum Importance {
        FATAL, SEVERE, INFO
    }

    public Warning(int translationId, Importance status, CallBack callable) {

        String image;
        switch (status) {
            case FATAL:
                image = "error.png";
                setBackground(new Color(193, 39, 39));
                break;
            case SEVERE:
                image = "announcement.png";
                setBackground(new Color(193, 95, 42));
                break;
            case INFO:
                image = "info.png";
                setBackground(new Color(193, 149, 40));
                break;
            default:
                image = "info.png";
        }

        ((FlowLayout)getLayout()).setAlignment(FlowLayout.CENTER);

        JLabel error = new JLabel(new ImageIcon(Config.IMAGE_DIR + image));
        error.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(error);

        JLabel errorMsg = new JLabel("<html><div style=\"width:100%;\">"
                + Config.translation.get(translationId) + "</div></html>");
        errorMsg.setFont(CustomFont.plain.deriveFont(12f));
        errorMsg.setForeground(Color.BLACK);
        add(errorMsg);

        JLabel icon = new JLabel(new ImageIcon(Config.IMAGE_DIR + "sync.png"));
        add(icon);

        JLabel retry = new JLabel(Config.translation.get(112));
        retry.setFont(CustomFont.plain.deriveFont(12f));
        retry.setForeground(Color.BLACK);
        add(retry);

        retry.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                callable.callBack();
            }
        });

    }
}
