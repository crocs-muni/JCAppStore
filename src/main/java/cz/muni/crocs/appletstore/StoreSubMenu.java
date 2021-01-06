package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.ui.Title;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * SubMenu from the store, can return from detailed info panel nad re-download the store
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreSubMenu extends JPanel {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private JLabel back;

    public StoreSubMenu() {
        //setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(true);

        setupBackLabelButton();
        add(back);
    }

    /**
     * Set callback for back
     * @param a action that can return from applet detailed info panel
     */
    public void setOnBack(Action a) {
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                a.actionPerformed(null);
            }
        });
    }

    public void setShowBackButton(boolean doShow) {
        back.setVisible(doShow);
        back.invalidate();
    }

    private void setupBackLabelButton() {
        back = new JLabel(new ImageIcon(Config.IMAGE_DIR + "back.png"));
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
