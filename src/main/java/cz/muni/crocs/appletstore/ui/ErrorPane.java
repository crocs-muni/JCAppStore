package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.iface.CallBack;
import sun.misc.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class ErrorPane extends JPanel {

    private BufferedImage bgImg;

    public ErrorPane (int translationId, String imgName) {
//        try {
//            bgImg = ImageIO.read(new File(Config.IMAGE_DIR + "notifBg.png"));
//        } catch (IOException e) {
//            setOpaque(false);
//        }
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel error = new JLabel(new ImageIcon(Config.IMAGE_DIR + imgName));
        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        error.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(error);
        JLabel errorMsg = new JLabel("<html><div style=\"width:100%;\">"
                + Config.translation.get(translationId) + "</div></html>");
        errorMsg.setFont(CustomFont.plain.deriveFont(20f));
        errorMsg.setForeground(Color.BLACK);
        errorMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorMsg);
    }

    public ErrorPane (int translationId, String imgName, CallBack callable) {
        this(translationId, imgName);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel(new ImageIcon(Config.IMAGE_DIR + "sync.png"));
        panel.add(icon);

        JLabel retry = new JLabel(Config.translation.get(112));
        retry.setFont(CustomFont.plain.deriveFont(16f));
        retry.setForeground(Color.BLACK);
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

// in case of adding some translucent white bg
//    @Override
//    protected void paintComponent(Graphics g) {
//        //super.paintComponent(g);
//        if (bgImg != null) {
//            g.drawImage(bgImg, 0, 0, this);
//        }
//    }
}
