package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.LeftMenu;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Notif frames for the left menu
 * left menu used to display other messages that were moved to popup windows/removed instead
 * Kept just for possible use in future
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class NotifLabel extends JLabel {
    private static final Logger logger = LoggerFactory.getLogger(NotifLabel.class);
    private static final ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private final Shape close = new Rectangle(202, 6, 12, 12);
    private final NotifLabel self = this;
    private BufferedImage closeIcon;

    public NotifLabel(String msg, LeftMenu parent) {
        super("<html><p>" + adjustLength(msg) + "</p></html>");

        try {
            this.closeIcon = ImageIO.read(new File(Config.IMAGE_DIR + "close_small.png"));
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Failed to load notification icon", e);
            this.closeIcon = new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB);
        }

        setBorder(new EmptyBorder(8, 8, 8, 8));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (close.contains(me.getPoint())) {
                    parent.remove(self);
                    parent.revalidate();
                } else {
                    InformerFactory.getInformer().showMessage(textSrc.getString("event"), msg, "label.png");
                }
            }
        });

        setPreferredSize(new Dimension(220, 50));
        setMinimumSize(new Dimension(220, 50));
        setMaximumSize(new Dimension(220, 50));
    }

    private static String adjustLength(String value) {
        if (value.length() <= 80) return value;
        return value.substring(0, 77) + "...";
    }


    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 8, 8);
        g2d.setComposite(old);

        g2d.drawImage(closeIcon, null, 204, 8);
        super.paintComponent(g);
    }
}
