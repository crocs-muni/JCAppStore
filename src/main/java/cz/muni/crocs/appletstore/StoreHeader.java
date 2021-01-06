package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.ui.Title;
import cz.muni.crocs.appletstore.util.Options;
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
public class StoreHeader extends BackgroundImgPanel {
    public static final int HEADER_HEIGHT = 350;

    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private final StoreWindowPane parent;
    private final JLabel selectedApps = new HeaderTag();
    private final JLabel allApps = new HeaderTag();
    private final JLabel title;
    private final JLabel redownload;

    private final Color selectedBackground;
    private final Color unselectedBackground;

    /**
     * Create a submenu
     */
    public StoreHeader(StoreWindowPane parentWindow) {
        super(Config.APP_STORE_DIR.getAbsolutePath() + Config.S + Config.APP_STORE_BGIMG_RELPATH);

        setLayout(null);

        parent = parentWindow;
        setMinimumSize(new Dimension(200, HEADER_HEIGHT));


        title = new JLabel(new ImageIcon(Config.IMAGE_DIR + "jcapptitle.png"));
        title.setFont(OptionsFactory.getOptions().getTitleFont(
                2, 35f));
        add(title);

        setAlignmentX(CENTER_ALIGNMENT); //used in StoreWindowPane custom FlowLayout to indicate full row occupied

        selectedBackground = parentWindow.getBackground();
        unselectedBackground = parentWindow.getBackground().darker();

        selectedApps.setText(textSrc.getString("selected_apps"));
        selectedApps.setFont(OptionsFactory.getOptions().getFont().deriveFont(17f));
        selectedApps.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        selectedApps.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        selectedApps.setBackground(selectedBackground);
        selectedApps.setOpaque(true);
        selectedApps.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parent.setShowAll(false);
                allApps.setBackground(unselectedBackground);
                selectedApps.setBackground(selectedBackground);
                allApps.invalidate();
                selectedApps.invalidate();
            }
        });
        allApps.setText(textSrc.getString("all_apps"));
        allApps.setFont(OptionsFactory.getOptions().getFont().deriveFont(17f));
        allApps.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        allApps.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        allApps.setBackground(unselectedBackground);
        allApps.setOpaque(true);
        allApps.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parent.setShowAll(true);
                allApps.setBackground(selectedBackground);
                selectedApps.setBackground(unselectedBackground);
                allApps.invalidate();
                selectedApps.invalidate();
            }
        });

        add(selectedApps);
        add(allApps);

        redownload = new JLabel(textSrc.getString("store_refresh"));
        redownload.setBackground(Color.black);
        redownload.setForeground(Color.white);
        redownload.setFont(OptionsFactory.getOptions().getFont(22f));
        redownload.setOpaque(true);
        redownload.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        redownload.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        redownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parentWindow.redownload();
            }
        });
        add(redownload);

        updateComponentsPosition();
    }



    private void updateComponentsPosition() {
        Insets insets = getInsets();
        Dimension selectedAppsSize = selectedApps.getPreferredSize();
        selectedApps.setBounds(60 + insets.left, HEADER_HEIGHT - selectedAppsSize.height,
                selectedAppsSize.width, selectedAppsSize.height);
        Dimension allAppsSize = selectedApps.getPreferredSize();
        allApps.setBounds(70 + insets.left + selectedAppsSize.width, HEADER_HEIGHT - allAppsSize.height,
                allAppsSize.width, allAppsSize.height);

        title.setBounds(40, 70, 700, 200);

        updateSizeDependentComponentsPosition();
    }

    private void updateSizeDependentComponentsPosition() {
        Insets insets = getInsets();
        Dimension size = redownload.getPreferredSize();
        Dimension bounds = getSize();
        redownload.setBounds(bounds.width - size.width - 50 - insets.right,20 + insets.top, size.width, size.height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        setPreferredSize(new Dimension(parent.getWidth(), HEADER_HEIGHT));
        updateSizeDependentComponentsPosition();
        invalidate();
        super.paintComponent(g);
    }


    private static class HeaderTag extends JLabel {

//        @Override
//        protected void paintComponent(Graphics g) {
//            g.setColor(getBackground());
//            g.fillRoundRect(0, 0, getWidth(),getHeight(), 20, 20);
//            super.paintComponent(g);
//        }
    }
}

