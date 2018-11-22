package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class ErrorPane extends JPanel {

    public ErrorPane(int translationId, String imgName) {
        new JPanel();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel error = new JLabel(new ImageIcon(Config.IMAGE_DIR + imgName));
        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        error.setBorder(new EmptyBorder(10, 10,10 ,10 ));
        add(error);
        JLabel errorMsg = new JLabel(Config.translation.get(translationId));
        errorMsg.setFont(CustomFont.plain.deriveFont(20f));
        errorMsg.setForeground(new Color(139, 139, 139));
        errorMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorMsg);
    }
}
