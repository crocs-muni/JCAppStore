package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.ui.Title;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class StoreSubMenu extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private BufferedImage storeTitle;
    private JButton reload;

    public StoreSubMenu() {
        try {
            storeTitle = ImageIO.read(new File(Config.IMAGE_DIR + "jcappstore-submenu.png"));
            setup();
        } catch (IOException e) {
            setupWithoutImage();
        }
    }

    void setOnReload(Action a) {
        reload.addActionListener(a);
    }

    private void setupWithoutImage() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        Title t = new Title(textSrc.getString("jcappstore"), 30f, SwingConstants.LEFT);
        t.setForeground(Color.white);
        t.setBorder(BorderFactory.createEmptyBorder(10, 40, 5, 0));
        add(t);

        add(Box.createHorizontalGlue());

        setupReloadButton();
        reload.setForeground(Color.WHITE);
        reload.setAlignmentY(Component.TOP_ALIGNMENT);
        add(reload);
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(Color.WHITE);
        setupReloadButton();
        reload.setAlignmentX(Component.RIGHT_ALIGNMENT);
        reload.setBorder(BorderFactory.createEmptyBorder(15, 20, 13, 0));
        add(reload);
    }

    private void setupReloadButton() {
        reload = new JButton("<html>" + textSrc.getString("store_refresh") + "</div></html>");
        reload.setUI(new CustomButtonUI());
        reload.setFont(OptionsFactory.getOptions().getFont(Font.BOLD, 12f));
        reload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reload.setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (storeTitle != null) {
            g.drawImage(storeTitle, 0, 0, this);
        }
    }
}
