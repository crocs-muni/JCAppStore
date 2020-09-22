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
    private BufferedImage storeTitle;
    private JButton reload;

    /**
     * Create a submenu
     */
    public StoreSubMenu() {
        try {
            storeTitle = ImageIO.read(new File(Config.IMAGE_DIR + "jcappstore-submenu.png"));
            setup();
        } catch (IOException e) {
            setupWithoutImage();
        }
    }

    /**
     * Set callback for re-download
     * @param a action that can re-download the store
     */
    void setOnReload(Action a) {
        reload.addActionListener(a);
    }

    /**
     * Set callback for back
     * @param a action that can return from applet detailed info panel
     */
    void setOnBack(Action a) {
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                a.actionPerformed(null);
            }
        });
    }

    private void setupWithoutImage() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(false);

        back = new JLabel(new ImageIcon(Config.IMAGE_DIR + "store_back.png"));
        add(back);

        Title t = new Title(textSrc.getString("jcappstore"), 30f, SwingConstants.LEFT);
        t.setForeground(Color.white);
        t.setBorder(BorderFactory.createEmptyBorder(10, 40, 5, 0));
        add(t);

        add(Box.createHorizontalStrut(200));

        setupReloadButton();
        reload.setForeground(Color.WHITE);
        reload.setAlignmentY(Component.TOP_ALIGNMENT);
        reload.setMaximumSize(new Dimension(250, reload.getMaximumSize().height));
        add(reload);
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(true);
        setupBackLabelButton();
        add(back);
        setBackground(Color.WHITE);
        setupReloadButton();
        add(reload);
    }

    private void setupBackLabelButton() {
        back = new JLabel(new ImageIcon(Config.IMAGE_DIR + "store_back.png"));
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setupReloadButton() {
        reload = new JButton("<html>" + textSrc.getString("store_refresh") + "</div></html>");
        reload.setUI(new CustomButtonUI());
        reload.setFont(OptionsFactory.getOptions().getFont(Font.BOLD, 12f));
        reload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reload.setAlignmentX(Component.RIGHT_ALIGNMENT);
        reload.setBorder(BorderFactory.createEmptyBorder(15, 20, 13, 0));
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
